import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import watchPartyService from '../services/watchPartyService';
import videoService from '../services/videoService';
import type { WatchPartyRoom, WatchPartyPlay } from '../types/WatchParty';
import type { Video } from '../types/Video';

export const WatchPartyPage = () => {
  const { roomId } = useParams<{ roomId?: string }>();
  const navigate = useNavigate();

  const [room, setRoom] = useState<WatchPartyRoom | null>(null);
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [connected, setConnected] = useState(false);

  const stompClientRef = useRef<Client | null>(null);

  // Room mode: join + subscribe (only runs when roomId exists)
  useEffect(() => {
    if (!roomId) return;

    let mounted = true;

    const init = async () => {
      try {
        setLoading(true);
        const roomData = await watchPartyService.joinRoom(roomId);
        if (!mounted) return;
        setRoom(roomData);

        const videoData = await videoService.getAllVideos();
        if (!mounted) return;
        setVideos(videoData);

        setError(null);
      } catch (err: any) {
        if (!mounted) return;
        const msg = err.response?.data?.error || 'Failed to join room';
        setError(msg);
      } finally {
        if (mounted) setLoading(false);
      }
    };

    init();

    // Connect WebSocket
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8084/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        if (!mounted) return;
        setConnected(true);

        client.subscribe(`/topic/watchparty/${roomId}`, (message) => {
          const play: WatchPartyPlay = JSON.parse(message.body);
          navigate(`/video/${play.videoId}`);
        });
      },
      onDisconnect: () => {
        if (mounted) setConnected(false);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      mounted = false;
      watchPartyService.leaveRoom(roomId).catch(() => {});
      if (stompClientRef.current?.active) {
        stompClientRef.current.deactivate();
      }
    };
  }, [roomId, navigate]);

  // Periodically refresh room members
  useEffect(() => {
    if (!roomId || error) return;

    const interval = setInterval(async () => {
      try {
        const roomData = await watchPartyService.getRoom(roomId);
        setRoom(roomData);
      } catch {
        // Room may have been deleted
      }
    }, 5000);

    return () => clearInterval(interval);
  }, [roomId, error]);

  const handlePlayVideo = (videoId: number) => {
    if (!stompClientRef.current?.active || !room) return;

    stompClientRef.current.publish({
      destination: '/app/watchparty/play',
      body: JSON.stringify({ roomId: room.roomId, videoId }),
    });
  };

  const copyRoomLink = () => {
    if (!roomId) return;
    const link = `${window.location.origin}/watchparty/${roomId}`;
    navigator.clipboard.writeText(link);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Create mode: no roomId
  if (!roomId) {
    return <CreateRoomView />;
  }

  // Decode username from JWT token payload
  const token = localStorage.getItem('token');
  let currentUsername: string | null = null;
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      currentUsername = payload.sub || null;
    } catch {
      // invalid token
    }
  }
  const isOwner = room?.owner?.username === currentUsername;

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loadingText}>Joining watch party...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={styles.container}>
        <div style={styles.errorCard}>
          <h2 style={styles.errorTitle}>Could not join room</h2>
          <p style={styles.errorMsg}>{error}</p>
          <button onClick={() => navigate('/watchparty')} style={styles.primaryBtn}>
            Create New Room
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      {/* Room Info Card */}
      <div style={styles.roomCard}>
        <div style={styles.roomHeader}>
          <h1 style={styles.roomTitle}>Watch Party</h1>
          <div style={styles.statusDot(connected)} />
          <span style={styles.statusText}>{connected ? 'Connected' : 'Connecting...'}</span>
        </div>

        <div style={styles.roomIdRow}>
          <span style={styles.roomIdLabel}>Room ID:</span>
          <code style={styles.roomIdCode}>{roomId}</code>
          <button onClick={copyRoomLink} style={styles.copyBtn}>
            {copied ? 'Copied!' : 'Copy Link'}
          </button>
        </div>

        <div style={styles.membersSection}>
          <h3 style={styles.membersTitle}>
            Members ({room?.members?.length || 0})
          </h3>
          <div style={styles.membersList}>
            {room?.members?.map((member) => (
              <div key={member.id} style={styles.memberChip}>
                <div style={styles.memberAvatar}>
                  {member.username.charAt(0).toUpperCase()}
                </div>
                <span style={styles.memberName}>
                  {member.username}
                  {member.username === room.owner.username && (
                    <span style={styles.ownerBadge}>Host</span>
                  )}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Video Selection (owner) or Waiting (member) */}
      {isOwner ? (
        <div style={styles.videoSection}>
          <h2 style={styles.sectionTitle}>Pick a video to play for everyone</h2>
          <div style={styles.videoGrid}>
            {videos.length === 0 ? (
              <div style={styles.noVideos}>No videos available</div>
            ) : (
              videos.map((video) => (
                <div
                  key={video.id}
                  style={styles.videoCard}
                  onClick={() => handlePlayVideo(video.id)}
                >
                  <div style={styles.thumbnail}>
                    {video.thumbnailPath ? (
                      <img
                        src={`http://localhost:8084/api/videos/${video.id}/thumbnail`}
                        alt={video.title}
                        style={styles.thumbnailImage}
                        onError={(e) => {
                          (e.target as HTMLImageElement).style.display = 'none';
                        }}
                      />
                    ) : null}
                    <div style={{
                      ...styles.thumbnailPlaceholder,
                      display: video.thumbnailPath ? 'none' : 'flex',
                    }}>
                      {video.title.charAt(0).toUpperCase()}
                    </div>
                    <div style={styles.playOverlay}>Play</div>
                  </div>
                  <div style={styles.videoInfo}>
                    <h3 style={styles.videoTitle}>{video.title}</h3>
                    <div style={styles.videoMeta}>
                      <span>@{video.uploader.username}</span>
                      <span>{video.viewCount.toLocaleString()} views</span>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      ) : (
        <div style={styles.waitingSection}>
          <div style={styles.waitingIcon}>...</div>
          <h2 style={styles.waitingTitle}>Waiting for host to play a video</h2>
          <p style={styles.waitingSubtext}>
            The host ({room?.owner?.username}) will select a video for everyone to watch together.
          </p>
        </div>
      )}
    </div>
  );
};

// Separate component for create mode to avoid conditional hook calls
const CreateRoomView = () => {
  const navigate = useNavigate();
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCreate = async () => {
    try {
      setCreating(true);
      setError(null);
      const room = await watchPartyService.createRoom();
      navigate(`/watchparty/${room.roomId}`);
    } catch (err: any) {
      const msg = err.response?.data?.error || 'Failed to create room';
      setError(msg);
    } finally {
      setCreating(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.createCard}>
        <h1 style={styles.createTitle}>Watch Party</h1>
        <p style={styles.createSubtext}>
          Watch videos together with friends in real-time. Create a room and share the link!
        </p>
        {error && <div style={styles.createError}>{error}</div>}
        <button
          onClick={handleCreate}
          disabled={creating}
          style={{
            ...styles.primaryBtn,
            opacity: creating ? 0.6 : 1,
          }}
        >
          {creating ? 'Creating...' : 'Create Room'}
        </button>
      </div>
    </div>
  );
};

const styles: { [key: string]: any } = {
  container: {
    maxWidth: '1400px',
    margin: '0 auto',
    padding: '24px',
    minHeight: 'calc(100vh - 60px)',
  },
  loadingText: {
    color: '#fff',
    textAlign: 'center',
    padding: '100px 0',
    fontSize: '18px',
  },

  // Error
  errorCard: {
    backgroundColor: '#181818',
    border: '1px solid #303030',
    borderRadius: '12px',
    padding: '48px',
    textAlign: 'center',
    maxWidth: '500px',
    margin: '80px auto',
  },
  errorTitle: {
    color: '#fff',
    fontSize: '22px',
    marginBottom: '12px',
  },
  errorMsg: {
    color: '#aaa',
    fontSize: '14px',
    marginBottom: '24px',
  },

  // Create mode
  createCard: {
    backgroundColor: '#181818',
    border: '1px solid #303030',
    borderRadius: '12px',
    padding: '64px 48px',
    textAlign: 'center',
    maxWidth: '500px',
    margin: '80px auto',
  },
  createTitle: {
    color: '#fff',
    fontSize: '28px',
    fontWeight: 700,
    marginBottom: '12px',
  },
  createSubtext: {
    color: '#aaa',
    fontSize: '16px',
    marginBottom: '32px',
    lineHeight: '1.5',
  },
  createError: {
    color: '#ff4444',
    fontSize: '14px',
    marginBottom: '16px',
  },

  // Buttons
  primaryBtn: {
    padding: '12px 32px',
    backgroundColor: '#ff0000',
    border: 'none',
    color: '#fff',
    borderRadius: '4px',
    fontSize: '16px',
    fontWeight: 600,
    cursor: 'pointer',
  },
  copyBtn: {
    padding: '6px 14px',
    backgroundColor: '#3ea6ff',
    border: 'none',
    color: '#fff',
    borderRadius: '4px',
    fontSize: '13px',
    fontWeight: 500,
    cursor: 'pointer',
    whiteSpace: 'nowrap',
  },

  // Room card
  roomCard: {
    backgroundColor: '#181818',
    border: '1px solid #303030',
    borderRadius: '12px',
    padding: '24px',
    marginBottom: '32px',
  },
  roomHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    marginBottom: '16px',
  },
  roomTitle: {
    color: '#fff',
    fontSize: '22px',
    fontWeight: 700,
    margin: 0,
  },
  statusDot: (connected: boolean): React.CSSProperties => ({
    width: '10px',
    height: '10px',
    borderRadius: '50%',
    backgroundColor: connected ? '#4caf50' : '#ff9800',
    marginLeft: '8px',
  }),
  statusText: {
    color: '#aaa',
    fontSize: '13px',
  },
  roomIdRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    marginBottom: '20px',
    flexWrap: 'wrap',
  },
  roomIdLabel: {
    color: '#aaa',
    fontSize: '14px',
  },
  roomIdCode: {
    color: '#3ea6ff',
    backgroundColor: '#0f0f0f',
    padding: '4px 10px',
    borderRadius: '4px',
    fontSize: '14px',
    fontFamily: 'monospace',
  },

  // Members
  membersSection: {
    borderTop: '1px solid #303030',
    paddingTop: '16px',
  },
  membersTitle: {
    color: '#fff',
    fontSize: '16px',
    fontWeight: 600,
    marginBottom: '12px',
  },
  membersList: {
    display: 'flex',
    gap: '10px',
    flexWrap: 'wrap',
  },
  memberChip: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    backgroundColor: '#282828',
    padding: '6px 14px',
    borderRadius: '20px',
  },
  memberAvatar: {
    width: '28px',
    height: '28px',
    borderRadius: '50%',
    backgroundColor: '#3ea6ff',
    color: '#fff',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '13px',
    fontWeight: 700,
  },
  memberName: {
    color: '#fff',
    fontSize: '14px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },
  ownerBadge: {
    backgroundColor: '#ff0000',
    color: '#fff',
    padding: '1px 6px',
    borderRadius: '4px',
    fontSize: '11px',
    fontWeight: 600,
  },

  // Video section (owner)
  sectionTitle: {
    color: '#fff',
    fontSize: '18px',
    fontWeight: 600,
    marginBottom: '20px',
  },
  videoSection: {},
  videoGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
    gap: '20px',
  },
  noVideos: {
    color: '#aaa',
    textAlign: 'center',
    padding: '60px 0',
    fontSize: '16px',
    gridColumn: '1 / -1',
  },
  videoCard: {
    backgroundColor: '#181818',
    borderRadius: '12px',
    overflow: 'hidden',
    cursor: 'pointer',
    border: '2px solid #303030',
    transition: 'border-color 0.2s',
    position: 'relative',
  },
  thumbnail: {
    width: '100%',
    aspectRatio: '16 / 9',
    backgroundColor: '#0f0f0f',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    overflow: 'hidden',
  },
  thumbnailImage: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  thumbnailPlaceholder: {
    fontSize: '48px',
    color: '#3ea6ff',
    fontWeight: 'bold',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
    position: 'absolute',
    top: 0,
    left: 0,
  },
  playOverlay: {
    position: 'absolute',
    inset: 0,
    backgroundColor: 'rgba(255, 0, 0, 0.0)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: 'transparent',
    fontSize: '18px',
    fontWeight: 700,
    transition: 'all 0.2s',
  },
  videoInfo: {
    padding: '12px',
  },
  videoTitle: {
    color: '#fff',
    fontSize: '14px',
    fontWeight: 600,
    marginBottom: '4px',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  videoMeta: {
    color: '#888',
    fontSize: '12px',
    display: 'flex',
    gap: '8px',
  },

  // Waiting section (non-owner)
  waitingSection: {
    textAlign: 'center',
    padding: '80px 24px',
  },
  waitingIcon: {
    fontSize: '48px',
    color: '#aaa',
    marginBottom: '16px',
    letterSpacing: '8px',
  },
  waitingTitle: {
    color: '#fff',
    fontSize: '22px',
    fontWeight: 600,
    marginBottom: '12px',
  },
  waitingSubtext: {
    color: '#aaa',
    fontSize: '15px',
    maxWidth: '400px',
    margin: '0 auto',
    lineHeight: '1.5',
  },
};
