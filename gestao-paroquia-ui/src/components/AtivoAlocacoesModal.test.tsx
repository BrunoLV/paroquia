import { vi } from 'vitest';
import React from 'react';
import { act, fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import AtivoAlocacoesModal from './AtivoAlocacoesModal';
import { getAlocacoesPorAtivo, cancelarAlocacao } from '../api/ativos';

vi.mock('../api/ativos');
const mockedGetAlocacoes = getAlocacoesPorAtivo as jest.Mock;
const mockedCancelar = cancelarAlocacao as jest.Mock;

const mockShowError = vi.fn();
const mockShowSuccess = vi.fn();
vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
    showSuccess: mockShowSuccess,
  }),
}));

const renderModal = (props: Partial<React.ComponentProps<typeof AtivoAlocacoesModal>> = {}) => {
  return render(
    <ThemeProvider theme={theme}>
      <AtivoAlocacoesModal
        ativoId="ativo-1"
        ativoNome="Projetor Epson"
        onClose={vi.fn()}
        {...props}
      />
    </ThemeProvider>
  );
};

describe('AtivoAlocacoesModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows empty state when there are no allocations', async () => {
    mockedGetAlocacoes.mockResolvedValue([]);

    await act(async () => {
      renderModal();
    });

    expect(screen.getByText(/Nenhuma alocação ou manutenção registrada/i)).toBeInTheDocument();
  });

  it('renders the Fechar button with a CloseIcon', async () => {
    mockedGetAlocacoes.mockResolvedValue([]);

    await act(async () => {
      renderModal();
    });

    const closeButton = screen.getByRole('button', { name: /Fechar/i });
    expect(closeButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
  });

  it('lists allocations with type, period and status', async () => {
    mockedGetAlocacoes.mockResolvedValue([
      {
        id: 'aloc-1',
        ativoId: 'ativo-1',
        ativoNome: 'Projetor Epson',
        numeroTombamento: 'PAT-2026-0001',
        tipo: 'ALOCACAO_EVENTO',
        eventoId: 'evento-1',
        organizacaoOrigemId: 'org-1',
        organizacaoDestinoId: 'org-1',
        emprestimo: false,
        dataInicio: '2026-09-01',
        dataFim: '2026-09-03',
        status: 'CONFIRMADA',
        impactadaPorAtraso: false,
        criadoEmUtc: '2026-08-01T00:00:00Z',
        devolvidoEmUtc: null,
      },
    ]);

    await act(async () => {
      renderModal();
    });

    expect(screen.getByTestId('EventIcon')).toBeInTheDocument();
    expect(screen.getByText('2026-09-01 — 2026-09-03')).toBeInTheDocument();
    expect(screen.getByTestId('CheckCircleIcon')).toBeInTheDocument();
  });

  it('shows the impact warning chip when impactadaPorAtraso is true', async () => {
    mockedGetAlocacoes.mockResolvedValue([
      {
        id: 'aloc-1',
        ativoId: 'ativo-1',
        ativoNome: 'Projetor Epson',
        numeroTombamento: 'PAT-2026-0001',
        tipo: 'ALOCACAO_EVENTO',
        eventoId: 'evento-1',
        organizacaoOrigemId: 'org-1',
        organizacaoDestinoId: 'org-1',
        emprestimo: false,
        dataInicio: '2026-09-10',
        dataFim: '2026-09-12',
        status: 'CONFIRMADA',
        impactadaPorAtraso: true,
        criadoEmUtc: '2026-08-01T00:00:00Z',
        devolvidoEmUtc: null,
      },
    ]);

    await act(async () => {
      renderModal();
    });

    expect(screen.getByText(/Pode atrasar/i)).toBeInTheDocument();
  });

  it('cancels a future reservation with a valid motivo', async () => {
    mockedGetAlocacoes.mockResolvedValue([
      {
        id: 'aloc-1',
        ativoId: 'ativo-1',
        ativoNome: 'Projetor Epson',
        numeroTombamento: 'PAT-2026-0001',
        tipo: 'ALOCACAO_EVENTO',
        eventoId: 'evento-1',
        organizacaoOrigemId: 'org-1',
        organizacaoDestinoId: 'org-1',
        emprestimo: false,
        dataInicio: '2099-09-01',
        dataFim: '2099-09-03',
        status: 'CONFIRMADA',
        impactadaPorAtraso: false,
        criadoEmUtc: '2026-08-01T00:00:00Z',
        devolvidoEmUtc: null,
      },
    ]);
    mockedCancelar.mockResolvedValue(undefined);

    await act(async () => {
      renderModal();
    });

    await act(async () => {
      screen.getByRole('button', { name: /Cancelar Reserva/i }).click();
    });

    fireEvent.change(screen.getByLabelText(/Motivo do Cancelamento/i), {
      target: { value: 'Evento foi remarcado para outra data' },
    });

    await act(async () => {
      screen.getByRole('button', { name: /Confirmar Cancelamento/i }).click();
    });

    expect(mockedCancelar).toHaveBeenCalledWith('aloc-1', {
      motivoCancelamento: 'Evento foi remarcado para outra data',
    });
    expect(mockShowSuccess).toHaveBeenCalled();
  });

  it('does not offer cancel action for allocations already in progress', async () => {
    mockedGetAlocacoes.mockResolvedValue([
      {
        id: 'aloc-1',
        ativoId: 'ativo-1',
        ativoNome: 'Projetor Epson',
        numeroTombamento: 'PAT-2026-0001',
        tipo: 'ALOCACAO_EVENTO',
        eventoId: 'evento-1',
        organizacaoOrigemId: 'org-1',
        organizacaoDestinoId: 'org-1',
        emprestimo: false,
        dataInicio: '2020-01-01',
        dataFim: '2020-01-03',
        status: 'DEVOLVIDA',
        impactadaPorAtraso: false,
        criadoEmUtc: '2020-01-01T00:00:00Z',
        devolvidoEmUtc: '2020-01-03T00:00:00Z',
      },
    ]);

    await act(async () => {
      renderModal();
    });

    expect(screen.queryByRole('button', { name: /Cancelar Reserva/i })).not.toBeInTheDocument();
  });
});
