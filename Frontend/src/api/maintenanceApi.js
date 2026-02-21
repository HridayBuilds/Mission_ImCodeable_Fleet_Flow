import api from './axios';

export const createMaintenanceLog = (data) => api.post('/maintenance', data);

export const updateMaintenanceLog = (id, data) => api.put(`/maintenance/${id}`, data);

export const updateMaintenanceStatus = (id, data) =>
  api.patch(`/maintenance/${id}/status`, data);

export const deleteMaintenanceLog = (id) => api.delete(`/maintenance/${id}`);

export const getMaintenanceLog = (id) => api.get(`/maintenance/${id}`);

export const getMaintenanceLogs = (params = {}) => api.get('/maintenance', { params });
