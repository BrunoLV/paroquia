import {vi} from "vitest";
import React from 'react';
import {act, render, waitFor} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import {AuthProvider} from '@/contextos/iam/presentation/auth/AuthContext';
import apiClient from '@/shared/infrastructure/http/client';
import Painel from '@/contextos/painel/presentation/pages/Painel';
import Calendario from '@/contextos/calendario/presentation/pages/Calendario';
import {NotificationProvider} from '@/shared/presentation/context/NotificationContext';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('Data Fetching Requirements', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: { id: '1', roles: ['ADMIN'], organizations: [] } });
      if (url === '/eventos') return Promise.resolve({ data: { content: [] } });
      if (url === '/organizacoes') return Promise.resolve({ data: [] });
      if (url === '/unidades-territoriais') return Promise.resolve({ data: [] });
      if (url === '/locais') return Promise.resolve({ data: [] });
      if (url === '/projetos') return Promise.resolve({ data: [] });
      if (url === '/solenidades-liturgicas/ativas') return Promise.resolve({ data: [] });
      return Promise.reject(new Error('Not found'));
    });
  });

  const renderComponent = async (Component: React.ComponentType) => {
    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <NotificationProvider>
                  <Component />
                </NotificationProvider>
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });
  };

  test('Painel should fetch locais and projetos on mount', async () => {
    await renderComponent(Painel);

    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/locais');
    });
    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/projetos');
    });
  });

  test('Calendario should fetch locais and projetos on mount', async () => {
    await renderComponent(Calendario);

    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/locais');
    });
    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/projetos');
    });
  });
});
