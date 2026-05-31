import React from 'react';
import { render, act, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { AuthProvider } from '../auth/AuthContext';
import apiClient from '../api/client';
import Dashboard from './Dashboard';
import Calendar from './Calendar';
import { NotificationProvider } from '../context/NotificationContext';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('Data Fetching Requirements', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') return Promise.resolve({ data: { id: '1', roles: ['ADMIN'], organizations: [] } });
      if (url === '/eventos') return Promise.resolve({ data: { content: [] } });
      if (url === '/organizacoes') return Promise.resolve({ data: [] });
      if (url === '/locais') return Promise.resolve({ data: [] });
      if (url === '/projetos') return Promise.resolve({ data: [] });
      return Promise.reject(new Error('Not found'));
    });
  });

  const renderComponent = async (Component: React.ComponentType) => {
    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <NotificationProvider>
                <Component />
              </NotificationProvider>
            </ThemeProvider>
          </AuthProvider>
        </MemoryRouter>
      );
    });
  };

  test('Dashboard should fetch locais and projetos on mount', async () => {
    await renderComponent(Dashboard);

    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/locais');
    });
    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/projetos');
    });
  });

  test('Calendar should fetch locais and projetos on mount', async () => {
    await renderComponent(Calendar);

    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/locais');
    });
    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/projetos');
    });
  });
});
