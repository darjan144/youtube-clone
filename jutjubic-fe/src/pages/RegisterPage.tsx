import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';

export const RegisterPage = () => {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    street: '',
    city: '',
    country: '',
    postalCode: '',
  });
  
  const [errors, setErrors] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    street: '',
    city: '',
    country: '',
    postalCode: '',
    general: '',
  });
  
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user types
    if (errors[name as keyof typeof errors]) {
      setErrors(prev => ({
        ...prev,
        [name]: '',
        general: ''
      }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors = {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      firstName: '',
      lastName: '',
      street: '',
      city: '',
      country: '',
      postalCode: '',
      general: '',
    };
    
    let isValid = true;

    // Username validation
    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
      isValid = false;
    } else if (formData.username.length < 3 || formData.username.length > 50) {
      newErrors.username = 'Username must be between 3 and 50 characters';
      isValid = false;
    }

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
    } else if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
      isValid = false;
    }

    // Confirm password validation
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
      isValid = false;
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
      isValid = false;
    }

    // First name validation
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
      isValid = false;
    }

    // Last name validation
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
      isValid = false;
    }

    // Street validation
    if (!formData.street.trim()) {
      newErrors.street = 'Street is required';
      isValid = false;
    }

    // City validation
    if (!formData.city.trim()) {
      newErrors.city = 'City is required';
      isValid = false;
    }

    // Country validation
    if (!formData.country.trim()) {
      newErrors.country = 'Country is required';
      isValid = false;
    }

    // Postal code validation
    if (!formData.postalCode.trim()) {
      newErrors.postalCode = 'Postal code is required';
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setErrors({
      username: '', email: '', password: '', confirmPassword: '',
      firstName: '', lastName: '', street: '', city: '', country: '', postalCode: '', general: ''
    });

    try {
      await authService.register(formData);

      // Show success message
      setSuccess(true);
      setLoading(false);

      // Redirect to login after 3 seconds
      setTimeout(() => {
        navigate('/login');
      }, 3000);

    } catch (error: any) {
      setLoading(false);
      
      // Handle specific error messages from backend
      const errorMessage = error.response?.data?.error || error.message || 'Registration failed';
      
      console.log('Registration error:', errorMessage);
      
      if (errorMessage.includes('already exists') || errorMessage.includes('already taken')) {
        setErrors(prev => ({
          ...prev,
          general: 'Email or username already exists'
        }));
      } else {
        setErrors(prev => ({
          ...prev,
          general: errorMessage
        }));
      }
    }
  };

  // Success screen
  if (success) {
    return (
      <div style={styles.container}>
        <div style={styles.successContainer}>
          <div style={styles.successIcon}>✓</div>
          <h1 style={styles.successTitle}>Registration Successful!</h1>
          <p style={styles.successText}>
            Please check your email to activate your account.
          </p>
          <p style={styles.successSubtext}>
            An activation link has been sent to <strong>{formData.email}</strong>
          </p>
          <p style={styles.redirectText}>
            Redirecting to login page...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.formContainer}>
        <h1 style={styles.title}>Create Account</h1>
        <p style={styles.subtitle}>Join Jutjubić today</p>

        <form onSubmit={handleSubmit} style={styles.form} noValidate>
          {/* General Error Message */}
          {errors.general && (
            <div style={styles.errorBox}>
              {errors.general}
            </div>
          )}

          {/* Username & Email */}
          <div style={styles.row}>
            <div style={styles.fieldContainer}>
              <label htmlFor="username" style={styles.label}>
                Username *
              </label>
              <input
                id="username"
                name="username"
                type="text"
                value={formData.username}
                onChange={handleChange}
                style={{
                  ...styles.input,
                  ...(errors.username ? styles.inputError : {})
                }}
                placeholder="Choose a username"
                disabled={loading}
              />
              {errors.username && (
                <span style={styles.errorText}>{errors.username}</span>
              )}
            </div>

            <div style={styles.fieldContainer}>
              <label htmlFor="email" style={styles.label}>
                Email Address *
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
          </div>

          {/* Password & Confirm Password */}
          <div style={styles.row}>
            <div style={styles.fieldContainer}>
              <label htmlFor="password" style={styles.label}>
                Password *
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="new-password"
                value={formData.password}
                onChange={handleChange}
                style={{
                  ...styles.input,
                  ...styles.passwordInput, // Dark red text per spec
                  ...(errors.password ? styles.inputError : {})
                }}
                placeholder="At least 8 characters"
                disabled={loading}
              />
              {errors.password && (
                <span style={styles.errorText}>{errors.password}</span>
              )}
            </div>

            <div style={styles.fieldContainer}>
              <label htmlFor="confirmPassword" style={styles.label}>
                Confirm Password *
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                autoComplete="new-password"
                value={formData.confirmPassword}
                onChange={handleChange}
                style={{
                  ...styles.input,
                  ...styles.passwordInput, // Dark red text per spec
                  ...(errors.confirmPassword ? styles.inputError : {})
                }}
                placeholder="Confirm your password"
                disabled={loading}
              />
              {errors.confirmPassword && (
                <span style={styles.errorText}>{errors.confirmPassword}</span>
              )}
            </div>
          </div>

          {/* First Name & Last Name */}
          <div style={styles.row}>
            <div style={styles.fieldContainer}>
              <label htmlFor="firstName" style={styles.label}>
                First Name *
              </label>
              <input
                id="firstName"
                name="firstName"
                type="text"
                autoComplete="given-name"
                value={formData.firstName}
                onChange={handleChange}
                style={{
                  ...styles.input,
                  ...(errors.firstName ? styles.inputError : {})
                }}
                placeholder="Your first name"
                disabled={loading}
              />
              {errors.firstName && (
                <span style={styles.errorText}>{errors.firstName}</span>
              )}
            </div>

            <div style={styles.fieldContainer}>
              <label htmlFor="lastName" style={styles.label}>
                Last Name *
              </label>
              <input
                id="lastName"
                name="lastName"
                type="text"
                autoComplete="family-name"
                value={formData.lastName}
                onChange={handleChange}
                style={{
                  ...styles.input,
                  ...(errors.lastName ? styles.inputError : {})
                }}
                placeholder="Your last name"
                disabled={loading}
              />
              {errors.lastName && (
                <span style={styles.errorText}>{errors.lastName}</span>
              )}
            </div>
          </div>

          {/* Address Section Title */}
          <div style={styles.sectionTitle}>Address Information</div>

          {/* Street */}
          <div style={styles.fieldContainer}>
            <label htmlFor="street" style={styles.label}>
              Street *
            </label>
            <input
              id="street"
              name="street"
              type="text"
              autoComplete="street-address"
              value={formData.street}
              onChange={handleChange}
              style={{
                ...styles.input,
                ...(errors.street ? styles.inputError : {})
              }}
              placeholder="Street address"
              disabled={loading}
            />
            {errors.street && (
              <span style={styles.errorText}>{errors.street}</span>
            )}
          </div>

          {/* City & Postal Code */}
          <div style={styles.row}>
            <div style={styles.fieldContainer}>
              <label htmlFor="city" style={styles.label}>
                City *
              </label>
              <input
                id="city"
                name="city"
                type="text"
                autoComplete="address-level2"
                value={formData.city}
                onChange={handleChange}
                style={{
                  ...styles.input,
                  ...(errors.city ? styles.inputError : {})
                }}
                placeholder="City"
                disabled={loading}
              />
              {errors.city && (
                <span style={styles.errorText}>{errors.city}</span>
              )}
            </div>

            <div style={styles.fieldContainer}>
              <label htmlFor="postalCode" style={styles.label}>
                Postal Code *
              </label>
              <input
                id="postalCode"
                name="postalCode"
                type="text"
                autoComplete="postal-code"
                value={formData.postalCode}
                onChange={handleChange}
                style={{
                  ...styles.input,
                  ...(errors.postalCode ? styles.inputError : {})
                }}
                placeholder="Postal code"
                disabled={loading}
              />
              {errors.postalCode && (
                <span style={styles.errorText}>{errors.postalCode}</span>
              )}
            </div>
          </div>

          {/* Country */}
          <div style={styles.fieldContainer}>
            <label htmlFor="country" style={styles.label}>
              Country *
            </label>
            <input
              id="country"
              name="country"
              type="text"
              autoComplete="country-name"
              value={formData.country}
              onChange={handleChange}
              style={{
                ...styles.input,
                ...(errors.country ? styles.inputError : {})
              }}
              placeholder="Country"
              disabled={loading}
            />
            {errors.country && (
              <span style={styles.errorText}>{errors.country}</span>
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
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>

          {/* Login Link */}
          <div style={styles.footer}>
            <span style={styles.footerText}>Already have an account? </span>
            <Link to="/login" style={styles.link}>
              Sign in
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
    maxWidth: '600px',
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
  row: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '16px',
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
  sectionTitle: {
    color: '#fff',
    fontSize: '16px',
    fontWeight: 600,
    marginTop: '8px',
    paddingBottom: '8px',
    borderBottom: '1px solid #303030',
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
  // Success screen styles
  successContainer: {
    width: '100%',
    maxWidth: '500px',
    backgroundColor: '#181818',
    padding: '60px 40px',
    borderRadius: '8px',
    border: '1px solid #303030',
    textAlign: 'center',
  },
  successIcon: {
    width: '80px',
    height: '80px',
    backgroundColor: '#0f7c0f',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '48px',
    color: '#fff',
    margin: '0 auto 24px',
  },
  successTitle: {
    color: '#fff',
    fontSize: '28px',
    fontWeight: 600,
    marginBottom: '16px',
  },
  successText: {
    color: '#aaa',
    fontSize: '16px',
    marginBottom: '12px',
    lineHeight: '1.5',
  },
  successSubtext: {
    color: '#999',
    fontSize: '14px',
    marginBottom: '24px',
  },
  redirectText: {
    color: '#666',
    fontSize: '14px',
    fontStyle: 'italic',
  },
};