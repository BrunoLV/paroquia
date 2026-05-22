import React from 'react';
import { render, screen, act, fireEvent, within } from '@testing-library/react';
import Dashboard from './Dashboard';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '../api/client';
import { AuthProvider } from '../auth/AuthContext';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('Dashboard Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderDashboard = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') {
        return Promise.resolve({ data: user });
      }
      if (url === '/eventos') {
        return Promise.resolve({ data: { content: [] } });
      }
      if (url === '/organizacoes') {
        return Promise.resolve({ data: [
          { id: 'org-1', nome: 'Pastoral A', ativo: true },
          { id: 'org-2', nome: 'Pastoral B', ativo: true }
        ] });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <Dashboard />
            </ThemeProvider>
          </AuthProvider>
        </MemoryRouter>
      );
    });
  };

  test('renders dashboard layout structure after loading', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderDashboard(adminUser);

    expect(screen.getByRole('heading', { name: /Visão Geral/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Ações Rápidas/i })).toBeInTheDocument();
  });

  test('opens New Event modal with filtered organizations', async () => {
    const memberUser = { 
      id: '2', 
      name: 'Member', 
      roles: ['USER'], 
      organizations: [{ id: 'org-1', nome: 'Pastoral A', ativo: true }] 
    };
    await renderDashboard(memberUser);

    const newEventBtn = screen.getByRole('button', { name: /Novo Evento/i });
    await act(async () => {
      newEventBtn.click();
    });

    // Modal should be open
    expect(screen.getByText(/Criar Novo Evento/i)).toBeInTheDocument();
    
    // Dropdown should contain only Pastoral A
    const select = screen.getByLabelText(/organização responsável/i);
    fireEvent.mouseDown(select);
    
    const listbox = screen.getByRole('listbox');
    expect(within(listbox).getByText('Pastoral A')).toBeInTheDocument();
    expect(within(listbox).queryByText('Pastoral B')).not.toBeInTheDocument();
  });

  test('implicitly filters events for non-ADMIN users', async () => {
    const memberUser = { 
      id: '2', 
      name: 'Member', 
      roles: ['USER'], 
      organizations: [{ id: 'org-1', nome: 'Pastoral A', ativo: true }] 
    };
    await renderDashboard(memberUser);

    // Verify events call included the organization filter
    expect(mockedApiClient.get).toHaveBeenCalledWith(
      '/eventos',
      expect.objectContaining({
        params: expect.objectContaining({
          organizacaoIds: ['org-1']
        })
      })
    );
  });

  test('renders error message on fetch failure', async () => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') return Promise.resolve({ data: { id: '1', roles: ['ADMIN'], organizations: [] } });
      return Promise.reject(new Error('Network Error'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <Dashboard />
            </ThemeProvider>
          </AuthProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByText(/Não foi possível carregar os dados do dashboard/i)).toBeInTheDocument();
  });
});
