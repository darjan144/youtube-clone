import axios from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: 'http://localhost:8084/api',
});

// Request interceptor - adds JWT token and handles Content-Type
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    console.log('=== AXIOS REQUEST INTERCEPTOR ===');
    console.log('URL:', config.url);
    console.log('Method:', config.method);
    console.log('BaseURL:', config.baseURL);
    console.log('Full URL:', (config.baseURL || '') + (config.url || ''));
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
    
    // CRITICAL: Content-Type handling for FormData
    if (config.data instanceof FormData) {
      console.log('✅ FormData detected - DELETING Content-Type to let browser set it with boundary');
      
      // EXPLICITLY delete Content-Type so browser sets it correctly
      delete config.headers['Content-Type'];
      
      // Also remove from common headers if it exists
      if (config.headers.common) {
        delete config.headers.common['Content-Type'];
      }
      if (config.headers.post) {
        delete config.headers.post['Content-Type'];
      }
      
      console.log('Content-Type header deleted - browser will set multipart/form-data with boundary');
      console.log('FormData entries:');
      for (const [key, value] of config.data.entries()) {
        if (value instanceof File) {
          console.log(`  ${key}: [File] ${value.name} (${value.size} bytes, ${value.type})`);
        } else {
          console.log(`  ${key}:`, value);
        }
      }
    } else {
      // For non-FormData, set JSON as default
      if (!config.headers['Content-Type']) {
        console.log('Setting Content-Type to application/json');
        config.headers['Content-Type'] = 'application/json';
      }
    }
    
    console.log('Final headers:', JSON.stringify(config.headers, null, 2));
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
    
    // Don't auto-logout on 401 - let component handle it
    if (error.response?.status === 401) {
      console.warn('⚠️ 401 Unauthorized - authentication may have failed');
      console.warn('Token present:', !!localStorage.getItem('token'));
      console.warn('Letting the component handle this error...');
    }
    
    return Promise.reject(error);
  }
);

export default api;