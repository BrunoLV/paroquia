import { vi } from "vitest";
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryProvider } from '../api/QueryProvider';
import Approvals from './Approvals';
import { getApprovals, decideApproval } from '../api/approvals';
import { getProjectApprovals, decideProjectApproval } from '../api/projects';
import { MemoryRouter } from 'react-router-dom';
import { NotificationProvider } from '../context/NotificationContext';

vi.mock('../api/approvals', () => ({
  getApprovals: jest.fn(),
  decideApproval: jest.fn(),
}));

vi.mock('../api/projects', () => ({
  getProjectApprovals: jest.fn(),
  decideProjectApproval: jest.fn(),
}));

vi.mock('../api/client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(() => Promise.resolve({ data: [] })),
  },
}));

const mockGetApprovals = vi.mocked(getApprovals);
const mockDecideApproval = vi.mocked(decideApproval);
const mockGetProjectApprovals = vi.mocked(getProjectApprovals);
const mockDecideProjectApproval = vi.mocked(decideProjectApproval);

const mockApprovals = [
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

const mockProjectApprovals = [
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

describe('Approvals page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderPage = () => {
    return render(
      <QueryProvider>
        <NotificationProvider>
          <MemoryRouter>
            <Approvals />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );
  };

  test('renders page title and tabs', async () => {
    mockGetApprovals.mockResolvedValueOnce({
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
    mockGetApprovals.mockResolvedValueOnce({
      content: mockApprovals,
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
    mockGetApprovals
      .mockResolvedValueOnce({
        content: mockApprovals,
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
      expect(mockGetApprovals).toHaveBeenLastCalledWith('APROVADA', 0, 10);
    });
  });

  test('opens detail drawer and displays correct details on clicking Visualizar', async () => {
    mockGetApprovals.mockResolvedValueOnce({
      content: mockApprovals,
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
    const viewButtons = screen.getAllByRole('button', { name: /Visualizar/i });
    fireEvent.click(viewButtons[0]);

    // Check that detail drawer is open
    expect(screen.getByText('Detalhes da Solicitação')).toBeInTheDocument();
    expect(screen.getByText('Solicitante:')).toBeInTheDocument();
    expect(screen.getAllByText('user-1')).toHaveLength(2);

    // Check that event details from actionPayloadJson are displayed
    expect(screen.getByText('Título do Evento:')).toBeInTheDocument();
  });

  test('calls decideApproval with APROVADA status and comment on approve', async () => {
    mockGetApprovals.mockResolvedValue({
      content: mockApprovals,
      totalElements: 2,
      totalPages: 1,
      size: 10,
      number: 0,
    });
    mockDecideApproval.mockResolvedValueOnce({ success: true });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    // Open drawer
    const viewButtons = screen.getAllByRole('button', { name: /Visualizar/i });
    fireEvent.click(viewButtons[0]);

    // Type observation
    const obsField = screen.getByLabelText(/Observações/i);
    fireEvent.change(obsField, { target: { value: 'Aprovando com louvor' } });

    // Click Approve
    const approveBtn = screen.getByRole('button', { name: /Aprovar/i });
    fireEvent.click(approveBtn);

    await waitFor(() => {
      expect(mockDecideApproval).toHaveBeenCalledWith('app-1', {
        status: 'APROVADA',
        observacao: 'Aprovando com louvor',
      });
    });
  });

  test('calls decideApproval with REPROVADA status and comment on reject', async () => {
    mockGetApprovals.mockResolvedValue({
      content: mockApprovals,
      totalElements: 2,
      totalPages: 1,
      size: 10,
      number: 0,
    });
    mockDecideApproval.mockResolvedValueOnce({ success: true });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Missa Especial de Pentecostes')).toBeInTheDocument();
    });

    // Open drawer
    const viewButtons = screen.getAllByRole('button', { name: /Visualizar/i });
    fireEvent.click(viewButtons[0]);

    // Type observation
    const obsField = screen.getByLabelText(/Observações/i);
    fireEvent.change(obsField, { target: { value: 'Rejeitando por indisponibilidade' } });

    // Click Reject
    const rejectBtn = screen.getByRole('button', { name: /Rejeitar/i });
    fireEvent.click(rejectBtn);

    await waitFor(() => {
      expect(mockDecideApproval).toHaveBeenCalledWith('app-1', {
        status: 'REPROVADA',
        observacao: 'Rejeitando por indisponibilidade',
      });
    });
  });

  test('lists pending project approvals on tab switch', async () => {
    mockGetApprovals.mockResolvedValueOnce({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
    });
    mockGetProjectApprovals.mockResolvedValueOnce(mockProjectApprovals);

    renderPage();

    // Click projects tab
    fireEvent.click(screen.getByRole('tab', { name: /Solicitações de Projetos/i }));

    await waitFor(() => {
      expect(screen.getByText('Criação de Projeto')).toBeInTheDocument();
      expect(screen.getByText('Evangelização das Famílias')).toBeInTheDocument();
    });
  });

  test('calls decideProjectApproval with true status and comment on approve', async () => {
    mockGetApprovals.mockResolvedValueOnce({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
    });
    mockGetProjectApprovals.mockResolvedValue(mockProjectApprovals);
    mockDecideProjectApproval.mockResolvedValueOnce({ success: true });

    renderPage();

    // Click projects tab
    fireEvent.click(screen.getByRole('tab', { name: /Solicitações de Projetos/i }));

    await waitFor(() => {
      expect(screen.getByText('Evangelização das Famílias')).toBeInTheDocument();
    });

    // Open drawer
    const viewButtons = screen.getAllByRole('button', { name: /Visualizar/i });
    fireEvent.click(viewButtons[0]);

    // Type observation
    const obsField = screen.getByLabelText(/Observações/i);
    fireEvent.change(obsField, { target: { value: 'Aprovando criação de projeto' } });

    // Click Approve
    const approveBtn = screen.getByRole('button', { name: /Aprovar/i });
    fireEvent.click(approveBtn);

    await waitFor(() => {
      expect(mockDecideProjectApproval).toHaveBeenCalledWith('papp-1', true, 'Aprovando criação de projeto');
    });
  });
});
