import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import OrganizacaoFormModal from './OrganizacaoFormModal';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { Organizacao } from '../api/types';

describe('OrganizacaoFormModal', () => {
  const mockOnSave = jest.fn();
  const mockOnClose = jest.fn();

  const mockOrganizacoes: Organizacao[] = [
    { id: 'org-clero', nome: 'Clero Paroquial', tipo: 'CLERO', contato: '', ativo: true },
    { id: 'org-matriz', nome: 'Igreja Matriz', tipo: 'MATRIZ', contato: '', parentId: 'org-clero', ativo: true },
    { id: 'org-comunidade', nome: 'Comunidade São José', tipo: 'COMUNIDADE', contato: '', parentId: 'org-clero', ativo: true },
    { id: 'org-conselho', nome: 'Conselho Pastoral', tipo: 'CONSELHO', contato: '', parentId: 'org-matriz', ativo: true },
    { id: 'org-pastoral', nome: 'Pastoral da Música', tipo: 'PASTORAL', contato: '', parentId: 'org-conselho', ativo: true },
    { id: 'org-inativo-conselho', nome: 'Conselho Inativo', tipo: 'CONSELHO', contato: '', parentId: 'org-matriz', ativo: false },
  ];

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onSave: mockOnSave,
    isPending: false,
    organizacoes: mockOrganizacoes,
    usuarios: [],
    igrejas: [],
    membros: [],
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('filters parent options dynamically based on organization type', () => {
    render(
      <ThemeProvider theme={theme}>
        <OrganizacaoFormModal {...defaultProps} />
      </ThemeProvider>
    );

    // 1. Test for MATRIZ: parent must be CLERO
    const tipoSelect = screen.getByLabelText(/Tipo/i);
    fireEvent.mouseDown(tipoSelect);
    fireEvent.click(screen.getByRole('option', { name: 'Matriz' }));

    const parentSelect = screen.getByLabelText(/Organização Pai/i);
    fireEvent.mouseDown(parentSelect);

    expect(screen.getByRole('option', { name: 'Clero Paroquial' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Conselho Pastoral' })).not.toBeInTheDocument();
  });

  test('displays only active organizations as parent options', () => {
    render(
      <ThemeProvider theme={theme}>
        <OrganizacaoFormModal {...defaultProps} />
      </ThemeProvider>
    );

    // 2. Test for PASTORAL: parent must be CONSELHO (and active)
    const parentSelect = screen.getByLabelText(/Organização Pai/i);
    fireEvent.mouseDown(parentSelect);

    expect(screen.getByRole('option', { name: 'Conselho Pastoral' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Conselho Inativo' })).not.toBeInTheDocument();
  });

  test('submits parentId correctly on save', () => {
    render(
      <ThemeProvider theme={theme}>
        <OrganizacaoFormModal {...defaultProps} />
      </ThemeProvider>
    );

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Nova Pastoral' } });

    // Open parent dropdown and select Conselho Pastoral
    const parentSelect = screen.getByLabelText(/Organização Pai/i);
    fireEvent.mouseDown(parentSelect);
    fireEvent.click(screen.getByRole('option', { name: 'Conselho Pastoral' }));

    fireEvent.submit(screen.getByRole('button', { name: /Cadastrar/i }));

    expect(mockOnSave).toHaveBeenCalledWith(
      expect.objectContaining({
        nome: 'Nova Pastoral',
        tipo: 'PASTORAL',
        parentId: 'org-conselho',
      }),
      expect.any(Array)
    );
  });
});
