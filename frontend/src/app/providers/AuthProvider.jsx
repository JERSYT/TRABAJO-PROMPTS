import React, { createContext, useContext, useState, useEffect } from 'react';
import apiClient from '../../services/api/apiClient';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Comprobar la sesión activa en el backend al montar la aplicación
  useEffect(() => {
    const checkActiveSession = async () => {
      try {
        await apiClient.get('/auth/csrf').catch(() => {});
        const response = await apiClient.get('/auth/me');
        setUser(response.data);
      } catch (err) {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    checkActiveSession();
  }, []);

  const login = async (username, password) => {
    await apiClient.get('/auth/csrf').catch(() => {});
    const response = await apiClient.post('/auth/login', { username, password });
    setUser(response.data);
    await apiClient.get('/auth/csrf').catch(() => {});
    return response.data;
  };

  const logout = async () => {
    try {
      await apiClient.post('/auth/logout');
    } finally {
      setUser(null);
      window.location.href = '/login';
    }
  };

  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    return user.roles.includes(role);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasRole, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
