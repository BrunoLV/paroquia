import React from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import Reports from './Reports';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '@/api/client';
import { AuthProvider } from '@/auth/AuthContext';
import { QueryProvider } from '@/api/QueryProvider';
import * as exportUtils from '@/utils/exportUtils';
import { describe, expect, it, vi, beforeEach } from 'vitest';

vi.mock('../api/client');
const mockedApiClient = vi.mocked(apiClient);

vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: vi.fn(),
    showSuccess: vi.fn(),
  }),
  NotificationProvider: ({ children }: any) => children,
}));

vi.spyOn(exportUtils, 'downloadCSV').mockImplementation(() => {});
vi.spyOn(exportUtils, 'generateStructuredPDF').mockResolvedValue(true);
vi.spyOn(exportUtils, 'generateWordDocument').mockResolvedValue(true);

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
            resumoGeral: {
              totalEventosPeriodo: 15, projetosAtivos: 4, solicitacoesPendentes: 3,
              totalFieisAtivos: 50, novosFieisMes: 2, fieisEngajadosPastoral: 30, fieisSemVinculoPastoral: 20
            },
            eventosPorGrupoParoquial: [
              { grupoParoquialNome: 'Pastoral Familiar', quantidadeEventos: 8 },
              { grupoParoquialNome: 'Catequese', quantidadeEventos: 7 },
            ],
            projetosPorStatus: { planejamento: 2, emExecucao: 4, concluidos: 6, atrasados: 1 },
            projetosAtivosDetalhes: [
              { nome: 'Festa Padroeiro', status: 'EM_ANDAMENTO', percentualTempoDecorrido: 60, totalEventos: 5, eventosRealizados: 3, percentualExecucao: 60, eventosPendentesAprovacao: 0, emRisco: false, proximoMarco: null, envolvidos: [] },
            ],
            eventosOrfaos: [],
            eventosSemana: [],
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

  it('renders page title, filter controls, export buttons, and metric cards after loading', async () => {
    await renderReports();

    expect(screen.getByText(/Relatórios & Métricas/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Exportar CSV/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Exportar PDF/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Exportar Word/i })).toBeInTheDocument();
    expect(screen.getByLabelText('Data Início')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
      expect(screen.getByText('4')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();
    });

    expect(screen.getByText('Distribuição de Eventos')).toBeInTheDocument();
    expect(screen.getByText('Volume de Eventos por Organização')).toBeInTheDocument();
    expect(screen.getByText('Acompanhamento de Projetos Ativos & Status')).toBeInTheDocument();
    expect(screen.getByText('Festa Padroeiro')).toBeInTheDocument();
  });

  it('triggers CSV download when Export CSV button is clicked', async () => {
    await renderReports();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
    });

    const csvButton = screen.getByRole('button', { name: /Exportar CSV/i });
    await act(async () => {
      fireEvent.click(csvButton);
    });

    await waitFor(() => expect(exportUtils.downloadCSV).toHaveBeenCalled());
  });

  it('triggers PDF export when Export PDF button is clicked', async () => {
    await renderReports();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
    });

    const pdfButton = screen.getByRole('button', { name: /Exportar PDF/i });
    await act(async () => {
      fireEvent.click(pdfButton);
    });

    await waitFor(() => expect(exportUtils.generateStructuredPDF).toHaveBeenCalledWith(expect.any(Object), expect.stringMatching(/\.pdf$/)));
  });

  it('triggers Word export when Export Word button is clicked', async () => {
    await renderReports();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
    });

    const wordButton = screen.getByRole('button', { name: /Exportar Word/i });
    await act(async () => {
      fireEvent.click(wordButton);
    });

    await waitFor(() => expect(exportUtils.generateWordDocument).toHaveBeenCalledWith(expect.any(Object), expect.stringMatching(/\.docx$/)));
  });

  it('filters dates and fetches events from backend when Filtrar button is clicked', async () => {
    await renderReports();

    await waitFor(() => {
      expect(screen.getByText('15')).toBeInTheDocument();
    });

    const startDateInput = screen.getByLabelText('Data Início');
    fireEvent.change(startDateInput, { target: { value: '2026-01-01' } });

    const filterButton = screen.getByRole('button', { name: /^Filtrar$/i });
    fireEvent.click(filterButton);

    await waitFor(() => {
      expect(mockedApiClient.get).toHaveBeenCalledWith('/eventos', expect.any(Object));
    });
  });

  it('renders error state on API failure', async () => {
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
