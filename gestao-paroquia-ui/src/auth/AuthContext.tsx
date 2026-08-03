import React, {createContext, ReactNode, useContext, useEffect, useState} from 'react';
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

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

/**
 * Contexto de sessão/autenticação da aplicação. A fonte da verdade da sessão é o backend
 * (cookie de sessão via `apiClient`, `withCredentials: true`); `localStorage['user']` é
 * apenas um cache de exibição para evitar flash de UI deslogada entre navegações — nunca é
 * usado para autorizar chamadas, só para preencher `user` até a verificação assíncrona
 * (`/auth/me`) confirmar ou invalidar a sessão.
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Provedor de autenticação: mantém o usuário autenticado (`user`) e o estado de carregamento
 * inicial (`isLoading`) para toda a árvore de componentes.
 *
 * Ciclo de vida da sessão:
 * - Ao montar, dispara `GET /auth/me` para validar se existe uma sessão de servidor ainda
 *   válida (o cookie de sessão é enviado automaticamente pelo Axios). Se válida, popula
 *   `user` e espelha o resultado em `localStorage['user']` (apenas cache de exibição).
 *   Se a chamada falhar (401/expirada/inexistente), limpa o cache local e mantém `user`
 *   como `null` — não há refresh de token automático aqui; expirar a sessão simplesmente
 *   desloga o usuário na próxima verificação.
 * - `login` chama `POST /auth/login`; o backend estabelece a sessão via cookie e a resposta
 *   traz os dados do usuário (em `response.data.user` ou diretamente em `response.data`,
 *   dependendo do formato retornado), que são gravados em `user` e em `localStorage['user']`.
 * - `logout` chama `POST /auth/logout` para encerrar a sessão no servidor; independente do
 *   resultado da chamada, sempre limpa `localStorage['user']` e zera `user` no `finally`,
 *   garantindo que a UI nunca fique presa em estado "autenticado" caso a chamada falhe.
 */
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

/**
 * Hook de acesso ao contexto de autenticação (`user`, `login`, `logout`, `isLoading`).
 * Lança erro em tempo de execução se usado fora de um `AuthProvider`, para detectar cedo
 * um componente montado fora da árvore protegida em vez de falhar silenciosamente com
 * `user` indefinido.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
