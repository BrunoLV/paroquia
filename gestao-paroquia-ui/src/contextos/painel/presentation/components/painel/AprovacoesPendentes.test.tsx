import React from 'react';
import { vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { MemoryRouter } from 'react-router-dom';
import theme from '@/shared/presentation/theme/theme';
import AprovacoesPendentes from './AprovacoesPendentes';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import * as aprovacoesHttp from '@/contextos/aprovacao/infrastructure/http/aprovacoes';

vi.mock('@/contextos/aprovacao/infrastructure/http/aprovacoes');
const mockedGetAprovacoes = vi.mocked(aprovacoesHttp.getAprovacoes);

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const renderComponent = () =>
  render(
    <MemoryRouter>
      <QueryProvider>
        <ThemeProvider theme={theme}>
          <AprovacoesPendentes />
        </ThemeProvider>
      </QueryProvider>
    </MemoryRouter>
  );

describe('AprovacoesPendentes', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('exibe apenas quantidades sintetizadas por tipo e botão para acessar módulo', async () => {
    mockedGetAprovacoes.mockResolvedValueOnce({
      content: [
        {
          id: '1',
          tipoSolicitacao: 'CRIACAO_EVENTO',
          status: 'PENDENTE',
          criadoEmUtc: '2026-06-01T10:00:00Z',
          actionPayloadJson: JSON.stringify({ titulo: 'Missa das Rosas' }),
        } as any,
        {
          id: '2',
          tipoSolicitacao: 'CRIACAO_EVENTO',
          status: 'PENDENTE',
          criadoEmUtc: '2026-06-01T11:00:00Z',
          actionPayloadJson: JSON.stringify({ titulo: 'Batizado Comunitário' }),
        } as any,
        {
          id: '3',
          tipoSolicitacao: 'CANCELAMENTO',
          status: 'PENDENTE',
          criadoEmUtc: '2026-06-01T12:00:00Z',
          actionPayloadJson: JSON.stringify({ titulo: 'Reunião Pastoral' }),
        } as any,
      ],
      totalElements: 3,
      totalPages: 1,
      size: 100,
      number: 0,
    });

    renderComponent();

    // Rótulos de tipos agregados devem estar visíveis
    const criacaoElements = await screen.findAllByText('Criação de Evento');
    expect(criacaoElements.length).toBeGreaterThanOrEqual(1);

    const cancelamentoElements = screen.getAllByText('Cancelamento de Evento');
    expect(cancelamentoElements.length).toBeGreaterThanOrEqual(1);

    // Contadores correspondentes (2 de criação, 1 de cancelamento)
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();

    // NÃO deve exibir os nomes ou detalhes individuais dos eventos no painel
    expect(screen.queryByText('Missa das Rosas')).not.toBeInTheDocument();
    expect(screen.queryByText('Batizado Comunitário')).not.toBeInTheDocument();
    expect(screen.queryByText('Reunião Pastoral')).not.toBeInTheDocument();

    // Botão de navegação deve estar visível e funcional
    const actionBtn = screen.getByRole('button', { name: /Acessar Módulo de Aprovações/i });
    expect(actionBtn).toBeInTheDocument();
    fireEvent.click(actionBtn);
    expect(mockNavigate).toHaveBeenCalledWith('/aprovacoes');
  });

  it('exibe estado vazio limpo quando não há aprovações pendentes', async () => {
    mockedGetAprovacoes.mockResolvedValueOnce({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 100,
      number: 0,
    });

    renderComponent();

    expect(await screen.findByText('Nenhuma aprovação pendente no momento.')).toBeInTheDocument();
    const verHistoricoBtn = screen.getByRole('button', { name: /Ver Histórico de Aprovações/i });
    expect(verHistoricoBtn).toBeInTheDocument();
    fireEvent.click(verHistoricoBtn);
    expect(mockNavigate).toHaveBeenCalledWith('/aprovacoes');
  });
});
