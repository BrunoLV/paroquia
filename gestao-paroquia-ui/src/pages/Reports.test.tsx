import React from 'react';
import { render, screen, act, fireEvent, waitFor } from '@testing-library/react';
import Reports from './Reports';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '../api/client';
import { AuthProvider } from '../auth/AuthContext';
import { QueryProvider } from '../api/QueryProvider';
import * as exportUtils from '../utils/exportUtils';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

jest.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: jest.fn(),
    showSuccess: jest.fn(),
  }),
}));

// Mock @mui/x-charts to avoid jsdom SVG rendering limitations
jest.mock('@mui/x-charts', () => ({
  PieChart: ({ series }: any) => (
    <div data-testid="mock-pie-chart">
      {series[0]?.data?.map((d: any) => (
        <span key={d.label}>{d.label}: {d.value}</span>
      ))}
    </div>
  ),
  BarChart: ({ xAxis, series }: any) => (
    <div data-testid="mock-bar-chart">
      <span>BarChart Mock</span>
    </div>
  ),
}));

jest.spyOn(exportUtils, 'downloadCSV').mockImplementation(() => {});
jest.spyOn(exportUtils, 'exportElementAsPDF').mockResolvedValue(true);

describe('Reports Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderReports = async (user: any = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] }) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') {
        return Promise.resolve({ data: user });
      }
      if (url === '/dashboard/metrics') {
        return Promise.resolve({
          data: {
            resumoGeral: { totalEventosMes: 15, projetosAtivos: 4, solicitacoesPendentes: 3 },
            eventosPorOrganizacao: [
              { organizacaoNome: 'Catequese', quantidadeEventos: 10 },
              { organizacaoNome: 'Liturgia', quantidadeEventos: 5 },
            ],
            projetosAtivosDetalhes: [
              {
                nome: 'Reforma da Matriz',
                status: 'EM_ANDAMENTO',
                percentualTempoDecorrido: 50,
                totalEventos: 6,
                eventosRealizados: 3,
                emRisco: false,
                envolvidos: ['Catequese'],
              },
            ],
            eventosSemana: [
              {
                titulo: 'Reunião de Pais',
                inicio: '2026-07-28T19:00:00Z',
                localNome: 'Salão Paroquial',
                organizacaoNome: 'Catequese',
              },
            ],
          },
        });
      }
      if (url === '/organizacoes') {
        return Promise.resolve({
          data: [
            { id: 'org-1', nome: 'Catequese' },
            { id: 'org-2', nome: 'Liturgia' },
          ],
        });
      }
      if (url === '/eventos') {
        return Promise.resolve({
          data: {
            content: [
              {
                id: 'evt-100',
                titulo: 'Retiro de Casais',
                inicio: '2026-08-15T09:00:00Z',
                localNome: 'Sítio São José',
                organizacaoResponsavelId: 'org-1',
              },
            ],
          },
        });
      }
      return Promise.reject(new Error('Not found'));
    });

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

    // Wait for metrics heading to appear
    await screen.findByRole('heading', { name: /Relatórios & Métricas Gerenciais/i });
  };

  test('renders page title, filter controls, export buttons, and metric cards after loading', async () => {
    await renderReports();

    expect(screen.getByRole('button', { name: /exportar csv/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /exportar pdf/i })).toBeInTheDocument();

    expect(screen.getByText('15')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();

    expect(screen.getByText('Catequese: 10')).toBeInTheDocument();
    expect(screen.getByText('Liturgia: 5')).toBeInTheDocument();
    expect(screen.getByText('Reforma da Matriz')).toBeInTheDocument();
  });

  test('triggers CSV download when Export CSV button is clicked', async () => {
    await renderReports();

    const csvButton = screen.getByRole('button', { name: /exportar csv/i });
    fireEvent.click(csvButton);

    expect(exportUtils.downloadCSV).toHaveBeenCalled();
  });

  test('triggers PDF export when Export PDF button is clicked', async () => {
    await renderReports();

    const pdfButton = screen.getByRole('button', { name: /exportar pdf/i });
    await act(async () => {
      fireEvent.click(pdfButton);
    });

    expect(exportUtils.exportElementAsPDF).toHaveBeenCalledWith('reports-container', 'relatorio-gerencial.pdf');
  });

  test('filters dates and fetches events from backend for the selected range', async () => {
    await renderReports();

    expect(screen.getByText('Reunião de Pais')).toBeInTheDocument();

    const startInput = screen.getByLabelText(/data início/i);
    const endInput = screen.getByLabelText(/data fim/i);

    // Set filter dates for August 2026
    await act(async () => {
      fireEvent.change(startInput, { target: { value: '2026-08-01' } });
      fireEvent.change(endInput, { target: { value: '2026-08-31' } });
    });

    // Wait for API-filtered event to render
    await screen.findByText('Retiro de Casais');
    expect(screen.getByText(/Filtro ativo de/i)).toBeInTheDocument();
  });

  test('renders error state on API failure', async () => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') return Promise.resolve({ data: { id: '1', roles: ['ADMIN'], organizations: [] } });
      return Promise.reject(new Error('Network Error'));
    });

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

    await waitFor(() => {
      expect(screen.getByText(/Não foi possível carregar os relatórios/i)).toBeInTheDocument();
    });
  });
});
