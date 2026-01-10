import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { UserProfilePage } from './pages/UserProfilePage';
import authService from './services/authService';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [username, setUsername] = useState<string | undefined>(undefined);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is authenticated on app load
    const authenticated = authService.isAuthenticated();
    setIsAuthenticated(authenticated);
    
    // TODO: Fetch current user info if authenticated
    if (authenticated) {
      // For now, we'll set a placeholder username
      // Later you'll fetch this from the backend using authService
      setUsername('User');
    }
    
    setLoading(false);
  }, []);

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUsername(undefined);
    window.location.href = '/'; // Redirect to home
  };

  // Show loading while checking authentication
  if (loading) {
    return (
      <div style={styles.loading}>
        <div style={styles.loadingText}>Loading...</div>
      </div>
    );
  }

  return (
    <Router>
      <div style={styles.app}>
        <Navbar 
          isAuthenticated={isAuthenticated}
          username={username}
          onLogout={handleLogout}
        />
        
        <main style={styles.main}>
          <Routes>
            {/* Home Page - Accessible to everyone */}
            <Route path="/" element={<HomePage />} />
            
            {/* User Profile - Accessible to everyone (per spec 3.1) */}
            <Route path="/user/:id" element={<UserProfilePage />} />
            
            {/* Login - Only accessible to unauthenticated users */}
            <Route 
              path="/login" 
              element={
                isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />
              } 
            />
            
            {/* Register - Only accessible to unauthenticated users */}
            <Route 
              path="/register" 
              element={
                isAuthenticated ? <Navigate to="/" replace /> : <RegisterPage />
              } 
            />
            
            {/* Upload - Only accessible to authenticated users */}
            <Route 
              path="/upload" 
              element={
                isAuthenticated ? (
                  <div style={styles.placeholder}>Upload Video Page (Coming Soon)</div>
                ) : (
                  <Navigate to="/login" replace />
                )
              } 
            />
            
            {/* Video Player - Accessible to everyone */}
            <Route 
              path="/video/:id" 
              element={<div style={styles.placeholder}>Video Player Page (Coming Soon)</div>} 
            />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

const styles: { [key: string]: React.CSSProperties } = {
  app: {
    minHeight: '100vh',
    backgroundColor: '#0f0f0f',
  },
  main: {
    minHeight: 'calc(100vh - 60px)',
  },
  loading: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#0f0f0f',
  },
  loadingText: {
    color: '#fff',
    fontSize: '18px',
  },
  placeholder: {
    color: '#fff',
    textAlign: 'center',
    padding: '100px 24px',
    fontSize: '24px',
  },
};

export default App;