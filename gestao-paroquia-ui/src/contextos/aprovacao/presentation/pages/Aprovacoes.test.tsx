import {vi} from "vitest";
import React from 'react';
import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';
import Aprovacoes from './Aprovacoes';
import {decideAprovacao, getAprovacoes} from '@/contextos/aprovacao/infrastructure/http/aprovacoes';
import {decidirAprovacaoProjeto, obterAprovacoesProjeto} from '@/contextos/projeto/infrastructure/http/projetos';
import {MemoryRouter} from 'react-router-dom';
import {NotificationProvider} from '@/shared/presentation/context/NotificationContext';

vi.mock('@/contextos/aprovacao/infrastructure/http/aprovacoes', () => ({
  getAprovacoes: jest.fn(),
  decideAprovacao: jest.fn(),
}));

vi.mock('@/contextos/projeto/infrastructure/http/projetos', () => ({
  obterAprovacoesProjeto: jest.fn(),
  decidirAprovacaoProjeto: jest.fn(),
}));

vi.mock('@/contextos/local/infrastructure/http/locais', () => ({
  getLocais: jest.fn(() => Promise.resolve([
    { id: 'loc-1', nome: 'Salão Paroquial', capacidade: 100, ativo: true },
    { id: 'loc-2', nome: 'Igreja Matriz', capacidade: 500, ativo: true },
  ])),
}));

vi.mock('@/contextos/patrimonio/infrastructure/http/ativos', () => ({
  getAtivos: jest.fn(() => Promise.resolve([
    { id: 'at-1', nome: 'Projetor Epson', tombamento: 'PAT-001', estadoConservacao: 'BOM', status: 'DISPONIVEL', organizacaoId: 'org-1' },
    { id: 'at-2', nome: 'Microfone sem Fio', tombamento: 'PAT-002', estadoConservacao: 'BOM', status: 'DISPONIVEL', organizacaoId: 'org-1' },
  ])),
}));

vi.mock('@/shared/infrastructure/http/client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(() => Promise.resolve({ data: [] })),
  },
}));

const mockGetAprovacoes = vi.mocked(getAprovacoes);
const mockDecideAprovacao = vi.mocked(decideAprovacao);
const mockObterAprovacoesProjeto = vi.mocked(obterAprovacoesProjeto);
const mockDecidirAprovacaoProjeto = vi.mocked(decidirAprovacaoProjeto);

const abrirDetalhesDaPrimeiraSolicitacao = () => {
  fireEvent.click(screen.getAllByRole('button', { name: /^Ações$/i })[0]);
  fireEvent.click(screen.getByRole('menuitem', { name: /Visualizar detalhes/i }));
};

const mockAprovacoes = [
  {
    id: 'app-1',
    eventoId: null,
    tipoSolicitacao: 'CRIACAO_EVENTO',
    status: 'PENDENTE',
    aprovadorPapel: 'CONSELHO_COORDENADOR',
    criadoEmUtc: '2026-05-25T12:00:00Z',
    decididoEmUtc: null,
    solicitanteId: 'user-1',
    aprovadorId: null,
    mensagemErroExecucao: null,
    actionPayloadJson: JSON.stringify({
      titulo: 'Missa Especial de Pentecostes',
      inicio: '2026-05-31T10:00:00Z',
      fim: '2026-05-31T11:30:00Z',
    }),
  },
  {
    id: 'app-2',
    eventoId: 'evt-2',
    tipoSolicitacao: 'CANCELAMENTO',
    status: 'PENDENTE',
    aprovadorPapel: 'CONSELHO_COORDENADOR',
    criadoEmUtc: '2026-05-25T13:00:00Z',
    decididoEmUtc: null,
    solicitanteId: 'user-2',
    aprovadorId: null,
    mensagemErroExecucao: null,
    actionPayloadJson: JSON.stringify({
      eventoId: 'evt-2',
      motivo: 'Falta de energia elétrica no local',
    }),
  },
];

const mockAprovacoesProjeto = [
  {
    id: 'papp-1',
    projetoId: null,
    tipoSolicitacao: 'CRIACAO',
    status: 'PENDENTE',
    solicitanteId: 'user-1',
    criadoEmUtc: '2026-05-25T12:00:00Z',
    decididoEmUtc: null,
    actionPayloadJson: JSON.stringify({
      nome: 'Evangelização das Famílias',
      descricao: 'Novo projeto social',
      inicio: '2026-06-01T00:00:00Z',
      fim: '2026-12-31T00:00:00Z',
    }),
    cancelarEventosVinculados: false,
  },
];

describe('Aprovacoes page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderPage = () => {
    return render(
      <QueryProvider>
        <NotificationProvider>
          <MemoryRouter>
            <Aprovacoes />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );
  };

  test('renders page title and tabs', async () => {
    mockGetAprovacoes.mockResolvedValueOnce({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
    });

    renderPage();

    expect(screen.getByText('Solicitações de Aprovação')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Pendentes/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Aprovadas/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Rejeitadas/i })).toBeInTheDocument();
  });

  test('lists pending approvals on load', async () => {
    mockGetAprovacoes.mockResolvedValueOnce({
      content: mockAprovacoes,
      totalElements: 2,
      totalPages: 1,
      size: 10,
      number: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
      expect(screen.getAllByText('Cancelamento de Evento')).toHaveLength(2);
      expect(screen.getByText('user-1')).toBeInTheDocument();
      expect(screen.getByText('user-2')).toBeInTheDocument();
    });
  });

  test('changes status filter when clicking tabs', async () => {
    mockGetAprovacoes
      .mockResolvedValueOnce({
        content: mockAprovacoes,
        totalElements: 2,
        totalPages: 1,
        size: 10,
        number: 0,
      })
      .mockResolvedValueOnce({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 10,
        number: 0,
      });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    // Click approved tab
    fireEvent.click(screen.getByRole('tab', { name: /Aprovadas/i }));

    await waitFor(() => {
      expect(mockGetAprovacoes).toHaveBeenLastCalledWith('APROVADA', 0, 10);
    });
  });

  test('opens detail drawer and displays correct details on clicking Visualizar', async () => {
    mockGetAprovacoes.mockResolvedValueOnce({
      content: mockAprovacoes,
      totalElements: 2,
      totalPages: 1,
      size: 10,
      number: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    // Find and click the 'Visualizar' button for the first row
    abrirDetalhesDaPrimeiraSolicitacao();

    // Check that detail drawer is open
    expect(screen.getByText('Detalhes da Solicitação')).toBeInTheDocument();
    expect(screen.getByText('Solicitante:')).toBeInTheDocument();
    expect(screen.getAllByText('user-1')).toHaveLength(2);

    // Check that event details from actionPayloadJson are displayed
    expect(screen.getByText('Título do Evento:')).toBeInTheDocument();
  });

  test('renders Aprovar and Rejeitar buttons with their semantic icons', async () => {
    mockGetAprovacoes.mockResolvedValueOnce({
      content: mockAprovacoes,
      totalElements: 2,
      totalPages: 1,
      size: 10,
      number: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    abrirDetalhesDaPrimeiraSolicitacao();

    const approveButton = screen.getByRole('button', { name: /Aprovar/i });
    const rejectButton = screen.getByRole('button', { name: /Rejeitar/i });
    expect(approveButton.querySelector('[data-testid="CheckCircleIcon"]')).toBeInTheDocument();
    expect(rejectButton.querySelector('[data-testid="CancelIcon"]')).toBeInTheDocument();
  });

  test('calls decideAprovacao with APROVADA status and comment on approve', async () => {
    mockGetAprovacoes.mockResolvedValue({
      content: mockAprovacoes,
      totalElements: 2,
      totalPages: 1,
      size: 10,
      number: 0,
    });
    mockDecideAprovacao.mockResolvedValueOnce({ success: true });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    // Open drawer
    abrirDetalhesDaPrimeiraSolicitacao();

    // Type observation
    const obsField = screen.getByLabelText(/Observações/i);
    fireEvent.change(obsField, { target: { value: 'Aprovando com louvor' } });

    // Click Approve
    const approveBtn = screen.getByRole('button', { name: /Aprovar/i });
    fireEvent.click(approveBtn);

    await waitFor(() => {
      expect(mockDecideAprovacao).toHaveBeenCalledWith('app-1', {
        status: 'APROVADA',
        observacao: 'Aprovando com louvor',
      });
    });
  });

  test('calls decideAprovacao with REPROVADA status and comment on reject', async () => {
    mockGetAprovacoes.mockResolvedValue({
      content: mockAprovacoes,
      totalElements: 2,
      totalPages: 1,
      size: 10,
      number: 0,
    });
    mockDecideAprovacao.mockResolvedValueOnce({ success: true });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    // Open drawer
    abrirDetalhesDaPrimeiraSolicitacao();

    // Type observation
    const obsField = screen.getByLabelText(/Observações/i);
    fireEvent.change(obsField, { target: { value: 'Rejeitando por indisponibilidade' } });

    // Click Reject
    const rejectBtn = screen.getByRole('button', { name: /Rejeitar/i });
    fireEvent.click(rejectBtn);

    await waitFor(() => {
      expect(mockDecideAprovacao).toHaveBeenCalledWith('app-1', {
        status: 'REPROVADA',
        observacao: 'Rejeitando por indisponibilidade',
      });
    });
  });

  test('lists pending project approvals on tab switch', async () => {
    mockGetAprovacoes.mockResolvedValueOnce({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
    });
    mockObterAprovacoesProjeto.mockResolvedValueOnce(mockAprovacoesProjeto);

    renderPage();

    // Click projects tab
    fireEvent.click(screen.getByRole('tab', { name: /Solicitações de Projetos/i }));

    await waitFor(() => {
      expect(screen.getByTestId('AddCircleOutlineOutlinedIcon')).toBeInTheDocument();
      expect(screen.getByText('Evangelização das Famílias')).toBeInTheDocument();
    });
  });

  test('calls decideAprovacaoProjeto with true status and comment on approve', async () => {
    mockGetAprovacoes.mockResolvedValueOnce({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
    });
    mockObterAprovacoesProjeto.mockResolvedValue(mockAprovacoesProjeto);
    mockDecidirAprovacaoProjeto.mockResolvedValueOnce({ success: true });

    renderPage();

    // Click projects tab
    fireEvent.click(screen.getByRole('tab', { name: /Solicitações de Projetos/i }));

    await waitFor(() => {
      expect(screen.getByText('Evangelização das Famílias')).toBeInTheDocument();
    });

    // Open drawer
    abrirDetalhesDaPrimeiraSolicitacao();

    // Type observation
    const obsField = screen.getByLabelText(/Observações/i);
    fireEvent.change(obsField, { target: { value: 'Aprovando criação de projeto' } });

    // Click Approve
    const approveBtn = screen.getByRole('button', { name: /Aprovar/i });
    fireEvent.click(approveBtn);

    await waitFor(() => {
      expect(mockDecidirAprovacaoProjeto).toHaveBeenCalledWith('papp-1', true, 'Aprovando criação de projeto');
    });
  });

  it('deve desabilitar botão de aprovação quando solicitação contiver conflito de local ou ativo', async () => {
    const mockAprovacaoComConflito = [
      {
        ...mockAprovacoes[0],
        id: 'app-conflito-1',
        conflitosJson: JSON.stringify([
          {
            tipo: 'LOCAL',
            itemNome: 'Salão Paroquial',
            eventoConflitanteTitulo: 'Missa Solene',
            solicitanteNome: 'Padre Pedro',
          },
        ]),
      },
    ];

    mockGetAprovacoes.mockResolvedValue({
      content: mockAprovacaoComConflito,
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    abrirDetalhesDaPrimeiraSolicitacao();

    await waitFor(() => {
      expect(screen.getByText(/CONFLITOS IDENTIFICADOS/i)).toBeInTheDocument();
    });

    const approveBtn = screen.getByRole('button', { name: /Aprovação Bloqueada/i });
    expect(approveBtn).toBeDisabled();
  });

  it('deve exibir o nome do local em vez do ID nos detalhes da solicitação', async () => {
    const mockAprovacaoComLocal = [
      {
        ...mockAprovacoes[0],
        id: 'app-local-1',
        actionPayloadJson: JSON.stringify({
          titulo: 'Missa no Salão Paroquial',
          localIds: ['loc-1', 'loc-2'],
        }),
      },
    ];

    mockGetAprovacoes.mockResolvedValue({
      content: mockAprovacaoComLocal,
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa no Salão Paroquial')).toBeInTheDocument();
    });

    abrirDetalhesDaPrimeiraSolicitacao();

    await waitFor(() => {
      expect(screen.getByText(/Locais Solicitados:/i)).toBeInTheDocument();
      expect(screen.getByText('Salão Paroquial')).toBeInTheDocument();
      expect(screen.getByText('Igreja Matriz')).toBeInTheDocument();
    });
  });

  it('deve exibir o nome dos ativos solicitados em vez do ID nos detalhes da solicitação de evento', async () => {
    const mockAprovacaoComAtivo = [
      {
        ...mockAprovacoes[0],
        id: 'app-ativo-1',
        actionPayloadJson: JSON.stringify({
          titulo: 'Formação com Projetor',
          localId: 'loc-1',
          ativoIds: ['at-1', 'at-2'],
        }),
      },
    ];

    mockGetAprovacoes.mockResolvedValue({
      content: mockAprovacaoComAtivo,
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Formação com Projetor')).toBeInTheDocument();
    });

    abrirDetalhesDaPrimeiraSolicitacao();

    await waitFor(() => {
      expect(screen.getByText(/Ativos Solicitados:/i)).toBeInTheDocument();
      expect(screen.getByText('Projetor Epson')).toBeInTheDocument();
      expect(screen.getByText('Microfone sem Fio')).toBeInTheDocument();
    });
  });

  it('deve resolver o nome do ativo a partir do ativoId em solicitações de empréstimo de ativo', async () => {
    const mockAprovacaoEmprestimo = [
      {
        ...mockAprovacoes[0],
        id: 'app-emp-1',
        tipoSolicitacao: 'ALOCACAO_ATIVO_EMPRESTIMO',
        actionPayloadJson: JSON.stringify({
          ativoId: 'at-1',
          motivo: 'Uso na reunião do conselho',
        }),
      },
    ];

    mockGetAprovacoes.mockResolvedValue({
      content: mockAprovacaoEmprestimo,
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: /^Ações$/i })[0]).toBeInTheDocument();
    });

    abrirDetalhesDaPrimeiraSolicitacao();

    await waitFor(() => {
      expect(screen.getByText(/Solicitação de Empréstimo de Ativo/i)).toBeInTheDocument();
      expect(screen.getByText(/Projetor Epson/i)).toBeInTheDocument();
    });
  });
});
