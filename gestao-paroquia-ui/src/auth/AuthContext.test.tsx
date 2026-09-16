import {vi} from "vitest";
import React from 'react';
import {act, render, screen} from '@testing-library/react';
import {AuthProvider, useAuth} from './AuthContext';
import apiClient from '@/api/client';

vi.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const TestComponent = () => {
  const { user, login, logout, isLoading } = useAuth();
  if (isLoading) return <div>Loading...</div>;
  return (
    <div>
      <div data-testid="user">{user ? user.name : 'none'}</div>
      <div data-testid="orgs">{user ? user.organizations.length : 0}</div>
      <button onClick={() => login('testuser', 'password')}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

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

  expect(mockedApiClient.get).toHaveBeenCalledWith('/auth/me');
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

  expect(mockedApiClient.get).toHaveBeenCalledWith('/auth/me');
  expect(screen.getByTestId('user').textContent).toBe('none');
  expect(screen.getByTestId('orgs').textContent).toBe('0');
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

  const loginButton = screen.getByText('Login');
  await act(async () => {
    loginButton.click();
  });

  // Verify login call
  expect(mockedApiClient.post).toHaveBeenCalledWith('/auth/login', {
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
  expect(mockedApiClient.post).toHaveBeenCalledWith('/auth/logout');
  expect(screen.getByTestId('user').textContent).toBe('none');
});
