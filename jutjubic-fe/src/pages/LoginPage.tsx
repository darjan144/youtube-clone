import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';

export const LoginPage = () => {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  
  const [errors, setErrors] = useState({
    email: '',
    password: '',
    general: '',
  });
  
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user types
    if (errors.general || errors[name as keyof typeof errors]) {
      setErrors(prev => ({
        ...prev,
        [name]: '',
        general: ''
      }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors = {
      email: '',
      password: '',
      general: '',
    };
    
    let isValid = true;

    // Email validation
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
      isValid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
      isValid = false;
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = 'Password is required';
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Prevent any default form behavior
    e.stopPropagation();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setErrors({ email: '', password: '', general: '' });

    try {
      // Call login service
      const response = await authService.login({
        email: formData.email,
        password: formData.password,
      });

      console.log('Login successful:', response);

      // Only navigate and reload on SUCCESS
      navigate('/');
      window.location.reload();

    } catch (error: any) {
      console.error('Login error:', error);
      
      // IMPORTANT: Set loading to false FIRST
      setLoading(false);
      
      // Handle specific error messages from backend
      const errorMessage = error.response?.data?.error || error.message || 'Login failed';
      
      console.log('Error message:', errorMessage);
      
      // Set error state
      if (errorMessage.includes('not activated') || errorMessage.includes('Account not activated')) {
        setErrors({
          email: '',
          password: '',
          general: 'Account not activated. Please check your email for the activation link.'
        });
      } else if (errorMessage.includes('Invalid') || errorMessage.includes('credentials') || errorMessage.includes('Bad credentials')) {
        setErrors({
          email: '',
          password: '',
          general: 'Invalid email or password'
        });
      } else {
        setErrors({
          email: '',
          password: '',
          general: errorMessage
        });
      }
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.formContainer}>
        <h1 style={styles.title}>Welcome Back</h1>
        <p style={styles.subtitle}>Sign in to your Jutjubić account</p>

        <form onSubmit={handleSubmit} style={styles.form} noValidate>
          {/* General Error Message */}
          {errors.general && (
            <div style={styles.errorBox}>
              {errors.general}
            </div>
          )}

          {/* Email Field */}
          <div style={styles.fieldContainer}>
            <label htmlFor="email" style={styles.label}>
              Email Address
            </label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              value={formData.email}
              onChange={handleChange}
              style={{
                ...styles.input,
                ...(errors.email ? styles.inputError : {})
              }}
              placeholder="Enter your email"
              disabled={loading}
            />
            {errors.email && (
              <span style={styles.errorText}>{errors.email}</span>
            )}
          </div>

          {/* Password Field */}
          <div style={styles.fieldContainer}>
            <label htmlFor="password" style={styles.label}>
              Password
            </label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              value={formData.password}
              onChange={handleChange}
              style={{
                ...styles.input,
                ...styles.passwordInput, // Dark red text per spec
                ...(errors.password ? styles.inputError : {})
              }}
              placeholder="Enter your password"
              disabled={loading}
            />
            {errors.password && (
              <span style={styles.errorText}>{errors.password}</span>
            )}
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            style={{
              ...styles.submitButton,
              ...(loading ? styles.submitButtonDisabled : {})
            }}
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>

          {/* Register Link */}
          <div style={styles.footer}>
            <span style={styles.footerText}>Don't have an account? </span>
            <Link to="/register" style={styles.link}>
              Sign up
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    minHeight: 'calc(100vh - 60px)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '24px',
    backgroundColor: '#0f0f0f',
  },
  formContainer: {
    width: '100%',
    maxWidth: '400px',
    backgroundColor: '#181818',
    padding: '40px',
    borderRadius: '8px',
    border: '1px solid #303030',
  },
  title: {
    color: '#fff',
    fontSize: '28px',
    fontWeight: 600,
    marginBottom: '8px',
    textAlign: 'center',
  },
  subtitle: {
    color: '#aaa',
    fontSize: '14px',
    marginBottom: '32px',
    textAlign: 'center',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  errorBox: {
    backgroundColor: '#3d1a1a',
    border: '1px solid #ff4444',
    color: '#ff4444',
    padding: '12px 16px',
    borderRadius: '4px',
    fontSize: '14px',
    textAlign: 'center',
  },
  fieldContainer: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  label: {
    color: '#fff',
    fontSize: '14px',
    fontWeight: 500,
  },
  input: {
    width: '100%',
    padding: '12px 16px',
    backgroundColor: '#121212',
    border: '1px solid #303030',
    borderRadius: '4px',
    color: '#fff',
    fontSize: '16px',
    outline: 'none',
    transition: 'border-color 0.2s',
  },
  passwordInput: {
    color: '#8B0000', // Dark red text for password (per spec section 3.2)
  },
  inputError: {
    borderColor: '#ff4444',
  },
  errorText: {
    color: '#ff4444',
    fontSize: '12px',
    marginTop: '4px',
  },
  submitButton: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#ff0000',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    fontSize: '16px',
    fontWeight: 600,
    cursor: 'pointer',
    transition: 'background-color 0.2s',
    marginTop: '8px',
  },
  submitButtonDisabled: {
    backgroundColor: '#666',
    cursor: 'not-allowed',
  },
  footer: {
    textAlign: 'center',
    marginTop: '8px',
  },
  footerText: {
    color: '#aaa',
    fontSize: '14px',
  },
  link: {
    color: '#3ea6ff',
    textDecoration: 'none',
    fontSize: '14px',
    fontWeight: 500,
  },
};