import {vi} from "vitest";
import React from 'react';
import {act, render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import AppRoutes from './AppRoutes';
import apiClient from '@/shared/infrastructure/http/client';
import {AuthProvider} from '@/contextos/iam/presentation/auth/AuthContext';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

// Mock components to simplify tests
vi.mock('@/contextos/calendario/presentation/pages/Calendario', () => ({ default: () => <div data-testid="calendar-page">Calendario Page</div> }));
vi.mock('@/contextos/iam/presentation/pages/Acesso', () => ({ default: () => <div data-testid="login-page">Acesso Page</div> }));
vi.mock('@/contextos/painel/presentation/pages/Painel', () => ({ default: () => <div data-testid="dashboard-page">Painel Page</div> }));
vi.mock('@/contextos/aprovacao/presentation/pages/Aprovacoes', () => ({ default: () => <div data-testid="approvals-page">Approvals Page</div> }));
vi.mock('@/contextos/governanca/presentation/pages/Configuracoes', () => ({ default: () => <div data-testid="settings-page">Configuracoes Page</div> }));

describe('Routing', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderWithProviders = async (initialRoute: string) => {
    return await act(async () => {
      render(
        <QueryProvider>
          <AuthProvider>
            <MemoryRouter initialEntries={[initialRoute]}>
              <AppRoutes />
            </MemoryRouter>
          </AuthProvider>
        </QueryProvider>
      );
    });
  };

  test('root path redirects to dashboard when authenticated', async () => {
    // Mock authenticated session
    const mockUser = { id: '1', name: 'Test User', roles: ['ADMIN'] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/');

    // Current behavior (FAILING): redirects to Calendario
    // Desired behavior: show Painel
    expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    expect(screen.queryByTestId('calendar-page')).not.toBeInTheDocument();
  });

  test('unauthenticated root path redirects to login', async () => {
    // Mock unauthenticated session
    mockedApiClient.get.mockRejectedValueOnce({ response: { status: 401 } });

    await renderWithProviders('/');

    expect(screen.getByTestId('login-page')).toBeInTheDocument();
  });

  test('authenticated user can access calendar at /calendar', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ADMIN'] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/calendario');

    expect(screen.getByTestId('calendar-page')).toBeInTheDocument();
  });

  test('authenticated user is redirected to painel for an unknown route', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ADMIN'] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/rota-inexistente');

    expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
  });

  test('legacy /login path never renders a missing route for an unauthenticated user', async () => {
    mockedApiClient.get.mockRejectedValueOnce({ response: { status: 401 } });

    await renderWithProviders('/login');

    expect(screen.getByTestId('login-page')).toBeInTheDocument();
  });

  test('authenticated user with approval role can access /approvals', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_PAROCO'] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/aprovacoes');

    expect(screen.getByTestId('approvals-page')).toBeInTheDocument();
  });

  test('authenticated user without approval role is redirected to dashboard when trying to access /approvals', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_MEMBRO_ATIVO'], organizations: [{ id: 'org-1', nome: 'Pastoral', tipo: 'PASTORAL', contato: null, ativo: true }] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/aprovacoes');

    expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    expect(screen.queryByTestId('approvals-page')).not.toBeInTheDocument();
  });

  test('authenticated user with management role can access /configuracoes', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_ADMIN'] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/configuracoes');

    expect(screen.getByTestId('settings-page')).toBeInTheDocument();
  });

  test('authenticated user without management role is redirected to dashboard when trying to access /configuracoes', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_MEMBRO_ATIVO'], organizations: [{ id: 'org-1', nome: 'Pastoral', tipo: 'PASTORAL', contato: null, ativo: true }] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/configuracoes');

    expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    expect(screen.queryByTestId('settings-page')).not.toBeInTheDocument();
  });
});
