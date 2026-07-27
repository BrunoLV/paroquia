import React from 'react';
import {act, fireEvent, render, screen, waitFor} from '@testing-library/react';
import Reports from './Reports';
import {ThemeProvider} from '@mui/material/styles';
import theme from '../theme';
import {MemoryRouter} from 'react-router-dom';
import apiClient from '../api/client';
import {AuthProvider} from '../auth/AuthContext';
import {QueryProvider} from '../api/QueryProvider';
import * as exportUtils from '../utils/exportUtils';
import {vi} from 'vitest';

vi.mock('../api/client');
const mockedApiClient = vi.mocked(apiClient);

vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: vi.fn(),
    showSuccess: vi.fn(),
  }),
  NotificationProvider: ({ children }: any) => children,
}));

// Mock @mui/x-charts to avoid jsdom SVG rendering limitations
vi.mock('@mui/x-charts', () => ({
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

vi.spyOn(exportUtils, 'downloadCSV').mockImplementation(() => {});
vi.spyOn(exportUtils, 'exportElementAsPDF').mockResolvedValue(true);

describe('Reports Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderReports = async (user: any = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] }) => {
    mockedApiClient.get.mockImplementation((url: string) => {
      if (url === '/auth/me') {
        return Promise.resolve({ data: user });
      }
      if (url === '/dashboard/metrics') {
        return Promise.resolve({
          data: {
            resumoGeral: { totalEventosMes: 15, projetosAtivos: 4, solicitacoesPendentes: 3 },
            eventosPorOrganizacao: [
              { organizacaoNome: 'Pastoral Familiar', quantidadeEventos: 8 },
              { organizacaoNome: 'Catequese', quantidadeEventos: 7 },
            ],
            projetosAtivosDetalhes: [
              { id: '1', nome: 'Festa Padroeiro', responsavel: 'Pe. João', status: 'EM_ANDAMENTO', percentualTempoDecorrido: 60, totalEventos: 5, eventosRealizados: 3, emRisco: false },
            ],
          },
        });
      }
      return Promise.reject(new Error('Unknown endpoint'));
    });

    let renderResult: any;
    await act(async () => {
      renderResult = render(
        <ThemeProvider theme={theme}>
          <QueryProvider>
            <MemoryRouter>
              <AuthProvider>
                <Reports />
              </AuthProvider>
            </MemoryRouter>
          </QueryProvider>
        </ThemeProvider>
      );
    });
    return renderResult;
  };

  test('renders page title, filter controls, export buttons, and metric cards after loading', async () => {
    await renderReports();

    expect(screen.getByText(/Relatórios & Métricas/i)).toBeInTheDocument();
    expect(screen.getByText('Exportar CSV')).toBeInTheDocument();
    expect(screen.getByText('Exportar PDF')).toBeInTheDocument();
    expect(screen.getByLabelText('Data Início')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
      expect(screen.getByText('4')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();
    });

    expect(screen.getByText('Distribuição de Eventos por Organização')).toBeInTheDocument();
    expect(screen.getByText('Volume de Eventos por Organização')).toBeInTheDocument();
    expect(screen.getByText('Acompanhamento de Projetos Ativos & Status')).toBeInTheDocument();
    expect(screen.getByText('Festa Padroeiro')).toBeInTheDocument();
  });

  test('triggers CSV download when Export CSV button is clicked', async () => {
    await renderReports();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
    });

    const csvButton = screen.getByText('Exportar CSV');
    fireEvent.click(csvButton);

    expect(exportUtils.downloadCSV).toHaveBeenCalled();
  });

  test('triggers PDF export when Export PDF button is clicked', async () => {
    await renderReports();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
    });

    const pdfButton = screen.getByText('Exportar PDF');
    fireEvent.click(pdfButton);

    expect(exportUtils.exportElementAsPDF).toHaveBeenCalledWith('reports-container', 'relatorio-gerencial.pdf');
  });

  test('filters dates and fetches events from backend when Filtrar button is clicked', async () => {
    await renderReports();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
    });

    const startDateInput = screen.getByLabelText('Data Início');
    fireEvent.change(startDateInput, { target: { value: '2026-01-01' } });

    const filterButton = screen.getByText('Filtrar');
    fireEvent.click(filterButton);

    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/eventos', expect.any(Object));
    });
  });

  test('renders error state on API failure', async () => {
    mockedApiClient.get.mockImplementation((url: string) => {
      if (url === '/auth/me') return Promise.resolve({ data: { id: '1', name: 'Admin', roles: ['ADMIN'] } });
      return Promise.reject(new Error('Network Error'));
    });

    await act(async () => {
      render(
        <ThemeProvider theme={theme}>
          <QueryProvider>
            <MemoryRouter>
              <AuthProvider>
                <Reports />
              </AuthProvider>
            </MemoryRouter>
          </QueryProvider>
        </ThemeProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByText(/Não foi possível carregar os relatórios/i)).toBeInTheDocument();
    });
  });
});


