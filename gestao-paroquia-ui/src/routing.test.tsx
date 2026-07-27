import { vi } from "vitest";
import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import AppRoutes from './AppRoutes';
import apiClient from './api/client';
import { AuthProvider } from './auth/AuthContext';
import { QueryProvider } from './api/QueryProvider';

vi.mock('./api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

// Mock components to simplify tests
vi.mock('./pages/Calendar', () => () => <div data-testid="calendar-page">Calendar Page</div>);
vi.mock('./pages/Login', () => () => <div data-testid="login-page">Login Page</div>);
vi.mock('./pages/Dashboard', () => () => <div data-testid="dashboard-page">Dashboard Page</div>);
vi.mock('./pages/Approvals', () => () => <div data-testid="approvals-page">Approvals Page</div>);
vi.mock('./pages/Settings', () => () => <div data-testid="settings-page">Settings Page</div>);

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

    // Current behavior (FAILING): redirects to Calendar
    // Desired behavior: show Dashboard
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

    await renderWithProviders('/calendar');

    expect(screen.getByTestId('calendar-page')).toBeInTheDocument();
  });

  test('authenticated user with approval role can access /approvals', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/approvals');

    expect(screen.getByTestId('approvals-page')).toBeInTheDocument();
  });

  test('authenticated user without approval role is redirected to dashboard when trying to access /approvals', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_PASTORAL_MEMBRO'], organizations: [{ id: 'org-1', nome: 'Pastoral', tipo: 'PASTORAL', contato: null, ativo: true }] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/approvals');

    expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    expect(screen.queryByTestId('approvals-page')).not.toBeInTheDocument();
  });

  test('authenticated user with management role can access /configuracoes', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/configuracoes');

    expect(screen.getByTestId('settings-page')).toBeInTheDocument();
  });

  test('authenticated user without management role is redirected to dashboard when trying to access /configuracoes', async () => {
    const mockUser = { id: '1', name: 'Test User', roles: ['ROLE_PASTORAL_MEMBRO'], organizations: [{ id: 'org-1', nome: 'Pastoral', tipo: 'PASTORAL', contato: null, ativo: true }] };
    mockedApiClient.get.mockResolvedValueOnce({ data: mockUser });

    await renderWithProviders('/configuracoes');

    expect(screen.getByTestId('dashboard-page')).toBeInTheDocument();
    expect(screen.queryByTestId('settings-page')).not.toBeInTheDocument();
  });
});

