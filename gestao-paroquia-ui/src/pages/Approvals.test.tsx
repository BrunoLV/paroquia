import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryProvider } from '../api/QueryProvider';
import Approvals from './Approvals';
import { getApprovals } from '../api/approvals';
import { MemoryRouter } from 'react-router-dom';

jest.mock('../api/approvals', () => ({
  getApprovals: jest.fn(),
  decideApproval: jest.fn(),
}));

const mockGetApprovals = getApprovals as jest.Mock;

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

describe('Approvals page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderPage = () => {
    return render(
      <QueryProvider>
        <MemoryRouter>
          <Approvals />
        </MemoryRouter>
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
});
