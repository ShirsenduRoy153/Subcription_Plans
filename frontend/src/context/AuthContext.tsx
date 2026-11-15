import { createContext, useContext, useState, useEffect, type ReactNode, type FC } from 'react';
import { User, Business, AuthResponse } from '../types';

interface AuthContextType {
  user: User | null;
  business: Business | null;
  token: string | null;
  isAuthenticated: boolean;
  loginWithAuthResponse: (data: AuthResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [business, setBusiness] = useState<Business | null>(() => {
    const saved = localStorage.getItem('business');
    return saved ? JSON.parse(saved) : null;
  });

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [token]);

  const loginWithAuthResponse = (data: AuthResponse) => {
    setToken(data.token);
    setUser(data.user);
    setBusiness(data.business || null);
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data.user));
    if (data.business) {
      localStorage.setItem('business', JSON.stringify(data.business));
    } else {
      localStorage.removeItem('business');
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setBusiness(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('business');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        business,
        token,
        isAuthenticated: !!token,
        loginWithAuthResponse,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
