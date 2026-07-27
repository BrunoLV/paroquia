import { vi } from "vitest";
import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import Login from './Login';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import apiClient from '../api/client';
import { Organizacao } from '../api/types';

vi.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = jest.fn();
const mockShowSuccess = jest.fn();
vi.mock('../context/NotificationContext', () => ({
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

test('shows toast validation errors when submitting empty form', async () => {
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

  const submitButton = screen.getByRole('button', { name: /Entrar/i });
  
  await act(async () => {
    fireEvent.click(submitButton);
  });

  expect(mockShowError).toHaveBeenCalledWith('O nome de usuário é obrigatório.');
  expect(mockedApiClient.post).not.toHaveBeenCalled();
});

test('toggles to signup form and submits registration', async () => {
  const mockOrgs: Organizacao[] = [
    { id: 'matriz-id', nome: 'Igreja Matriz', tipo: 'MATRIZ', contato: null, ativo: true },
    { id: 'comunidade-id', nome: 'Comunidade São José', tipo: 'COMUNIDADE', contato: null, ativo: true }
  ];

  mockedApiClient.get.mockResolvedValueOnce({ data: null }); // for auth check
  mockedApiClient.get.mockResolvedValueOnce({ data: mockOrgs }); // for organizations list
  mockedApiClient.post.mockResolvedValueOnce({ data: 'new-user-uuid' }); // for signup post

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

  // Toggle to register form
  const registerLink = screen.getByRole('button', { name: /Não tem uma conta\? Cadastre-se/i });
  await act(async () => {
    fireEvent.click(registerLink);
  });

  expect(screen.getByLabelText(/Nome Completo/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/E-mail/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/Senha/i, { selector: '#signup-password' })).toBeInTheDocument();
  expect(screen.getByLabelText(/Confirmar Senha/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/Telefone/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/Data de Nascimento/i)).toBeInTheDocument();

  fireEvent.change(screen.getByLabelText(/Nome Completo/i), { target: { value: 'João Teste' } });
  fireEvent.change(screen.getByLabelText(/E-mail/i), { target: { value: 'joao@test.com' } });
  fireEvent.change(screen.getByLabelText(/Senha/i, { selector: '#signup-password' }), { target: { value: 'pass123' } });
  fireEvent.change(screen.getByLabelText(/Confirmar Senha/i), { target: { value: 'pass123' } });
  fireEvent.change(screen.getByLabelText(/Telefone/i), { target: { value: '11999999999' } });
  fireEvent.change(screen.getByLabelText(/Data de Nascimento/i), { target: { value: '1995-12-10' } });

  const registerSubmitButton = screen.getByRole('button', { name: /Cadastrar/i });

  // Try submitting without selecting churchId
  await act(async () => {
    fireEvent.click(registerSubmitButton);
  });

  expect(mockShowError).toHaveBeenCalledWith('A igreja / comunidade é obrigatória.');

  // Select churchId
  const churchSelect = screen.getByTestId('signup-church-select');
  await act(async () => {
    fireEvent.change(churchSelect, { target: { value: 'matriz-id' } });
  });

  // Submit successfully
  await act(async () => {
    fireEvent.click(registerSubmitButton);
  });

  expect(mockedApiClient.post).toHaveBeenCalledWith('/usuarios/registrar', {
    nome: 'João Teste',
    username: 'joao@test.com',
    password: 'pass123',
    telefone: '11999999999',
    dataNascimento: '1995-12-10',
    igrejaId: 'matriz-id',
    batizado: false,
    primeiraComunhao: false,
    crismado: false,
    casadoIgrejaCatolica: false
  });

  expect(mockShowSuccess).toHaveBeenCalledWith(
    'Cadastro solicitado com sucesso! Aguarde a aprovação de um administrador.'
  );
});

test('renders layout wrapper with vertical centering, full viewport height, and scroll support', async () => {
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

  const wrapper = screen.getByTestId('login-layout-wrapper');
  expect(wrapper).toBeInTheDocument();
  // We will expect CSS classes/styles applied to the wrapper element
  expect(wrapper).toHaveStyle({
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
  });
});



