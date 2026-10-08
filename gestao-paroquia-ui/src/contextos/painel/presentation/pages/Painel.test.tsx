import {vi} from "vitest";
import React from 'react';
import {act, fireEvent, render, screen, within} from '@testing-library/react';
import Painel from './Painel';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import {MemoryRouter} from 'react-router-dom';
import apiClient from '@/shared/infrastructure/http/client';
import {AuthProvider} from '@/contextos/iam/presentation/auth/AuthContext';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({
    showError: jest.fn(),
    showSuccess: jest.fn(),
  }),
}));

describe('Painel Page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderDashboard = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/autenticacao/meu-perfil') {
        return Promise.resolve({ data: user });
      }
      if (url === '/painel/metricas') {
        return Promise.resolve({
          data: {
            resumoGeral: {
              totalEventosPeriodo: 12, projetosAtivos: 3, solicitacoesPendentes: 2,
              totalFieisAtivos: 40, novosFieisMes: 3, fieisEngajadosPastoral: 25, fieisSemVinculoPastoral: 15
            },
            eventosPorOrganizacao: [
              { organizacaoNome: 'Pastoral A', quantidadeEventos: 8 },
              { organizacaoNome: 'Pastoral B', quantidadeEventos: 4 }
            ],
            projetosPorStatus: { planejamento: 1, emExecucao: 2, concluidos: 4, atrasados: 1 },
            projetosAtivosDetalhes: [
              { nome: 'Projeto Alfa', status: 'EM_ANDAMENTO', percentualTempoDecorrido: 65.0, totalEventos: 10, eventosRealizados: 6, percentualExecucao: 60, eventosPendentesAprovacao: 1, emRisco: false, proximoMarco: null, envolvidos: ['Pastoral A'] },
              { nome: 'Projeto Beta', status: 'ABERTO', percentualTempoDecorrido: 85.0, totalEventos: 5, eventosRealizados: 2, percentualExecucao: 40, eventosPendentesAprovacao: 2, emRisco: true, proximoMarco: null, envolvidos: ['Pastoral B'] }
            ],
            eventosOrfaos: [],
            eventosSemana: [
              { titulo: 'Missa das Flores', inicio: '2026-06-15T10:00:00.000', localNome: 'Igreja Matriz', organizacaoNome: 'Pastoral A' }
            ]
          }
        });
      }
      if (url === '/painel/logistica') {
        return Promise.resolve({
          data: {
            choquesHorario: [],
            ocupacaoAmbientes: { taxaDiaPercentual: 10, taxaSemanaPercentual: 20, porLocal: [] },
            eventosPorResponsavel: [{ organizacaoNome: 'Pastoral A', quantidade: 3 }],
            pendenciasHomologacao: [],
          }
        });
      }
      if (url === '/painel/aniversariantes') {
        return Promise.resolve({ data: [{ primeiroNome: 'Maria', dia: 15, mes: 6 }] });
      }
      if (url === '/painel/comunidade') {
        return Promise.resolve({ data: { retencaoPastorais: [] } });
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
                <Painel />
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

    expect(screen.getByRole('heading', { name: /Painel/i })).toBeInTheDocument();
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
      if (url === '/autenticacao/meu-perfil') return Promise.resolve({ data: { id: '1', roles: ['ADMIN'], organizations: [] } });
      return Promise.reject(new Error('Network Error'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryProvider>
            <AuthProvider>
              <ThemeProvider theme={theme}>
                <Painel />
              </ThemeProvider>
            </AuthProvider>
          </QueryProvider>
        </MemoryRouter>
      );
    });

    expect(screen.getByText(/Não foi possível carregar os dados do dashboard/i)).toBeInTheDocument();
  });
});
