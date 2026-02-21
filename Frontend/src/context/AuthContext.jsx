import { createContext, useState, useEffect, useCallback } from 'react';
import { login as loginApi, signup as signupApi } from '../api/authApi';
import { getMe } from '../api/userApi';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('ff_user');
    return stored ? JSON.parse(stored) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('ff_token'));
  const [loading, setLoading] = useState(true);

  // On mount, verify token is still valid
  useEffect(() => {
    const verifyAuth = async () => {
      if (token) {
        try {
          const res = await getMe();
          const userData = res.data;
          setUser(userData);
          localStorage.setItem('ff_user', JSON.stringify(userData));
        } catch {
          // Token invalid, clear
          localStorage.removeItem('ff_token');
          localStorage.removeItem('ff_user');
          setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    };
    verifyAuth();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const login = useCallback(async (credentials) => {
    const res = await loginApi(credentials);
    const data = res.data;
    const userData = {
      id: data.userId,
      email: data.email,
      name: data.name,
      employeeId: data.employeeId,
      role: data.role,
      isVerified: data.isVerified,
    };
    localStorage.setItem('ff_token', data.accessToken);
    localStorage.setItem('ff_user', JSON.stringify(userData));
    setToken(data.accessToken);
    setUser(userData);
    return data;
  }, []);

  const signupUser = useCallback(async (data) => {
    const res = await signupApi(data);
    return res.data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('ff_token');
    localStorage.removeItem('ff_user');
    setToken(null);
    setUser(null);
  }, []);

  const updateUser = useCallback((updatedData) => {
    setUser((prev) => {
      const merged = { ...prev, ...updatedData };
      localStorage.setItem('ff_user', JSON.stringify(merged));
      return merged;
    });
  }, []);

  const value = {
    user,
    token,
    loading,
    isAuthenticated: !!token && !!user,
    login,
    signupUser,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
