import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ReservaDetalhesDrawer } from './ReservaDetalhesDrawer';
import { ReservaDetalhe } from '@/shared/domain/types/types';

describe('ReservaDetalhesDrawer', () => {
  const mockReserva: ReservaDetalhe = {
    id: 'res-1',
    tipoItem: 'LOCAL',
    itemId: 'loc-1',
    itemNome: 'Salão Paroquial São José',
    dataHoraInicio: '2026-10-15T06:00:00Z',
    dataHoraFim: '2026-10-15T12:00:00Z',
    bufferMinutos: 30,
    fimComBuffer: '2026-10-15T12:30:00Z',
    descricao: 'Reserva automática (Manhã) vinculada ao evento: Retiro Pastoral',
    solicitanteId: 'user-1',
    solicitanteNome: 'Padre Antônio',
    pastoralMovimentoId: 'past-1',
    pastoralMovimentoNome: 'Pastoral Familiar',
    eventoId: 'ev-1',
    eventoTitulo: 'Retiro Pastoral',
    grupoRecorrenciaId: 'rec-1',
    status: 'CONFIRMADA',
    createdAt: '2026-10-01T10:00:00Z',
    updatedAt: '2026-10-01T10:00:00Z',
  };

  it('não deve renderizar quando reserva for nula', () => {
    const { container } = render(
      <ReservaDetalhesDrawer open={true} reserva={null} onClose={vi.fn()} />
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('deve renderizar dados da reserva, badge do turno e alerta de recorrência', () => {
    render(
      <ReservaDetalhesDrawer open={true} reserva={mockReserva} onClose={vi.fn()} />
    );

    expect(screen.getByText('Detalhes da Reserva')).toBeInTheDocument();
    expect(screen.getByText('Salão Paroquial São José')).toBeInTheDocument();
    expect(screen.getByText('Espaço Físico Paroquial')).toBeInTheDocument();
    expect(screen.getByText('Turno: Manhã')).toBeInTheDocument();
    expect(screen.getByText('Esta reserva faz parte de uma série programada recorrente.')).toBeInTheDocument();
    expect(screen.getByText('Padre Antônio')).toBeInTheDocument();
    expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
    expect(screen.getByText(/30 minutos/)).toBeInTheDocument();
  });

  it('deve acionar onClose ao clicar em fechar', () => {
    const onClose = vi.fn();
    render(
      <ReservaDetalhesDrawer open={true} reserva={mockReserva} onClose={onClose} />
    );

    const closeBtn = screen.getByRole('button', { name: /fechar/i });
    fireEvent.click(closeBtn);
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
