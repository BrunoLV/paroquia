import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ReservasTable } from './ReservasTable';
import { ReservaDetalhe } from '@/shared/domain/types/types';

describe('ReservasTable', () => {
  const mockReservas: ReservaDetalhe[] = [
    {
      id: 'res-1',
      tipoItem: 'LOCAL',
      itemId: 'loc-1',
      itemNome: 'Auditório Central',
      dataHoraInicio: '2026-10-15T06:00:00Z',
      dataHoraFim: '2026-10-15T12:00:00Z',
      bufferMinutos: 15,
      fimComBuffer: '2026-10-15T12:15:00Z',
      descricao: 'Encontro de Jovens (Manhã)',
      solicitanteId: 'user-1',
      solicitanteNome: 'Maria Santos',
      pastoralMovimentoId: 'past-1',
      pastoralMovimentoNome: 'Pastoral Juvenil',
      eventoId: 'ev-1',
      eventoTitulo: 'Retiro da Juventude',
      status: 'CONFIRMADA',
      createdAt: '2026-10-01T10:00:00Z',
      updatedAt: '2026-10-01T10:00:00Z',
    },
  ];

  it('deve renderizar reservas e exibir badge de turno correspondente ao horário', () => {
    render(
      <ReservasTable
        reservas={mockReservas}
        podeAprovar={true}
        onVisualizar={vi.fn()}
        onAprovar={vi.fn()}
        onRejeitar={vi.fn()}
        onCancelar={vi.fn()}
        onRetirar={vi.fn()}
        onDevolver={vi.fn()}
        onFinalizarUso={vi.fn()}
        page={0}
        rowsPerPage={10}
        onPageChange={vi.fn()}
        onRowsPerPageChange={vi.fn()}
      />
    );

    expect(screen.getByText('Auditório Central')).toBeInTheDocument();
    expect(screen.getByText('Espaço Físico')).toBeInTheDocument();
    expect(screen.getByText('Encontro de Jovens (Manhã)')).toBeInTheDocument();
    expect(screen.getByText('Evento: Retiro da Juventude')).toBeInTheDocument();
    expect(screen.getByText('Maria Santos')).toBeInTheDocument();
    expect(screen.getByText('Pastoral Juvenil')).toBeInTheDocument();
    expect(screen.getByText('Manhã')).toBeInTheDocument();
    expect(screen.getByText(/\+ 15 min margem logística/)).toBeInTheDocument();
  });

  it('deve exibir mensagem de estado vazio quando lista for vazia', () => {
    render(
      <ReservasTable
        reservas={[]}
        podeAprovar={false}
        onVisualizar={vi.fn()}
        onAprovar={vi.fn()}
        onRejeitar={vi.fn()}
        onCancelar={vi.fn()}
        onRetirar={vi.fn()}
        onDevolver={vi.fn()}
        onFinalizarUso={vi.fn()}
        page={0}
        rowsPerPage={10}
        onPageChange={vi.fn()}
        onRowsPerPageChange={vi.fn()}
      />
    );

    expect(screen.getByText('Nenhuma reserva encontrada.')).toBeInTheDocument();
  });
});
