import {vi} from "vitest";
import React from 'react';
import {act, render, screen} from '@testing-library/react';
import {AuthProvider, useAuth} from './AuthContext';
import apiClient from '@/shared/infrastructure/http/client';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const TestComponent = () => {
  const { user, login, logout, isLoading } = useAuth();
  if (isLoading) return <div>Loading...</div>;
  return (
    <div>
      <div data-testid="user">{user ? user.name : 'none'}</div>
      <div data-testid="orgs">{user ? user.organizations.length : 0}</div>
      <button onClick={() => { void login('testuser', 'password').catch(() => undefined); }}>Acesso</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

beforeEach(() => {
  vi.clearAllMocks();
  localStorage.clear();
});

test('verifies session on initialization', async () => {
  const mockUser = { 
    id: '1', 
    name: 'Session User', 
    username: 'sessionuser', 
    roles: ['USER'],
    organizations: [{ id: 'org1', nome: 'Pastoral A', tipo: 'PASTORAL', contato: null, ativo: true }]
  };
  mockedApiClient.get.mockResolvedValueOnce({
    data: mockUser,
  });

  await act(async () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
  });

  expect(mockedApiClient.get).toHaveBeenCalledWith('/autenticacao/meu-perfil');
  expect(screen.getByTestId('user').textContent).toBe('Session User');
  expect(screen.getByTestId('orgs').textContent).toBe('1');
});

test('handles no session on initialization', async () => {
  mockedApiClient.get.mockRejectedValueOnce({
    response: { status: 401 },
  });

  await act(async () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
  });

  expect(mockedApiClient.get).toHaveBeenCalledWith('/autenticacao/meu-perfil');
  expect(screen.getByTestId('user').textContent).toBe('none');
  expect(screen.getByTestId('orgs').textContent).toBe('0');
  expect(localStorage.getItem('user')).toBeNull();
});

test('provides auth state and methods', async () => {
  const mockUser = { 
    id: '1', 
    name: 'Test User', 
    username: 'testuser', 
    roles: ['ADMIN'],
    organizations: []
  };
  
  // Mock initialization call
  mockedApiClient.get.mockResolvedValueOnce({ data: null });
  // Mock login response
  mockedApiClient.post.mockResolvedValueOnce({
    data: { user: mockUser },
  });
  // Mock logout response
  mockedApiClient.post.mockResolvedValueOnce({});

  await act(async () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
  });

  expect(screen.getByTestId('user').textContent).toBe('none');

  const loginButton = screen.getByText('Acesso');
  await act(async () => {
    loginButton.click();
  });

  // Verify login call
  expect(mockedApiClient.post).toHaveBeenCalledWith('/autenticacao/login', {
    username: 'testuser',
    password: 'password',
  });
  expect(screen.getByTestId('user').textContent).toBe('Test User');
  expect(screen.getByTestId('orgs').textContent).toBe('0');

  const logoutButton = screen.getByText('Logout');
  await act(async () => {
    logoutButton.click();
  });

  // Verify logout call
  expect(mockedApiClient.post).toHaveBeenCalledWith('/autenticacao/logout');
  expect(screen.getByTestId('user').textContent).toBe('none');
});

test('normalizes the root login response used by the API session contract', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });
  mockedApiClient.post.mockResolvedValueOnce({
    data: {
      usuarioId: 'usuario-42',
      username: 'coordenadora',
      roles: ['ROLE_CONSELHO_COORDENADOR'],
      organizations: [],
    },
  });

  await act(async () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
  });

  await act(async () => {
    screen.getByText('Acesso').click();
  });

  expect(screen.getByTestId('user')).toHaveTextContent('coordenadora');
  expect(JSON.parse(localStorage.getItem('user') ?? '{}')).toMatchObject({
    id: 'usuario-42',
    username: 'coordenadora',
    roles: ['ROLE_CONSELHO_COORDENADOR'],
  });
});

test('keeps the UI unauthenticated when login is rejected', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });
  mockedApiClient.post.mockRejectedValueOnce(new Error('Credenciais inválidas'));

  await act(async () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
  });

  await act(async () => {
    screen.getByText('Acesso').click();
  });

  expect(screen.getByTestId('user')).toHaveTextContent('none');
  expect(localStorage.getItem('user')).toBeNull();
});

test('clears the local session even when the server logout request fails', async () => {
  mockedApiClient.get.mockResolvedValueOnce({
    data: { id: '1', name: 'Usuário', username: 'usuario', roles: [], organizations: [] },
  });
  mockedApiClient.post.mockRejectedValueOnce(new Error('Servidor indisponível'));

  await act(async () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
  });

  expect(localStorage.getItem('user')).not.toBeNull();

  await act(async () => {
    screen.getByText('Logout').click();
  });

  expect(screen.getByTestId('user')).toHaveTextContent('none');
  expect(localStorage.getItem('user')).toBeNull();
});
