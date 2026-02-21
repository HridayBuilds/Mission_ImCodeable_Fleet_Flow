import api from './axios';

export const getFinancialSummaries = (params = {}) =>
  api.get('/analytics/financial-summaries', { params });

export const getVehicleCosts = () => api.get('/analytics/vehicle-costs');

export const getTopCostliestVehicles = (limit = 5) =>
  api.get('/analytics/top-costliest-vehicles', { params: { limit } });

export const getFleetSummary = () => api.get('/analytics/fleet-summary');

export const generateSummary = (year, month) =>
  api.post('/analytics/generate-summary', null, { params: { year, month } });

export const generateCurrentSummary = () =>
  api.post('/analytics/generate-current-summary');
