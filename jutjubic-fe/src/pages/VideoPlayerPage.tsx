import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import videoService from '../services/videoService';
import type { Video } from '../types/Video';
import type { Comment } from '../types/Comment';

export const VideoPlayerPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [video, setVideo] = useState<Video | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const viewCountedRef = useRef(false);

  // Comment form state
  const [newComment, setNewComment] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [commentError, setCommentError] = useState<string | null>(null);
  const isLoggedIn = !!localStorage.getItem('token');

  useEffect(() => {
    loadVideo();
    loadComments();
    // Reset view counted flag when video changes
    viewCountedRef.current = false;
  }, [id]);

  // Increment view count when video loads
  useEffect(() => {
    if (video && !viewCountedRef.current) {
      viewCountedRef.current = true;
      videoService.incrementViewCount(video.id).catch(err => {
        console.error('Failed to increment view count:', err);
      });
    }
  }, [video]);

  const loadVideo = async () => {
    try {
      setLoading(true);
      const response = await fetch(`http://localhost:8084/api/videos/${id}`);
      
      if (!response.ok) {
        throw new Error('Video not found');
      }
      
      const data = await response.json();
      setVideo(data);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load video');
      console.error('Error loading video:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async () => {
    try {
      setCommentsLoading(true);
      const response = await fetch(
        `http://localhost:8084/api/videos/${id}/comments?page=0&size=50`
      );

      if (!response.ok) {
        throw new Error('Failed to load comments');
      }

      const data = await response.json();
      // Backend returns Page object with content array
      setComments(data.content || []);
    } catch (err: any) {
      console.error('Error loading comments:', err);
      setComments([]);
    } finally {
      setCommentsLoading(false);
    }
  };

  const handleSubmitComment = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!newComment.trim()) {
      setCommentError('Comment cannot be empty');
      return;
    }

    if (!isLoggedIn) {
      setCommentError('You must be logged in to comment');
      return;
    }

    try {
      setSubmittingComment(true);
      setCommentError(null);

      const createdComment = await videoService.createComment(Number(id), newComment.trim());

      // Add new comment to the top of the list
      setComments(prev => [createdComment, ...prev]);
      setNewComment('');
    } catch (err: any) {
      console.error('Error posting comment:', err);
      if (err.response?.status === 429) {
        setCommentError('Rate limit exceeded. Maximum 60 comments per hour allowed.');
      } else if (err.response?.status === 401) {
        setCommentError('You must be logged in to comment');
      } else {
        setCommentError(err.response?.data?.error || 'Failed to post comment');
      }
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleCommentAuthorClick = (authorId: number) => {
    navigate(`/user/${authorId}`);
  };

  const handleUploaderClick = (uploaderId: number) => {
    navigate(`/user/${uploaderId}`);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return 'Today';
    } else if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      return `${diffDays} days ago`;
    } else {
      return date.toLocaleDateString();
    }
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>Loading video...</div>
      </div>
    );
  }

  if (error || !video) {
    return (
      <div style={styles.container}>
        <div style={styles.errorContainer}>
          <h2 style={styles.errorTitle}>Video Not Found</h2>
          <p style={styles.errorText}>{error || 'The video you are looking for does not exist.'}</p>
          <button onClick={() => navigate('/')} style={styles.backButton}>
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      {/* Video Player Section */}
      <div style={styles.videoSection}>
        {/* Video Player */}
        <div style={styles.videoPlayer}>
          {video.videoPath ? (
            <video
              controls
              autoPlay={false}
              style={styles.videoElement}
              poster={`http://localhost:8084/api/videos/${video.id}/thumbnail`}
            >
              <source
                src={`http://localhost:8084${video.videoPath}`}
                type="video/mp4"
              />
              Your browser does not support the video tag.
            </video>
          ) : (
            <div style={styles.videoPlaceholder}>
              <div style={styles.playIcon}>▶</div>
              <p style={styles.placeholderText}>Video unavailable</p>
              <p style={styles.placeholderSubtext}>{video.title}</p>
            </div>
          )}
        </div>

        {/* Video Info */}
        <div style={styles.videoInfo}>
          <h1 style={styles.videoTitle}>{video.title}</h1>
          
          <div style={styles.videoMeta}>
            <span style={styles.viewCount}>
              {video.viewCount.toLocaleString()} views
            </span>
            <span style={styles.metaSeparator}>•</span>
            <span style={styles.uploadDate}>
              {formatDate(video.createdAt)}
            </span>
          </div>

          {/* Uploader Info */}
          <div style={styles.uploaderSection}>
            <div
              style={styles.uploaderAvatar}
              onClick={() => handleUploaderClick(video.uploader.id)}
            >
              {video.uploader.username.charAt(0).toUpperCase()}
            </div>
            <div style={styles.uploaderInfo}>
              <div
                style={styles.uploaderName}
                onClick={() => handleUploaderClick(video.uploader.id)}
              >
                @{video.uploader.username}
              </div>
              <div style={styles.uploaderFullName}>
                {video.uploader.firstName} {video.uploader.lastName}
              </div>
            </div>
          </div>

          {/* Video Description */}
          {video.description && (
            <div style={styles.descriptionSection}>
              <h3 style={styles.descriptionTitle}>Description</h3>
              <p style={styles.descriptionText}>{video.description}</p>
            </div>
          )}

          {/* Video Tags */}
          {video.tags && video.tags.length > 0 && (
            <div style={styles.tagsSection}>
              {video.tags.map((tag, index) => (
                <span key={index} style={styles.tag}>
                  #{tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Comments Section */}
      <div style={styles.commentsSection}>
        <h2 style={styles.commentsTitle}>
          Comments ({video.commentCount || comments.length})
        </h2>

        {/* Comment Form */}
        {isLoggedIn ? (
          <form onSubmit={handleSubmitComment} style={styles.commentForm}>
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Add a comment..."
              style={styles.commentInput}
              rows={3}
              disabled={submittingComment}
            />
            {commentError && (
              <div style={styles.commentError}>{commentError}</div>
            )}
            <div style={styles.commentFormActions}>
              <button
                type="button"
                onClick={() => setNewComment('')}
                style={styles.cancelButton}
                disabled={submittingComment || !newComment.trim()}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={styles.submitButton}
                disabled={submittingComment || !newComment.trim()}
              >
                {submittingComment ? 'Posting...' : 'Comment'}
              </button>
            </div>
          </form>
        ) : (
          <div style={styles.loginPrompt}>
            <span>Please </span>
            <span
              style={styles.loginLink}
              onClick={() => navigate('/login')}
            >
              log in
            </span>
            <span> to comment</span>
          </div>
        )}

        {commentsLoading ? (
          <div style={styles.commentsLoading}>Loading comments...</div>
        ) : comments.length === 0 ? (
          <div style={styles.noComments}>
            No comments yet. Be the first to comment!
          </div>
        ) : (
          <div style={styles.commentsList}>
            {comments.map((comment) => (
              <div key={comment.id} style={styles.commentItem}>
                {/* Comment Author Avatar */}
                <div
                  style={styles.commentAvatar}
                  onClick={() => handleCommentAuthorClick(comment.author.id)}
                >
                  {comment.author.username.charAt(0).toUpperCase()}
                </div>

                {/* Comment Content */}
                <div style={styles.commentContent}>
                  <div style={styles.commentHeader}>
                    <span
                      style={styles.commentAuthor}
                      onClick={() => handleCommentAuthorClick(comment.author.id)}
                    >
                      @{comment.author.username}
                    </span>
                    <span style={styles.commentDate}>
                      {formatDate(comment.createdAt)}
                    </span>
                  </div>
                  <p style={styles.commentText}>{comment.text}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    minHeight: 'calc(100vh - 60px)',
    backgroundColor: '#0f0f0f',
    padding: '24px',
  },
  loading: {
    color: '#fff',
    fontSize: '18px',
    textAlign: 'center',
    marginTop: '100px',
  },
  errorContainer: {
    maxWidth: '500px',
    margin: '100px auto',
    padding: '40px',
    backgroundColor: '#181818',
    borderRadius: '8px',
    border: '1px solid #303030',
    textAlign: 'center',
  },
  errorTitle: {
    color: '#ff4444',
    fontSize: '24px',
    marginBottom: '16px',
  },
  errorText: {
    color: '#aaa',
    fontSize: '16px',
    marginBottom: '24px',
  },
  backButton: {
    padding: '12px 24px',
    backgroundColor: '#303030',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
  },
  videoSection: {
    maxWidth: '1200px',
    margin: '0 auto',
  },
  videoPlayer: {
    width: '100%',
    aspectRatio: '16 / 9',
    backgroundColor: '#000',
    borderRadius: '12px',
    overflow: 'hidden',
    marginBottom: '16px',
  },
  videoElement: {
    width: '100%',
    height: '100%',
    objectFit: 'contain',
    backgroundColor: '#000',
  },
  videoPlaceholder: {
    width: '100%',
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#1a1a1a',
  },
  playIcon: {
    fontSize: '72px',
    color: '#fff',
    marginBottom: '16px',
    opacity: 0.7,
  },
  placeholderText: {
    color: '#aaa',
    fontSize: '20px',
    marginBottom: '8px',
  },
  placeholderSubtext: {
    color: '#666',
    fontSize: '14px',
  },
  videoInfo: {
    backgroundColor: '#181818',
    borderRadius: '12px',
    padding: '24px',
    marginBottom: '24px',
  },
  videoTitle: {
    color: '#fff',
    fontSize: '24px',
    fontWeight: 600,
    marginBottom: '12px',
    lineHeight: '1.4',
  },
  videoMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginBottom: '24px',
  },
  viewCount: {
    color: '#aaa',
    fontSize: '14px',
  },
  metaSeparator: {
    color: '#666',
    fontSize: '14px',
  },
  uploadDate: {
    color: '#aaa',
    fontSize: '14px',
  },
  uploaderSection: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    paddingBottom: '24px',
    borderBottom: '1px solid #303030',
    marginBottom: '24px',
  },
  uploaderAvatar: {
    width: '48px',
    height: '48px',
    borderRadius: '50%',
    backgroundColor: '#ff0000',
    color: '#fff',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '20px',
    fontWeight: 'bold',
    cursor: 'pointer',
    transition: 'opacity 0.2s',
  },
  uploaderInfo: {
    flex: 1,
  },
  uploaderName: {
    color: '#fff',
    fontSize: '16px',
    fontWeight: 600,
    marginBottom: '4px',
    cursor: 'pointer',
    transition: 'color 0.2s',
  },
  uploaderFullName: {
    color: '#aaa',
    fontSize: '14px',
  },
  descriptionSection: {
    marginBottom: '24px',
  },
  descriptionTitle: {
    color: '#fff',
    fontSize: '16px',
    fontWeight: 600,
    marginBottom: '8px',
  },
  descriptionText: {
    color: '#aaa',
    fontSize: '14px',
    lineHeight: '1.6',
  },
  tagsSection: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '8px',
  },
  tag: {
    color: '#3ea6ff',
    fontSize: '14px',
    padding: '4px 12px',
    backgroundColor: '#263850',
    borderRadius: '16px',
  },
  commentsSection: {
    maxWidth: '1200px',
    margin: '0 auto',
    backgroundColor: '#181818',
    borderRadius: '12px',
    padding: '24px',
  },
  commentsTitle: {
    color: '#fff',
    fontSize: '20px',
    fontWeight: 600,
    marginBottom: '24px',
  },
  commentForm: {
    marginBottom: '24px',
    paddingBottom: '24px',
    borderBottom: '1px solid #303030',
  },
  commentInput: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#0f0f0f',
    border: '1px solid #303030',
    borderRadius: '4px',
    color: '#fff',
    fontSize: '14px',
    resize: 'vertical' as const,
    fontFamily: 'inherit',
    marginBottom: '12px',
  },
  commentFormActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '8px',
  },
  cancelButton: {
    padding: '10px 16px',
    backgroundColor: 'transparent',
    color: '#aaa',
    border: 'none',
    borderRadius: '18px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
  },
  submitButton: {
    padding: '10px 16px',
    backgroundColor: '#3ea6ff',
    color: '#0f0f0f',
    border: 'none',
    borderRadius: '18px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
  },
  commentError: {
    color: '#ff4444',
    fontSize: '12px',
    marginBottom: '12px',
  },
  loginPrompt: {
    color: '#aaa',
    fontSize: '14px',
    padding: '16px',
    backgroundColor: '#181818',
    borderRadius: '8px',
    marginBottom: '24px',
    textAlign: 'center' as const,
  },
  loginLink: {
    color: '#3ea6ff',
    cursor: 'pointer',
    textDecoration: 'underline',
  },
  commentsLoading: {
    color: '#aaa',
    textAlign: 'center',
    padding: '40px',
  },
  noComments: {
    color: '#aaa',
    textAlign: 'center',
    padding: '40px',
    fontSize: '14px',
  },
  commentsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px',
  },
  commentItem: {
    display: 'flex',
    gap: '12px',
  },
  commentAvatar: {
    width: '40px',
    height: '40px',
    borderRadius: '50%',
    backgroundColor: '#3ea6ff',
    color: '#fff',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '16px',
    fontWeight: 'bold',
    cursor: 'pointer',
    flexShrink: 0,
    transition: 'opacity 0.2s',
  },
  commentContent: {
    flex: 1,
  },
  commentHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginBottom: '8px',
  },
  commentAuthor: {
    color: '#fff',
    fontSize: '14px',
    fontWeight: 600,
    cursor: 'pointer',
    transition: 'color 0.2s',
  },
  commentDate: {
    color: '#666',
    fontSize: '12px',
  },
  commentText: {
    color: '#aaa',
    fontSize: '14px',
    lineHeight: '1.5',
    margin: 0,
  },
};