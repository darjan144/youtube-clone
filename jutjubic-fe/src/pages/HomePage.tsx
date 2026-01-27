import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import videoService from '../services/videoService';
import type { Video } from '../types/Video';

export const HomePage = () => {
  const navigate = useNavigate();
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadVideos();
  }, []);

  const loadVideos = async () => {
    try {
      setLoading(true);
      const data = await videoService.getAllVideos();
      setVideos(data);
      setError(null);
    } catch (err) {
      setError('Failed to load videos');
      console.error('Error loading videos:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleVideoClick = (videoId: number) => {
    navigate(`/video/${videoId}`);
  };

  const handleUploaderClick = (e: React.MouseEvent, uploaderId: number) => {
    e.stopPropagation(); // Prevent video click when clicking uploader
    navigate(`/user/${uploaderId}`);
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>Loading videos...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={styles.container}>
        <div style={styles.error}>{error}</div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Videos</h1>
      <div style={styles.videoGrid}>
        {videos.length === 0 ? (
          <div style={styles.noVideos}>No videos available</div>
        ) : (
          videos.map((video) => (
            <div
              key={video.id}
              style={styles.videoCard}
              onClick={() => handleVideoClick(video.id)}
            >
              {/* Thumbnail */}
              <div style={styles.thumbnail}>
                {video.thumbnailPath ? (
                  <img
                    src={`http://localhost:8084/api/videos/${video.id}/thumbnail`}
                    alt={video.title}
                    style={styles.thumbnailImage}
                    onError={(e) => {
                      // Fallback to placeholder if image fails to load
                      (e.target as HTMLImageElement).style.display = 'none';
                      (e.target as HTMLImageElement).nextElementSibling?.removeAttribute('style');
                    }}
                  />
                ) : null}
                <div style={{
                  ...styles.thumbnailPlaceholder,
                  display: video.thumbnailPath ? 'none' : 'flex'
                }}>
                  {video.title.charAt(0).toUpperCase()}
                </div>
                {/* Red dot indicator in corner per spec 3.1 */}
                <div style={styles.redDot} />
              </div>

              {/* Video Info */}
              <div style={styles.videoInfo}>
                <h3 style={styles.videoTitle}>{video.title}</h3>
                
                {/* Uploader - Clickable */}
                <div
                  style={styles.uploaderLink}
                  onClick={(e) => handleUploaderClick(e, video.uploader.id)}
                >
                  @{video.uploader.username}
                </div>

                <div style={styles.videoMeta}>
                  <span>{video.viewCount.toLocaleString()} views</span>
                  <span>•</span>
                  <span>{new Date(video.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    maxWidth: '1400px',
    margin: '0 auto',
    padding: '24px',
    minHeight: 'calc(100vh - 60px)',
  },
  title: {
    color: '#fff',
    fontSize: '24px',
    fontWeight: 600,
    marginBottom: '24px',
  },
  loading: {
    color: '#fff',
    textAlign: 'center',
    padding: '100px 0',
    fontSize: '18px',
  },
  error: {
    color: '#ff4444',
    textAlign: 'center',
    padding: '100px 0',
    fontSize: '18px',
  },
  noVideos: {
    color: '#aaa',
    textAlign: 'center',
    padding: '100px 0',
    fontSize: '18px',
    gridColumn: '1 / -1',
  },
  videoGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
    gap: '24px',
  },
  videoCard: {
    backgroundColor: '#181818',
    borderRadius: '12px',
    overflow: 'hidden',
    cursor: 'pointer',
    transition: 'transform 0.2s, box-shadow 0.2s',
    border: '2px solid #ff0000', // Red border per spec 3.1
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
  },
  thumbnailImage: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  thumbnailPlaceholder: {
    fontSize: '64px',
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
  redDot: {
    position: 'absolute',
    top: '8px',
    right: '8px',
    width: '12px',
    height: '12px',
    backgroundColor: '#ff0000',
    borderRadius: '50%',
  },
  videoInfo: {
    padding: '16px',
  },
  videoTitle: {
    color: '#fff',
    fontSize: '16px',
    fontWeight: 600,
    marginBottom: '8px',
    lineHeight: '1.4',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical',
  },
  uploaderLink: {
    color: '#aaa',
    fontSize: '14px',
    marginBottom: '8px',
    cursor: 'pointer',
    transition: 'color 0.2s',
    display: 'inline-block',
  },
  videoMeta: {
    color: '#888',
    fontSize: '12px',
    display: 'flex',
    gap: '6px',
    alignItems: 'center',
  },
};