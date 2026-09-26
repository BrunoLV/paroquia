import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import DioceseFormModal from './DioceseFormModal';
import { Diocese } from '@/shared/domain/types/types';

describe('DioceseFormModal', () => {
  const handleClose = vi.fn();
  const handleSave = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve renderizar o modal em modo de criação com campos vazios e título Nova diocese', () => {
    render(
      <DioceseFormModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        isPending={false}
      />
    );

    expect(screen.getByText('Nova diocese')).toBeInTheDocument();
    expect(screen.getByLabelText(/Nome da Diocese/i)).toHaveValue('');
    expect(screen.getByLabelText(/Bispo Diocesano/i)).toHaveValue('');
    expect(screen.getByRole('button', { name: /^Cancelar$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Salvar$/i })).toBeInTheDocument();
  });

  it('deve renderizar o modal em modo de edição com dados populados e título Editar diocese', () => {
    const mockDiocese: Diocese = {
      id: 'dio-1',
      nome: 'Diocese de Santos',
      bispoDiocesano: 'Dom Tarcísio Scaramussa',
      cnpjMitra: '61.378.766/0001-45',
      cidade: 'Santos',
      uf: 'SP',
      contato: 'curia@diocesedesantos.com.br',
      ativo: true,
    };

    render(
      <DioceseFormModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        diocese={mockDiocese}
        isPending={false}
      />
    );

    expect(screen.getByText('Editar diocese')).toBeInTheDocument();
    expect(screen.getByLabelText(/Nome da Diocese/i)).toHaveValue('Diocese de Santos');
    expect(screen.getByLabelText(/Bispo Diocesano/i)).toHaveValue('Dom Tarcísio Scaramussa');
    expect(screen.getByLabelText(/Cidade Sede/i)).toHaveValue('Santos');
    expect(screen.getByLabelText(/UF/i)).toHaveValue('SP');
  });

  it('deve exibir mensagens de validação ao tentar submeter campos obrigatórios em branco', async () => {
    render(
      <DioceseFormModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        isPending={false}
      />
    );

    const submitBtn = screen.getByRole('button', { name: /^Salvar$/i });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(screen.getByText(/O nome da diocese deve ter no mínimo 3 caracteres/i)).toBeInTheDocument();
      expect(screen.getByText(/Informe o nome do Bispo Diocesano/i)).toBeInTheDocument();
    });

    expect(handleSave).not.toHaveBeenCalled();
  });

  it('deve submeter o formulário corretamente quando dados válidos forem preenchidos', async () => {
    render(
      <DioceseFormModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        isPending={false}
      />
    );

    fireEvent.change(screen.getByLabelText(/Nome da Diocese/i), {
      target: { value: 'Diocese de Santo Amaro' },
    });
    fireEvent.change(screen.getByLabelText(/Bispo Diocesano/i), {
      target: { value: 'Dom José Negri' },
    });
    fireEvent.change(screen.getByLabelText(/Cidade Sede/i), {
      target: { value: 'São Paulo' },
    });
    fireEvent.change(screen.getByLabelText(/UF/i), {
      target: { value: 'sp' },
    });

    const submitBtn = screen.getByRole('button', { name: /^Salvar$/i });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(handleSave).toHaveBeenCalledWith(
        expect.objectContaining({
          nome: 'Diocese de Santo Amaro',
          bispoDiocesano: 'Dom José Negri',
          cidade: 'São Paulo',
          uf: 'SP',
        })
      );
    });
  });

  it('deve chamar onClose ao clicar em Cancelar', () => {
    render(
      <DioceseFormModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        isPending={false}
      />
    );

    const cancelBtn = screen.getByRole('button', { name: /^Cancelar$/i });
    fireEvent.click(cancelBtn);

    expect(handleClose).toHaveBeenCalled();
  });
});
