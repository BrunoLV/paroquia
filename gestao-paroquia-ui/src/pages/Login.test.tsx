import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import Login from './Login';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import apiClient from '../api/client';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = jest.fn();
const mockShowSuccess = jest.fn();
jest.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
    showSuccess: mockShowSuccess,
  }),
  NotificationProvider: ({ children }: any) => <div>{children}</div>,
}));


test('renders login form', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <Login />
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });

  expect(screen.getByLabelText(/Nome de Usuário/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/Senha/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Entrar/i })).toBeInTheDocument();
});

test('submits login form', async () => {
  const mockUser = { id: '1', name: 'Test User', username: 'testuser', roles: ['ADMIN'] };
  mockedApiClient.get.mockResolvedValueOnce({ data: null });
  mockedApiClient.post.mockResolvedValueOnce({
    data: { user: mockUser },
  });

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <Login />
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });

  fireEvent.change(screen.getByLabelText(/Nome de Usuário/i), { target: { value: 'testuser' } });
  fireEvent.change(screen.getByLabelText(/Senha/i), { target: { value: 'password' } });

  const submitButton = screen.getByRole('button', { name: /Entrar/i });
  
  await act(async () => {
    fireEvent.click(submitButton);
  });

  expect(mockedApiClient.post).toHaveBeenCalledWith('/auth/login', {
    username: 'testuser',
    password: 'password',
  });
});

test('shows error toast on login failure', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });
  mockedApiClient.post.mockRejectedValueOnce(new Error('Invalid credentials'));

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <Login />
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });

  fireEvent.change(screen.getByLabelText(/Nome de Usuário/i), { target: { value: 'wronguser' } });
  fireEvent.change(screen.getByLabelText(/Senha/i), { target: { value: 'wrongpassword' } });

  const submitButton = screen.getByRole('button', { name: /Entrar/i });
  
  await act(async () => {
    fireEvent.click(submitButton);
  });

  expect(mockShowError).toHaveBeenCalledWith('Falha ao entrar. Verifique suas credenciais.');
});

