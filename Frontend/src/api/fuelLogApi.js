import api from './axios';

export const createFuelLog = (data) => api.post('/fuel-logs', data);

export const deleteFuelLog = (id) => api.delete(`/fuel-logs/${id}`);

export const getFuelLog = (id) => api.get(`/fuel-logs/${id}`);

export const getFuelLogs = (params = {}) => api.get('/fuel-logs', { params });
