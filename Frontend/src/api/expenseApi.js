import api from './axios';

export const createExpense = (data) => api.post('/expenses', data);

export const updateExpense = (id, data) => api.put(`/expenses/${id}`, data);

export const updateExpenseStatus = (id, status) =>
  api.patch(`/expenses/${id}/status`, null, { params: { status } });

export const deleteExpense = (id) => api.delete(`/expenses/${id}`);

export const getExpense = (id) => api.get(`/expenses/${id}`);

export const getExpenses = (params = {}) => api.get('/expenses', { params });
