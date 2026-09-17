import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { LocalDeleteConfirmDialog } from './LocalDeleteConfirmDialog';
import { Local } from '@/shared/domain/types/types';

describe('LocalDeleteConfirmDialog', () => {
  const mockLocal: Local = {
    id: 'loc-1',
    nome: 'Salão Comunitário',
    tipo: 'SALAO_PAROQUIAL',
    capacidadeMaxima: 100,
    descricao: null,
    ativo: true,
  };

  it('deve exibir mensagem com nome do local', () => {
    render(
      <LocalDeleteConfirmDialog
        open={true}
        local={mockLocal}
        isPending={false}
        onClose={vi.fn()}
        onConfirm={vi.fn()}
      />
    );

    expect(screen.getByText('Excluir Local')).toBeInTheDocument();
    expect(screen.getByText('Salão Comunitário')).toBeInTheDocument();
    expect(screen.getByText(/eventos futuros agendados não podem ser excluídos/i)).toBeInTheDocument();
  });

  it('deve disparar callbacks ao clicar nos botões', () => {
    const onClose = vi.fn();
    const onConfirm = vi.fn();

    render(
      <LocalDeleteConfirmDialog
        open={true}
        local={mockLocal}
        isPending={false}
        onClose={onClose}
        onConfirm={onConfirm}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /Cancelar/i }));
    expect(onClose).toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: /Excluir/i }));
    expect(onConfirm).toHaveBeenCalled();
  });
});
