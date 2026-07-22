import React from 'react';
import { render, screen, act } from '@testing-library/react';
import Reports from './Reports';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '../api/client';
import { AuthProvider } from '../auth/AuthContext';
import { QueryProvider } from '../api/QueryProvider';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

jest.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: jest.fn(),
    showSuccess: jest.fn(),
  }),
}));

describe('Reports Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderReports = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') {
        return Promise.resolve({ data: user });
      }
      if (url === '/relatorios/resumo') {
        return Promise.resolve({
          data: {
            totalMembros: 150,
            totalLideres: 15,
            totalComunidades: 5,
            volumeAtividadesMes: 42,
            distribuicaoMembrosPorComunidade: [
              { comunidadeNome: 'Matriz', quantidade: 80 },
              { comunidadeNome: 'São José', quantidade: 70 },
            ],
          },
        });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <Reports />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });
  };

  test('renders page title and summary cards after loading', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderReports(adminUser);

    expect(screen.getByRole('heading', { name: /Relatórios & Métricas/i })).toBeInTheDocument();
    expect(screen.getByText(/Membros Leigos/i)).toBeInTheDocument();
    expect(screen.getByText('150')).toBeInTheDocument();
    expect(screen.getByText(/Líderes & Coordenadores/i)).toBeInTheDocument();
    expect(screen.getByText('15')).toBeInTheDocument();
    expect(screen.getByText(/Volume de Atividades/i)).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
  });

  test('renders error state on API failure', async () => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') return Promise.resolve({ data: { id: '1', roles: ['ADMIN'], organizations: [] } });
      return Promise.reject(new Error('Network Error'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <Reports />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByText(/Não foi possível carregar os relatórios/i)).toBeInTheDocument();
  });
});
