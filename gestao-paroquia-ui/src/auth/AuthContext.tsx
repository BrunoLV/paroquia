import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import apiClient from '../api/client';

interface Organization {
  id: string;
  nome: string;
  tipo: string;
  contato: string | null;
  ativo: boolean;
}

interface User {
  id: string;
  username: string;
  name: string;
  roles: string[];
  organizations: Organization[];
}

interface AuthResponse {
  user: User;
}

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const verifySession = async () => {
      try {
        const response = await apiClient.get<any>('/auth/me');
        if (response.data) {
          const data = response.data;
          const currentUser: User = {
            id: data.id || data.usuarioId || '',
            username: data.username || '',
            name: data.name || data.username || '',
            roles: data.roles || [],
            organizations: data.organizations || []
          };
          setUser(currentUser);
          localStorage.setItem('user', JSON.stringify(currentUser));
        }
      } catch (error) {
        localStorage.removeItem('user');
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    verifySession();
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await apiClient.post<any>('/auth/login', {
        username,
        password,
      });

      const responseData = response.data;
      let newUser: User;

      if (responseData && responseData.user) {
        newUser = {
          ...responseData.user,
          organizations: responseData.user.organizations || []
        };
      } else {
        newUser = {
          id: responseData.usuarioId || responseData.id || '',
          username: responseData.username || '',
          name: responseData.name || responseData.username || '',
          roles: responseData.roles || [],
          organizations: responseData.organizations || []
        };
      }

      localStorage.setItem('user', JSON.stringify(newUser));
      setUser(newUser);
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout failed', error);
    } finally {
      localStorage.removeItem('user');
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
