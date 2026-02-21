import api from './axios';

export const signup = (data) => api.post('/auth/signup', data);

export const login = (data) => api.post('/auth/login', data);

export const verifyEmail = (token) => api.get('/auth/verify', { params: { token } });

export const forgotPassword = (data) => api.post('/auth/forgot-password', data);

export const resetPassword = (data) => api.post('/auth/reset-password', data);

export const validateResetToken = (token) =>
  api.get('/auth/validate-reset-token', { params: { token } });

export const healthCheck = () => api.get('/auth/health');
