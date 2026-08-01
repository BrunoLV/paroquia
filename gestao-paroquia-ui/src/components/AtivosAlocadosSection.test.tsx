import { vi } from 'vitest';
import React from 'react';
import { act, render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import AtivosAlocadosSection from './AtivosAlocadosSection';
import { getAlocacoesPorEvento } from '../api/ativos';

vi.mock('../api/ativos');
const mockedGetAlocacoesPorEvento = getAlocacoesPorEvento as jest.Mock;

const renderSection = () => render(
  <ThemeProvider theme={theme}>
    <AtivosAlocadosSection eventoId="evento-1" />
  </ThemeProvider>
);

describe('AtivosAlocadosSection', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders nothing when no assets are allocated to the event', async () => {
    mockedGetAlocacoesPorEvento.mockResolvedValue([]);

    const { container } = renderSection();
    await act(async () => {
      await Promise.resolve();
    });

    expect(container.textContent).not.toContain('Ativos Alocados');
  });

  it('lists allocated assets with status', async () => {
    mockedGetAlocacoesPorEvento.mockResolvedValue([
      {
        id: 'aloc-1',
        ativoId: 'ativo-1',
        ativoNome: 'Caixa de Som',
        numeroTombamento: 'PAT-2026-0003',
        tipo: 'ALOCACAO_EVENTO',
        eventoId: 'evento-1',
        organizacaoOrigemId: 'org-1',
        organizacaoDestinoId: 'org-1',
        emprestimo: false,
        dataInicio: '2026-09-01',
        dataFim: '2026-09-02',
        status: 'CONFIRMADA',
        impactadaPorAtraso: false,
        criadoEmUtc: '2026-08-01T00:00:00Z',
        devolvidoEmUtc: null,
      },
    ]);

    await act(async () => {
      renderSection();
    });

    expect(await screen.findByText('Ativos Alocados')).toBeInTheDocument();
    expect(screen.getByText('Caixa de Som')).toBeInTheDocument();
    expect(screen.getByText('Confirmada')).toBeInTheDocument();
  });

  it('shows the impact warning chip when impactadaPorAtraso is true', async () => {
    mockedGetAlocacoesPorEvento.mockResolvedValue([
      {
        id: 'aloc-1',
        ativoId: 'ativo-1',
        ativoNome: 'Caixa de Som',
        numeroTombamento: 'PAT-2026-0003',
        tipo: 'ALOCACAO_EVENTO',
        eventoId: 'evento-1',
        organizacaoOrigemId: 'org-1',
        organizacaoDestinoId: 'org-1',
        emprestimo: false,
        dataInicio: '2026-09-01',
        dataFim: '2026-09-02',
        status: 'CONFIRMADA',
        impactadaPorAtraso: true,
        criadoEmUtc: '2026-08-01T00:00:00Z',
        devolvidoEmUtc: null,
      },
    ]);

    await act(async () => {
      renderSection();
    });

    expect(await screen.findByText(/Pode atrasar/i)).toBeInTheDocument();
  });
});
