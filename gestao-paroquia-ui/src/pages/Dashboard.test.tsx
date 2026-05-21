import React from 'react';
import { render, screen, act } from '@testing-library/react';
import Dashboard from './Dashboard';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '../api/client';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('Dashboard Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders dashboard layout structure after loading', async () => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/eventos') {
        return Promise.resolve({ data: { content: [] } });
      }
      if (url === '/organizacoes') {
        return Promise.resolve({ data: [] });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <ThemeProvider theme={theme}>
            <Dashboard />
          </ThemeProvider>
        </MemoryRouter>
      );
    });

    // Verify titles are present after loading spinner disappears
    expect(screen.getByRole('heading', { name: /Visão Geral/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Ações Rápidas/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Próximos Eventos/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Aprovações Pendentes/i })).toBeInTheDocument();
  });

  test('renders error message on fetch failure', async () => {
    mockedApiClient.get.mockRejectedValue(new Error('Network Error'));

    await act(async () => {
      render(
        <MemoryRouter>
          <ThemeProvider theme={theme}>
            <Dashboard />
          </ThemeProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByText(/Não foi possível carregar os dados do dashboard/i)).toBeInTheDocument();
  });
});
