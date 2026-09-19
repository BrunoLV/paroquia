import { vi } from 'vitest';
import React from 'react';
import { act, fireEvent, render, screen } from '@testing-library/react';
import Acesso, { obterDestinoPosAutenticacao } from './Acesso';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '@/contextos/iam/presentation/auth/AuthContext';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import apiClient from '@/shared/infrastructure/http/client';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = jest.fn();
const mockShowSuccess = jest.fn();
vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
    showSuccess: mockShowSuccess,
  }),
  NotificationProvider: ({ children }: any) => <div>{children}</div>,
}));

beforeEach(() => {
  vi.clearAllMocks();
});

test('retoma somente uma rota canônica após autenticação', () => {
  [
    '/painel', '/calendario', '/fieis', '/clerigos', '/notificacoes', '/ajustes-cadastrais',
    '/organograma', '/relatorios', '/conselhos', '/aprovacoes', '/projetos', '/reservas',
    '/ativos', '/locais', '/organizacoes', '/cadastros', '/cadastros/fieis',
    '/cadastros/locais', '/cadastros/organizacoes', '/configuracoes',
  ].forEach((rota) => {
    expect(obterDestinoPosAutenticacao({ from: { pathname: rota } })).toBe(rota);
  });

  ['/login', '/dashboard', '/calendar', '/rota-inexistente', '/reservas/'].forEach((rota) => {
    expect(obterDestinoPosAutenticacao({ from: { pathname: rota } })).toBe('/painel');
  });

  expect(obterDestinoPosAutenticacao()).toBe('/painel');
});

test('renders login form', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <Acesso />
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
            <Acesso />
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });

  fireEvent.change(screen.getByLabelText(/Nome de Usuário/i), { target: { value: 'testuser' } });
  fireEvent.change(screen.getByLabelText(/Senha/i), { target: { value: 'password123' } });

  const submitButton = screen.getByRole('button', { name: /Entrar/i });

  await act(async () => {
    fireEvent.click(submitButton);
  });

  expect(mockedApiClient.post).toHaveBeenCalledWith('/autenticacao/login', {
    username: 'testuser',
    password: 'password123',
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
            <Acesso />
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

test('shows toast validation errors when submitting empty form', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <Acesso />
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });

  const submitButton = screen.getByRole('button', { name: /Entrar/i });

  await act(async () => {
    fireEvent.click(submitButton);
  });

  expect(mockShowError).toHaveBeenCalledWith('O nome de usuário é obrigatório.');
  expect(mockedApiClient.post).not.toHaveBeenCalled();
});

test('renders layout wrapper with vertical centering, full viewport height, and scroll support', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });

  await act(async () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <ThemeProvider theme={theme}>
            <Acesso />
          </ThemeProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  });

  const wrapper = screen.getByTestId('login-layout-wrapper');
  expect(wrapper).toBeInTheDocument();
  expect(wrapper).toHaveStyle({
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
  });
});
