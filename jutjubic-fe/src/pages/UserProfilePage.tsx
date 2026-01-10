import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import type { User } from '../types/User';

export const UserProfilePage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadUser();
  }, [id]);

  const loadUser = async () => {
    try {
      setLoading(true);
      const response = await fetch(`http://localhost:8084/api/users/${id}`);
      
      if (!response.ok) {
        throw new Error('User not found');
      }
      
      const data = await response.json();
      setUser(data);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load user profile');
      console.error('Error loading user:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>Loading profile...</div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div style={styles.container}>
        <div style={styles.errorContainer}>
          <h2 style={styles.errorTitle}>User Not Found</h2>
          <p style={styles.errorText}>{error || 'The user you are looking for does not exist.'}</p>
          <button onClick={() => navigate('/')} style={styles.backButton}>
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.profileCard}>
        {/* Profile Header */}
        <div style={styles.header}>
          <div style={styles.avatar}>
            {user.username.charAt(0).toUpperCase()}
          </div>
          <div style={styles.headerInfo}>
            <h1 style={styles.username}>@{user.username}</h1>
            <p style={styles.fullName}>
              {user.firstName} {user.lastName}
            </p>
            <p style={styles.email}>{user.email}</p>
          </div>
        </div>

        {/* Profile Details */}
        <div style={styles.detailsSection}>
          <h2 style={styles.sectionTitle}>Profile Information</h2>
          
          <div style={styles.detailsGrid}>
            <div style={styles.detailItem}>
              <span style={styles.detailLabel}>Username</span>
              <span style={styles.detailValue}>{user.username}</span>
            </div>
            
            <div style={styles.detailItem}>
              <span style={styles.detailLabel}>Full Name</span>
              <span style={styles.detailValue}>
                {user.firstName} {user.lastName}
              </span>
            </div>
            
            <div style={styles.detailItem}>
              <span style={styles.detailLabel}>Email</span>
              <span style={styles.detailValue}>{user.email}</span>
            </div>
            
            {user.id && (
              <div style={styles.detailItem}>
                <span style={styles.detailLabel}>User ID</span>
                <span style={styles.detailValue}>{user.id}</span>
              </div>
            )}
          </div>
        </div>

        {/* Back Button */}
        <div style={styles.footer}>
          <button onClick={() => navigate(-1)} style={styles.backButton}>
            ← Back
          </button>
        </div>
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    minHeight: 'calc(100vh - 60px)',
    padding: '40px 24px',
    backgroundColor: '#0f0f0f',
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
  profileCard: {
    maxWidth: '800px',
    margin: '0 auto',
    backgroundColor: '#181818',
    borderRadius: '12px',
    border: '1px solid #303030',
    overflow: 'hidden',
  },
  header: {
    padding: '40px',
    background: 'linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%)',
    borderBottom: '1px solid #303030',
    display: 'flex',
    alignItems: 'center',
    gap: '24px',
  },
  avatar: {
    width: '100px',
    height: '100px',
    borderRadius: '50%',
    backgroundColor: '#ff0000',
    color: '#fff',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '40px',
    fontWeight: 'bold',
    flexShrink: 0,
  },
  headerInfo: {
    flex: 1,
  },
  username: {
    color: '#fff',
    fontSize: '32px',
    fontWeight: 600,
    marginBottom: '8px',
  },
  fullName: {
    color: '#aaa',
    fontSize: '18px',
    marginBottom: '4px',
  },
  email: {
    color: '#666',
    fontSize: '14px',
  },
  detailsSection: {
    padding: '40px',
  },
  sectionTitle: {
    color: '#fff',
    fontSize: '20px',
    fontWeight: 600,
    marginBottom: '24px',
    paddingBottom: '12px',
    borderBottom: '1px solid #303030',
  },
  detailsGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '24px',
  },
  detailItem: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  detailLabel: {
    color: '#888',
    fontSize: '12px',
    textTransform: 'uppercase',
    letterSpacing: '1px',
    fontWeight: 500,
  },
  detailValue: {
    color: '#fff',
    fontSize: '16px',
  },
  footer: {
    padding: '24px 40px',
    borderTop: '1px solid #303030',
    backgroundColor: '#1a1a1a',
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
    transition: 'background-color 0.2s',
  },
};