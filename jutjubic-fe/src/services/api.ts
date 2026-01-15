import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: 'http://localhost:8084/api',
  // NO DEFAULT HEADERS - Let interceptor handle it
});

// Request interceptor - adds JWT token and handles Content-Type
api.interceptors.request.use(
  (config) => {
    console.log('=== AXIOS REQUEST INTERCEPTOR ===');
    console.log('URL:', config.url);
    console.log('Method:', config.method);
    console.log('BaseURL:', config.baseURL);
    console.log('Full URL:', config.baseURL + config.url);
    console.log('Data type:', typeof config.data);
    console.log('Data is FormData?', config.data instanceof FormData);
    
    // Add JWT token
    const token = localStorage.getItem('token');
    if (token) {
      console.log('Adding Authorization header');
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      console.warn('⚠️ No token found in localStorage!');
    }
    
    // Smart Content-Type handling
    if (config.data instanceof FormData) {
      console.log('✅ FormData detected - removing Content-Type header');
      console.log('FormData entries:');
      for (const [key, value] of config.data.entries()) {
        if (value instanceof File) {
          console.log(`  ${key}: [File] ${value.name} (${value.size} bytes, ${value.type})`);
        } else {
          console.log(`  ${key}:`, value);
        }
      }
      delete config.headers['Content-Type'];
      console.log('Content-Type header deleted - axios will set it with boundary');
    } else if (!config.headers['Content-Type']) {
      console.log('Setting Content-Type to application/json');
      config.headers['Content-Type'] = 'application/json';
    }
    
    console.log('Final headers:', config.headers);
    console.log('Request config ready!');
    
    return config;
  },
  (error) => {
    console.error('❌ Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - handles errors globally
api.interceptors.response.use(
  (response) => {
    console.log('=== AXIOS RESPONSE RECEIVED ===');
    console.log('Status:', response.status);
    console.log('Status text:', response.statusText);
    console.log('Data:', response.data);
    return response;
  },
  (error) => {
    console.error('=== AXIOS RESPONSE ERROR ===');
    console.error('Error:', error);
    console.error('Error message:', error.message);
    console.error('Error response:', error.response);
    console.error('Error response status:', error.response?.status);
    console.error('Error response data:', error.response?.data);
    
    if (error.response?.status === 401) {
      console.warn('⚠️ 401 Unauthorized - removing token and redirecting to login');
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

export default api;