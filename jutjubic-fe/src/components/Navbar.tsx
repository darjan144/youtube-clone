import { Link } from 'react-router-dom';
import { useState } from 'react';

interface NavbarProps {
  isAuthenticated: boolean;
  username?: string;
  onLogout: () => void;
}

export const Navbar = ({ isAuthenticated, username, onLogout }: NavbarProps) => {
  const [showUserMenu, setShowUserMenu] = useState(false);

  return (
    <nav style={styles.nav}>
      <div style={styles.container}>
        {/* Logo/Brand */}
        <Link to="/" style={styles.logo}>
          Jutjubić
        </Link>

        {/* Search Bar - placeholder for now */}
        <div style={styles.searchContainer}>
          <input 
            type="text" 
            placeholder="Search videos..." 
            style={styles.searchInput}
          />
        </div>

        {/* Auth Section */}
        <div style={styles.authSection}>
          {isAuthenticated ? (
            <div style={styles.authenticatedSection}>
              {/* Upload Button */}
              <Link to="/upload" style={styles.uploadButton}>
                + Upload Video
              </Link>

              {/* Watch Party Button */}
              <Link to="/watchparty" style={styles.watchPartyButton}>
                Watch Party
              </Link>
              
              {/* User Menu */}
              <div style={styles.userMenuContainer}>
                <button 
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  style={styles.userButton}
                >
                  {username || 'User'}
                </button>
                
                {showUserMenu && (
                  <div style={styles.dropdown}>
                    <Link to="/my-videos" style={styles.dropdownItem}>
                      My Videos
                    </Link>
                    <button onClick={onLogout} style={styles.dropdownItem}>
                      Logout
                    </button>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div style={styles.authButtons}>
              <Link to="/login" style={styles.loginButton}>
                Login
              </Link>
              <Link to="/register" style={styles.registerButton}>
                Register
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  nav: {
    backgroundColor: '#202020',
    padding: '12px 24px',
    borderBottom: '1px solid #303030',
    position: 'sticky',
    top: 0,
    zIndex: 1000,
  },
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    maxWidth: '1920px',
    margin: '0 auto',
  },
  logo: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#ff0000',
    textDecoration: 'none',
    marginRight: '40px',
  },
  searchContainer: {
    flex: 1,
    maxWidth: '600px',
  },
  searchInput: {
    width: '100%',
    padding: '8px 16px',
    backgroundColor: '#121212',
    border: '1px solid #303030',
    borderRadius: '2px',
    color: '#fff',
    fontSize: '16px',
  },
  authSection: {
    marginLeft: '40px',
  },
  authenticatedSection: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
  },
  uploadButton: {
    padding: '8px 16px',
    backgroundColor: '#ff0000',
    border: 'none',
    color: '#fff',
    textDecoration: 'none',
    borderRadius: '2px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
  },
  watchPartyButton: {
    padding: '8px 16px',
    backgroundColor: 'transparent',
    border: '1px solid #ff0000',
    color: '#ff0000',
    textDecoration: 'none',
    borderRadius: '2px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
  },
  authButtons: {
    display: 'flex',
    gap: '12px',
  },
  loginButton: {
    padding: '8px 16px',
    backgroundColor: 'transparent',
    border: '1px solid #3ea6ff',
    color: '#3ea6ff',
    textDecoration: 'none',
    borderRadius: '2px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
  },
  registerButton: {
    padding: '8px 16px',
    backgroundColor: '#3ea6ff',
    border: 'none',
    color: '#fff',
    textDecoration: 'none',
    borderRadius: '2px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
  },
  userMenuContainer: {
    position: 'relative',
  },
  userButton: {
    padding: '8px 16px',
    backgroundColor: '#3ea6ff',
    border: 'none',
    color: '#fff',
    borderRadius: '2px',
    fontSize: '14px',
    fontWeight: 500,
    cursor: 'pointer',
  },
  dropdown: {
    position: 'absolute',
    top: '100%',
    right: 0,
    marginTop: '8px',
    backgroundColor: '#282828',
    border: '1px solid #303030',
    borderRadius: '2px',
    minWidth: '150px',
    zIndex: 1001,
  },
  dropdownItem: {
    width: '100%',
    padding: '12px 16px',
    backgroundColor: 'transparent',
    border: 'none',
    color: '#fff',
    textAlign: 'left',
    cursor: 'pointer',
    fontSize: '14px',
    textDecoration: 'none',
    display: 'block',
  },
};