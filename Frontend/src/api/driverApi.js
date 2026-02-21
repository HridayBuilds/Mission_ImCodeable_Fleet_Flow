import api from './axios';

export const createDriver = (data) => api.post('/drivers', data);

export const updateDriver = (id, data) => api.put(`/drivers/${id}`, data);

export const updateDriverStatus = (id, status) =>
  api.patch(`/drivers/${id}/status`, null, { params: { status } });

export const fileComplaint = (id) => api.post(`/drivers/${id}/complaint`);

export const deleteDriver = (id) => api.delete(`/drivers/${id}`);

export const getDriver = (id) => api.get(`/drivers/${id}`);

export const getDrivers = (params = {}) => api.get('/drivers', { params });

export const getAvailableDrivers = () => api.get('/drivers/available');

export const getExpiredLicenseDrivers = () => api.get('/drivers/expired-license');
