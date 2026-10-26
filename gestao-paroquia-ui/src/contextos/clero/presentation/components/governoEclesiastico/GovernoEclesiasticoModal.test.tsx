import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import GovernoEclesiasticoModal from './GovernoEclesiasticoModal';
import { Diocese, GovernoEclesiastico, Clerigo } from '@/shared/domain/types/types';

describe('GovernoEclesiasticoModal', () => {
  const handleClose = vi.fn();
  const handleSave = vi.fn();

  const mockDioceses: Diocese[] = [
    {
      id: 'dio-1',
      nome: 'Diocese de Santo Amaro',
      bispoDiocesano: 'Dom José Negri',
      cnpjMitra: '61.378.766/0001-45',
      cidade: 'São Paulo',
      uf: 'SP',
      ativo: true,
    },
  ];

  const mockClerigos: Clerigo[] = [
    {
      clerigoId: 'cle-1',
      nomeEclesiastico: 'Padre Pedro da Silva',
      // @ts-expect-error fixture parcial: o teste só preenche os campos que exercita
      grauOrdem: 'PRESBITERO',
      funcaoAtual: 'PAROCO',
      ativo: true,
      dataOrdenacao: '2010-12-08',
      dioceseIncardinacao: 'Diocese de Santo Amaro',
      endereco: {
        logradouro: 'Rua da Paróquia',
        numero: '10',
        bairro: 'Centro',
        cidade: 'São Paulo',
        uf: 'SP',
        cep: '04800-000',
      },
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve renderizar o modal com título Configurar governo eclesiástico e as duas seções coesas', () => {
    render(
      <GovernoEclesiasticoModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        dioceses={mockDioceses}
        clerigos={mockClerigos}
        isPending={false}
      />
    );

    expect(screen.getByText('Configurar governo eclesiástico')).toBeInTheDocument();
    expect(screen.getByText('Vinculação Canônica e Provisão do Pároco')).toBeInTheDocument();
    expect(screen.getByText('Registro Histórico e Ereção Canônica')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Cancelar$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Salvar$/i })).toBeInTheDocument();
  });

  it('deve carregar os dados de governo existentes no formulário', () => {
    const mockGoverno: GovernoEclesiastico = {
      id: 'gov-1',
      dioceseId: 'dio-1',
      dioceseNome: 'Diocese de Santo Amaro',
      bispoDiocesano: 'Dom José Negri',
      parocoId: 'cle-1',
      parocoNome: 'Padre Pedro da Silva',
      dataPosseParoco: '2020-02-01',
      decretoErecaoParoquia: 'Decreto 10/1960',
      dataInstalacaoParoquia: '1960-05-13',
      livroTomboNumero: '01/1960',
    };

    render(
      <GovernoEclesiasticoModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        governo={mockGoverno}
        dioceses={mockDioceses}
        clerigos={mockClerigos}
        isPending={false}
      />
    );

    expect(screen.getByLabelText(/Data da Posse do Pároco/i)).toHaveValue('2020-02-01');
    expect(screen.getByLabelText(/Data de Instalação da Paróquia/i)).toHaveValue('1960-05-13');
    expect(screen.getByLabelText(/Decreto Canônico de Ereção/i)).toHaveValue('Decreto 10/1960');
    expect(screen.getByLabelText(/Nº do Livro Tombo \/ Registro/i)).toHaveValue('01/1960');
  });

  it('deve chamar onClose ao clicar no botão Cancelar', () => {
    render(
      <GovernoEclesiasticoModal
        open={true}
        onClose={handleClose}
        onSave={handleSave}
        dioceses={mockDioceses}
        clerigos={mockClerigos}
        isPending={false}
      />
    );

    const cancelBtn = screen.getByRole('button', { name: /^Cancelar$/i });
    fireEvent.click(cancelBtn);

    expect(handleClose).toHaveBeenCalled();
  });
});
