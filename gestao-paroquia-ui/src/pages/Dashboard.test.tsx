import { vi } from "vitest";
import React from 'react';
import { render, screen, act, fireEvent, within } from '@testing-library/react';
import Dashboard from './Dashboard';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '../api/client';
import { AuthProvider } from '../auth/AuthContext';
import { QueryProvider } from '../api/QueryProvider';

vi.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: jest.fn(),
    showSuccess: jest.fn(),
  }),
}));

// Mock @mui/x-charts components to avoid jsdom rendering errors
vi.mock('@mui/x-charts', () => ({
  PieChart: ({ series }: any) => (
    <div data-testid="mock-pie-chart">
      {series[0]?.data?.map((d: any) => (
        <span key={d.label}>{d.label}: {d.value}</span>
      ))}
    </div>
  ),
}));

describe('Dashboard Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderDashboard = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') {
        return Promise.resolve({ data: user });
      }
      if (url === '/dashboard/metrics') {
        return Promise.resolve({
          data: {
            resumoGeral: { totalEventosMes: 12, projetosAtivos: 3, solicitacoesPendentes: 2 },
            eventosPorOrganizacao: [
              { organizacaoNome: 'Pastoral A', quantidadeEventos: 8 },
              { organizacaoNome: 'Pastoral B', quantidadeEventos: 4 }
            ],
            projetosAtivosDetalhes: [
              { nome: 'Projeto Alfa', status: 'EM_ANDAMENTO', percentualTempoDecorrido: 65.0, totalEventos: 10, eventosRealizados: 6, emRisco: false, envolvidos: ['Pastoral A'] },
              { nome: 'Projeto Beta', status: 'ABERTO', percentualTempoDecorrido: 85.0, totalEventos: 5, eventosRealizados: 2, emRisco: true, envolvidos: ['Pastoral B'] }
            ],
            eventosSemana: [
              { titulo: 'Missa das Flores', inicio: '2026-06-15T10:00:00.000', localNome: 'Igreja Matriz', organizacaoNome: 'Pastoral A' }
            ]
          }
        });
      }
      if (url === '/organizacoes') {
        return Promise.resolve({ data: [
          { id: 'org-1', nome: 'Pastoral A', ativo: true },
          { id: 'org-2', nome: 'Pastoral B', ativo: true }
        ] });
      }
      if (url === '/locais') {
        return Promise.resolve({ data: [] });
      }
      if (url === '/projetos') {
        return Promise.resolve({ data: [] });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <Dashboard />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });
  };

  test('renders dashboard layout structure and metrics after loading', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderDashboard(adminUser);

    expect(screen.getByRole('heading', { name: /Dashboard/i })).toBeInTheDocument();
    expect(screen.getByText('Pastoral A: 8')).toBeInTheDocument();
    expect(screen.getByText('Pastoral B: 4')).toBeInTheDocument();
    expect(screen.getByText('Projeto Alfa')).toBeInTheDocument();
    expect(screen.getByText('Projeto Beta')).toBeInTheDocument();
    expect(screen.getByText('Missa das Flores')).toBeInTheDocument();
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

    expect(screen.getByText(/Criar Novo Evento/i)).toBeInTheDocument();
    
    const select = screen.getByLabelText(/organização responsável/i);
    fireEvent.mouseDown(select);
    
    const listbox = screen.getByRole('listbox');
    expect(within(listbox).getByText('Pastoral A')).toBeInTheDocument();
    expect(within(listbox).queryByText('Pastoral B')).not.toBeInTheDocument();
  });

  test('renders error message on fetch failure', async () => {
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
                <Dashboard />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByText(/Não foi possível carregar os dados do dashboard/i)).toBeInTheDocument();
  });
});
