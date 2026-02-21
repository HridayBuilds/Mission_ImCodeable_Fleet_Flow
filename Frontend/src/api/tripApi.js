import api from './axios';

export const createTrip = (data) => api.post('/trips', data);

export const dispatchTrip = (id) => api.patch(`/trips/${id}/dispatch`);

export const markInTransit = (id) => api.patch(`/trips/${id}/in-transit`);

export const completeTrip = (id, data) => api.patch(`/trips/${id}/complete`, data);

export const cancelTrip = (id, data) => api.patch(`/trips/${id}/cancel`, data);

export const getTrip = (id) => api.get(`/trips/${id}`);

export const getTrips = (params = {}) => api.get('/trips', { params });
