import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import type { ReservaDetalhe } from '@/shared/domain/types/types';
import { ReservasTable } from './ReservasTable';

/** A padronização visual não deve ampliar permissões nem alterar transições de reserva. */
describe('menu de ações das reservas', () => {
  it.each([
    ['PENDENTE', 'LOCAL', true, ['Ver detalhes', 'Aprovar Reserva', 'Rejeitar Solicitação', 'Cancelar Reserva']],
    ['PENDENTE', 'LOCAL', false, ['Ver detalhes', 'Cancelar Reserva']],
    ['CONFIRMADA', 'ATIVO', false, ['Ver detalhes', 'Registrar Saída (Retirada)', 'Cancelar Reserva']],
    ['CONFIRMADA', 'LOCAL', false, ['Ver detalhes', 'Cancelar Reserva']],
    ['RETIRADO', 'ATIVO', false, ['Ver detalhes', 'Registrar Devolução']],
  ] as const)('%s / %s / aprovação %s', async (status, tipoItem, podeAprovar, labels) => {
    const reserva: ReservaDetalhe = {
      id: 'reserva-teste', tipoItem, itemId: 'item-teste', itemNome: 'Recurso de teste',
      dataHoraInicio: '2026-10-01T12:00:00Z', dataHoraFim: '2026-10-01T13:00:00Z',
      fimComBuffer: '2026-10-01T13:00:00Z', bufferMinutos: 0, descricao: 'Teste',
      solicitanteId: 'solicitante-teste', solicitanteNome: 'Pessoa de teste',
      pastoralMovimentoId: 'pastoral-teste', pastoralMovimentoNome: 'Grupo de teste', status,
      createdAt: '2026-10-01T12:00:00Z', updatedAt: '2026-10-01T12:00:00Z',
    };
    const onVisualizar = vi.fn();
    render(<ReservasTable reservas={[reserva]} podeAprovar={podeAprovar} onVisualizar={onVisualizar}
      onAprovar={vi.fn()} onRejeitar={vi.fn()} onCancelar={vi.fn()} onRetirar={vi.fn()} onDevolver={vi.fn()}
      page={0} rowsPerPage={10} onPageChange={vi.fn()} onRowsPerPageChange={vi.fn()} />);
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: /ações/i }));
    expect(screen.getAllByRole('menuitem').map(item => item.textContent)).toEqual(labels);
    await user.click(screen.getByRole('menuitem', { name: 'Ver detalhes' }));
    expect(onVisualizar).toHaveBeenCalledTimes(1);
    expect(onVisualizar).toHaveBeenCalledWith(reserva);
  });
});
