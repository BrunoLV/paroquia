import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import OrganizacaoFormModal from './OrganizacaoFormModal';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { Organizacao } from '../api/types';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { getOrganizacaoMembros } from '../api/organizacoes';

jest.mock('../api/organizacoes', () => ({
  ...jest.requireActual('../api/organizacoes'),
  getOrganizacaoMembros: jest.fn(),
}));

const mockGetOrganizacaoMembros = getOrganizacaoMembros as jest.Mock;

describe('OrganizacaoFormModal', () => {
  const mockOnSave = jest.fn();
  const mockOnClose = jest.fn();

  const mockOrganizacoes: Organizacao[] = [
    { id: 'org-clero', nome: 'Clero Paroquial', tipo: 'CLERO', contato: '', ativo: true },
    { id: 'org-matriz', nome: 'Igreja Matriz', tipo: 'MATRIZ', contato: '', parentId: 'org-clero', ativo: true },
    { id: 'org-comunidade', nome: 'Comunidade São José', tipo: 'COMUNIDADE', contato: '', parentId: 'org-clero', ativo: true },
    { id: 'org-conselho', nome: 'Conselho Pastoral', tipo: 'CONSELHO_PASTORAL', contato: '', parentId: 'org-matriz', ativo: true },
    { id: 'org-pastoral', nome: 'Pastoral da Música', tipo: 'PASTORAL', contato: '', parentId: 'org-conselho', ativo: true },
    { id: 'org-inativo-conselho', nome: 'Conselho Inativo', tipo: 'CONSELHO_PASTORAL', contato: '', parentId: 'org-matriz', ativo: false },
  ];

  const mockPastoraisOficiais = [
    { id: 'po-pastoral-1', nome: 'Catequese Oficial', tipo: 'PASTORAL' as const },
    { id: 'po-pastoral-2', nome: 'Dízimo Oficial', tipo: 'PASTORAL' as const },
    { id: 'po-movimento-1', nome: 'EJC Oficial', tipo: 'MOVIMENTO' as const },
  ];

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onSave: mockOnSave,
    isPending: false,
    organizacoes: mockOrganizacoes,
    usuarios: [],
    igrejas: [],
    pastoraisOficiais: mockPastoraisOficiais,
  };

  const renderModal = (props: {
    open?: boolean;
    onClose?: () => void;
    onSave?: (data: any) => void;
    organizacao?: Organizacao | null;
    isPending?: boolean;
    usuarios?: any[];
    igrejas?: Organizacao[];
    organizacoes?: Organizacao[];
    pastoraisOficiais?: any[];
  } = {}) => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
    const mergedProps = { ...defaultProps, ...props };
    return render(
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <OrganizacaoFormModal {...mergedProps} />
        </ThemeProvider>
      </QueryClientProvider>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockGetOrganizacaoMembros.mockResolvedValue([]);
  });

  test('filters parent options dynamically based on organization type', () => {
    renderModal();

    // 1. Test for MATRIZ: parent must be CLERO
    const tipoSelect = screen.getByLabelText(/^Tipo\s*\*?$/);
    fireEvent.mouseDown(tipoSelect);
    fireEvent.click(screen.getByRole('option', { name: 'Matriz' }));

    const parentSelect = screen.getByLabelText(/Organização Pai/i);
    fireEvent.mouseDown(parentSelect);

    expect(screen.getByRole('option', { name: 'Clero Paroquial' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Conselho Pastoral' })).not.toBeInTheDocument();
  });

  test('displays only active organizations as parent options', () => {
    renderModal();

    // 2. Test for PASTORAL: parent must be CONSELHO (and active)
    const parentSelect = screen.getByLabelText(/Organização Pai/i);
    fireEvent.mouseDown(parentSelect);

    expect(screen.getByRole('option', { name: 'Conselho Pastoral' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Conselho Inativo' })).not.toBeInTheDocument();
  });

  test('submits parentId correctly on save', () => {
    renderModal();

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Nova Pastoral' } });

    // Select official type
    fireEvent.mouseDown(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]);
    fireEvent.click(screen.getByRole('option', { name: 'Catequese Oficial' }));

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
        pastoralOficialId: 'po-pastoral-1',
      })
    );
  });

  test('displays official type dropdown only for PASTORAL or MOVIMENTO and filters options accordingly', () => {
    renderModal();

    // 1. Initially type is PASTORAL. Dropdown should be visible.
    expect(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]).toBeInTheDocument();
    
    // Open dropdown
    fireEvent.mouseDown(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]);
    // Should show PASTORAL types, but NOT MOVIMENTO types
    expect(screen.getByRole('option', { name: 'Catequese Oficial' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Dízimo Oficial' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'EJC Oficial' })).not.toBeInTheDocument();

    // Close options by clicking backdrop or body
    fireEvent.click(document.body);

    // 2. Change type to MOVIMENTO
    const tipoSelect = screen.getByLabelText(/^Tipo\s*\*?$/);
    fireEvent.mouseDown(tipoSelect);
    fireEvent.click(screen.getByRole('option', { name: 'Movimento' }));

    // Dropdown should still be visible
    expect(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]).toBeInTheDocument();
    fireEvent.mouseDown(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]);
    // Should show MOVIMENTO types, but NOT PASTORAL types
    expect(screen.getByRole('option', { name: 'EJC Oficial' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Catequese Oficial' })).not.toBeInTheDocument();

    // Close options
    fireEvent.click(document.body);

    // 3. Change type to CLERO
    fireEvent.mouseDown(tipoSelect);
    fireEvent.click(screen.getByRole('option', { name: 'Clero' }));

    // Dropdown should NOT be visible
    expect(screen.queryAllByLabelText(/Tipo Oficial \/ Movimento/i).length).toBe(0);
  });

  test('requires pastoralOficialId to submit successfully', () => {
    renderModal();

    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Nova Pastoral' } });
    fireEvent.submit(screen.getByRole('button', { name: /Cadastrar/i }));

    // Should show validation error and NOT call onSave
    expect(screen.getByText('A associação oficial é obrigatória')).toBeInTheDocument();
    expect(mockOnSave).not.toHaveBeenCalled();

    // Select official type
    fireEvent.mouseDown(screen.getAllByLabelText(/Tipo Oficial \/ Movimento/i)[0]);
    fireEvent.click(screen.getByRole('option', { name: 'Catequese Oficial' }));

    // Submit again
    fireEvent.submit(screen.getByRole('button', { name: /Cadastrar/i }));

    expect(mockOnSave).toHaveBeenCalledWith(
      expect.objectContaining({
        nome: 'Nova Pastoral',
        tipo: 'PASTORAL',
        pastoralOficialId: 'po-pastoral-1'
      })
    );
  });

  test('renders coordinator select dropdown and calls getOrganizacaoMembros when parent organization resolves to a church', async () => {
    mockGetOrganizacaoMembros.mockResolvedValue([
      { membershipId: 'm-1', usuarioId: 'u-1', usuarioNome: 'Membro Teste', papel: 'OUTRO', ativo: true }
    ]);

    renderModal({
      organizacao: { id: 'org-pastoral', nome: 'Pastoral da Música', tipo: 'PASTORAL', contato: '', parentId: 'org-conselho', ativo: true }
    });

    // Expect Coordenador to be rendered as select/combobox
    const coordSelect = screen.getByRole('combobox', { name: /^Coordenador$/ });
    expect(coordSelect).toBeInTheDocument();

    fireEvent.mouseDown(coordSelect);
    
    await waitFor(() => {
      expect(screen.getByRole('option', { name: 'Membro Teste' })).toBeInTheDocument();
    });
  });
});
