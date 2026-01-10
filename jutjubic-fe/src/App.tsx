import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { HomePage } from './pages/HomePage';
import authService from './services/authService';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [username, setUsername] = useState<string | undefined>(undefined);

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
  }, []);

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUsername(undefined);
    window.location.href = '/'; // Redirect to home
  };

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
            <Route path="/" element={<HomePage />} />
            {/* Placeholder routes - we'll add these pages later */}
            <Route path="/login" element={<div style={styles.placeholder}>Login Page (Coming Soon)</div>} />
            <Route path="/register" element={<div style={styles.placeholder}>Register Page (Coming Soon)</div>} />
            <Route path="/upload" element={<div style={styles.placeholder}>Upload Video Page (Coming Soon)</div>} />
            <Route path="/video/:id" element={<div style={styles.placeholder}>Video Player Page (Coming Soon)</div>} />
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
  placeholder: {
    color: '#fff',
    textAlign: 'center',
    padding: '100px 24px',
    fontSize: '24px',
  },
};

export default App;