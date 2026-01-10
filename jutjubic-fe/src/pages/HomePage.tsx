import { useEffect, useState } from 'react';
import videoService from '../services/videoService';
import type { Video } from '../types/Video';

export const HomePage = () => {
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
          <div style={styles.noVideos}>No videos available yet</div>
        ) : (
          videos.map((video) => (
            <div key={video.id} style={styles.videoCard}>
              {/* Thumbnail placeholder */}
              <div style={styles.thumbnail}>
                <div style={styles.thumbnailPlaceholder}>
                  {video.title.charAt(0).toUpperCase()}
                </div>
              </div>
              
              {/* Video info */}
              <div style={styles.videoInfo}>
                <h3 style={styles.videoTitle}>{video.title}</h3>
                <div style={styles.videoMeta}>
                  <span>{video.uploader.username}</span>
                  <span style={styles.separator}>•</span>
                  <span>{video.viewCount} views</span>
                  <span style={styles.separator}>•</span>
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
    padding: '24px',
    maxWidth: '1920px',
    margin: '0 auto',
  },
  title: {
    color: '#fff',
    fontSize: '24px',
    marginBottom: '24px',
  },
  loading: {
    color: '#aaa',
    textAlign: 'center',
    padding: '48px',
    fontSize: '18px',
  },
  error: {
    color: '#ff4444',
    textAlign: 'center',
    padding: '48px',
    fontSize: '18px',
  },
  noVideos: {
    color: '#aaa',
    textAlign: 'center',
    padding: '48px',
    fontSize: '18px',
  },
  videoGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
    gap: '24px',
  },
  videoCard: {
    cursor: 'pointer',
    transition: 'transform 0.2s',
  },
  thumbnail: {
    position: 'relative',
    paddingBottom: '56.25%', // 16:9 aspect ratio
    backgroundColor: '#181818',
    borderRadius: '12px',
    overflow: 'hidden',
    marginBottom: '12px',
  },
  thumbnailPlaceholder: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '64px',
    color: '#fff',
    backgroundColor: '#3ea6ff',
  },
  videoInfo: {
    padding: '0 4px',
  },
  videoTitle: {
    color: '#fff',
    fontSize: '16px',
    fontWeight: 500,
    marginBottom: '8px',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical',
  },
  videoMeta: {
    color: '#aaa',
    fontSize: '14px',
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
  },
  separator: {
    margin: '0 4px',
  },
};