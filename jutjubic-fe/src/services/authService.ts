// src/services/authService.ts

import api from './api';
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../types/User';

export const authService = {
  // Login user
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post('/auth/login', credentials);
    
    // Save token to localStorage
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    
    return response.data;
  },

  // Register new user
  register: async (userData: RegisterRequest): Promise<RegisterResponse> => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  // Logout user
  logout: () => {
    localStorage.removeItem('token');
  },

  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  },

  // Get token
  getToken: (): string | null => {
    return localStorage.getItem('token');
  },
};

export default authService;