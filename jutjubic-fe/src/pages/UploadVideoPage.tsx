import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import videoService from '../services/videoService';

export const UploadVideoPage = () => {
  const navigate = useNavigate();
  
  // Form state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [tags, setTags] = useState('');
  const [location, setLocation] = useState('');
  const [videoFile, setVideoFile] = useState<File | null>(null);
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  
  // UI state
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  const handleVideoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      
      if (file.type !== 'video/mp4') {
        setError('Only MP4 videos are supported');
        return;
      }
      
      if (file.size > 200 * 1024 * 1024) {
        setError('Video file must be less than 200MB');
        return;
      }
      
      setVideoFile(file);
      setError(null);
    }
  };

  const handleThumbnailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      
      if (!file.type.startsWith('image/')) {
        setError('Thumbnail must be an image (JPEG or PNG)');
        return;
      }
      
      setThumbnailFile(file);
      setError(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    // CRITICAL: Prevent default IMMEDIATELY as first line!
    e.preventDefault();
    e.stopPropagation();
    
    console.log('=== FORM SUBMIT HANDLER CALLED ===');
    console.log('Event prevented!');
    
    setError(null);

    console.log('Validating form...');
    console.log('Title:', title);
    console.log('Video file:', videoFile);
    console.log('Thumbnail file:', thumbnailFile);

    // Validation
    if (!title.trim()) {
      console.error('Validation failed: No title');
      setError('Title is required');
      return;
    }
    if (!videoFile) {
      console.error('Validation failed: No video');
      setError('Video file is required');
      return;
    }
    if (!thumbnailFile) {
      console.error('Validation failed: No thumbnail');
      setError('Thumbnail is required');
      return;
    }

    console.log('✅ Validation passed!');

    try {
      console.log('Setting uploading state to true...');
      setUploading(true);
      setUploadProgress(0);

      // Parse tags
      const tagArray = tags
        .split(',')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0);

      console.log('Calling videoService.uploadVideo...');
      const uploadedVideo = await videoService.uploadVideo(
        {
          title,
          description,
          tags: tagArray,
          location: location.trim() || undefined,
        },
        videoFile,
        thumbnailFile,
        (progress) => {
          console.log('Progress:', progress + '%');
          setUploadProgress(progress);
        }
      );

      console.log('✅ Upload successful!', uploadedVideo);
      setSuccess(true);
      
      setTimeout(() => {
        console.log('Navigating to video page...');
        navigate(`/video/${uploadedVideo.id}`);
      }, 1000);

    } catch (err: any) {
      console.error('❌ Upload error:', err);
      setError(err.response?.data?.error || 'Failed to upload video');
      setUploading(false);
    }
  };

  // DEFENSIVE: Also prevent form submission via button click
  const handleButtonClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();
    console.log('=== UPLOAD BUTTON CLICKED ===');
    console.log('Button click prevented, calling handleSubmit manually...');
    
    // Manually call handleSubmit with a synthetic form event
    handleSubmit(e as any);
  };

  if (success) {
    return (
      <div style={styles.container}>
        <div style={styles.successCard}>
          <h2 style={styles.successTitle}>✅ Upload Successful!</h2>
          <p style={styles.successMessage}>Redirecting to your video...</p>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.uploadCard}>
        <h1 style={styles.title}>Upload Video</h1>
        
        {error && (
          <div style={styles.errorBox}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={styles.form}>
          {/* Title */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Title *</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Enter video title"
              style={styles.input}
              maxLength={200}
              disabled={uploading}
            />
            <span style={styles.charCount}>{title.length}/200</span>
          </div>

          {/* Description */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Enter video description"
              style={styles.textarea}
              rows={4}
              maxLength={5000}
              disabled={uploading}
            />
            <span style={styles.charCount}>{description.length}/5000</span>
          </div>

          {/* Tags */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Tags</label>
            <input
              type="text"
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              placeholder="tutorial, java, spring boot (comma-separated)"
              style={styles.input}
              disabled={uploading}
            />
            <span style={styles.hint}>Separate tags with commas</span>
          </div>

          {/* Location */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Location (optional)</label>
            <input
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="Belgrade, Serbia"
              style={styles.input}
              disabled={uploading}
            />
          </div>

          {/* Video File */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Video File * (MP4, max 200MB)</label>
            <input
              type="file"
              accept="video/mp4"
              onChange={handleVideoChange}
              style={styles.fileInput}
              disabled={uploading}
            />
            {videoFile && (
              <div style={styles.fileInfo}>
                📹 {videoFile.name} ({formatFileSize(videoFile.size)})
              </div>
            )}
          </div>

          {/* Thumbnail File */}
          <div style={styles.formGroup}>
            <label style={styles.label}>Thumbnail * (JPEG or PNG)</label>
            <input
              type="file"
              accept="image/jpeg,image/png"
              onChange={handleThumbnailChange}
              style={styles.fileInput}
              disabled={uploading}
            />
            {thumbnailFile && (
              <div style={styles.fileInfo}>
                🖼️ {thumbnailFile.name} ({formatFileSize(thumbnailFile.size)})
              </div>
            )}
          </div>

          {/* Upload Progress */}
          {uploading && (
            <div style={styles.progressContainer}>
              <div style={styles.progressBar}>
                <div 
                  style={{
                    ...styles.progressFill,
                    width: `${uploadProgress}%`
                  }}
                />
              </div>
              <span style={styles.progressText}>{uploadProgress}%</span>
            </div>
          )}

          {/* Submit Button - CHANGED TO BUTTON TYPE */}
          <button
            type="button"
            onClick={handleButtonClick}
            disabled={uploading}
            style={{
              ...styles.submitButton,
              ...(uploading ? styles.submitButtonDisabled : {})
            }}
          >
            {uploading ? '🎬 Uploading...' : '🎥 Upload Video'}
          </button>

          {/* Cancel Button */}
          {!uploading && (
            <button
              type="button"
              onClick={() => navigate('/')}
              style={styles.cancelButton}
            >
              Cancel
            </button>
          )}
        </form>
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    maxWidth: '800px',
    margin: '40px auto',
    padding: '0 20px',
  },
  uploadCard: {
    backgroundColor: '#fff',
    borderRadius: '8px',
    padding: '30px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '30px',
    color: '#333',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  label: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#333',
  },
  input: {
    padding: '10px 12px',
    fontSize: '14px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    outline: 'none',
    transition: 'border-color 0.2s',
  },
  textarea: {
    padding: '10px 12px',
    fontSize: '14px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    outline: 'none',
    resize: 'vertical',
    fontFamily: 'inherit',
  },
  fileInput: {
    padding: '10px',
    fontSize: '14px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    cursor: 'pointer',
  },
  fileInfo: {
    fontSize: '13px',
    color: '#666',
    padding: '8px',
    backgroundColor: '#f5f5f5',
    borderRadius: '4px',
  },
  charCount: {
    fontSize: '12px',
    color: '#999',
    textAlign: 'right',
  },
  hint: {
    fontSize: '12px',
    color: '#666',
    fontStyle: 'italic',
  },
  errorBox: {
    padding: '12px',
    backgroundColor: '#fee',
    color: '#c00',
    borderRadius: '4px',
    marginBottom: '20px',
    fontSize: '14px',
  },
  progressContainer: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    marginTop: '10px',
  },
  progressBar: {
    flex: 1,
    height: '8px',
    backgroundColor: '#e0e0e0',
    borderRadius: '4px',
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#ff4d4f',
    transition: 'width 0.3s ease',
  },
  progressText: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#333',
    minWidth: '45px',
  },
  submitButton: {
    padding: '14px',
    fontSize: '16px',
    fontWeight: '600',
    color: '#fff',
    backgroundColor: '#ff4d4f',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    marginTop: '10px',
    transition: 'background-color 0.2s',
  },
  submitButtonDisabled: {
    backgroundColor: '#ccc',
    cursor: 'not-allowed',
  },
  cancelButton: {
    padding: '12px',
    fontSize: '14px',
    color: '#666',
    backgroundColor: 'transparent',
    border: '1px solid #ddd',
    borderRadius: '4px',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
  successCard: {
    backgroundColor: '#fff',
    borderRadius: '8px',
    padding: '60px 30px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    textAlign: 'center',
  },
  successTitle: {
    fontSize: '32px',
    color: '#52c41a',
    marginBottom: '16px',
  },
  successMessage: {
    fontSize: '16px',
    color: '#666',
  },
};