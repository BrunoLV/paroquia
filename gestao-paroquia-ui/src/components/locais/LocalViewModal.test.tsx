import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { LocalViewModal } from './LocalViewModal';
import { Local } from '@/api/types';

describe('LocalViewModal', () => {
  const mockLocal: Local = {
    id: 'loc-1',
    nome: 'Salão Paroquial São José',
    tipo: 'SALAO_PAROQUIAL',
    capacidadeMaxima: 250,
    descricao: 'Equipado com ar condicionado e som',
    ativo: true,
    igrejaNome: 'Matriz N. S. de Fátima',
    logradouro: 'Rua das Flores',
    numero: '123',
    bairro: 'Centro',
    cidade: 'São Paulo',
    uf: 'SP',
    cep: '01001-000',
    enderecoFormatado: 'Rua das Flores, 123 - Centro, São Paulo/SP - CEP: 01001-000',
  };

  it('não deve renderizar quando local for nulo ou open for false', () => {
    const { container } = render(
      <LocalViewModal
        open={false}
        local={null}
        canManage={true}
        onClose={vi.fn()}
        onEdit={vi.fn()}
      />
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('deve renderizar dados completos do local quando aberto', () => {
    render(
      <LocalViewModal
        open={true}
        local={mockLocal}
        canManage={true}
        onClose={vi.fn()}
        onEdit={vi.fn()}
      />
    );

    expect(screen.getByText('Salão Paroquial São José')).toBeInTheDocument();
    expect(screen.getByText('250 pessoas')).toBeInTheDocument();
    expect(screen.getByText('Matriz N. S. de Fátima')).toBeInTheDocument();
    expect(screen.getByText(/Rua das Flores, 123/i)).toBeInTheDocument();
    expect(screen.getByText('CEP: 01001-000')).toBeInTheDocument();
    expect(screen.getByText('Equipado com ar condicionado e som')).toBeInTheDocument();
    expect(screen.getByText('Ativo')).toBeInTheDocument();
  });

  it('deve acionar callbacks de fechar e editar', () => {
    const onClose = vi.fn();
    const onEdit = vi.fn();

    render(
      <LocalViewModal
        open={true}
        local={mockLocal}
        canManage={true}
        onClose={onClose}
        onEdit={onEdit}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /Fechar/i }));
    expect(onClose).toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: /Editar Registro/i }));
    expect(onEdit).toHaveBeenCalledWith(mockLocal);
  });
});
