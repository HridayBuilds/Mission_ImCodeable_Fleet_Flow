import api from './axios';

export const getMe = () => api.get('/users/me');

export const updateProfile = (data) => api.put('/users/update-profile', data);

export const changePassword = (data) => api.post('/users/change-password', data);
