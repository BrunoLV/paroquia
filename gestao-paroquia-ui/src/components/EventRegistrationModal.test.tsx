import React from 'react';
import { render, screen, fireEvent, within } from '@testing-library/react';
import EventRegistrationModal from './EventRegistrationModal';
import { useAuth } from '../auth/AuthContext';

jest.mock('../auth/AuthContext');
const mockedUseAuth = useAuth as jest.Mock;

describe('EventRegistrationModal', () => {
  const mockOnClose = jest.fn();
  const mockOnSubmit = jest.fn();

  const mockOrgs = [
    { id: 'org-1', nome: 'Pastoral A', tipo: 'PASTORAL', contato: null, ativo: true },
    { id: 'org-2', nome: 'Pastoral B', tipo: 'PASTORAL', contato: null, ativo: true },
  ];

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onSubmit: mockOnSubmit,
    isSubmitting: false,
    organizations: mockOrgs,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockedUseAuth.mockReturnValue({
      user: { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] }
    });
  });

  it('renders modal when open is true', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    expect(screen.getByText(/Criar Novo Evento/i)).toBeInTheDocument();
  });

  it('does not render modal when open is false', () => {
    render(<EventRegistrationModal {...defaultProps} open={false} />);
    expect(screen.queryByText(/Criar Novo Evento/i)).not.toBeInTheDocument();
  });

  it('renders all organizations for ADMIN', async () => {
    render(<EventRegistrationModal {...defaultProps} />);
    
    const select = screen.getByLabelText(/organização responsável/i);
    fireEvent.mouseDown(select);
    
    const listbox = screen.getByRole('listbox');
    expect(within(listbox).getByText('Pastoral A')).toBeInTheDocument();
    expect(within(listbox).getByText('Pastoral B')).toBeInTheDocument();
  });

  it('restricts organizations for non-admin members', async () => {
    mockedUseAuth.mockReturnValue({
      user: { 
        id: '2', 
        name: 'Member', 
        roles: ['USER'], 
        organizations: [{ id: 'org-2', nome: 'Pastoral B' }] 
      }
    });

    render(<EventRegistrationModal {...defaultProps} />);
    
    const select = screen.getByLabelText(/organização responsável/i);
    fireEvent.mouseDown(select);
    
    const listbox = screen.getByRole('listbox');
    expect(within(listbox).queryByText('Pastoral A')).not.toBeInTheDocument();
    expect(within(listbox).getByText('Pastoral B')).toBeInTheDocument();
  });

  it('renders with "Editar Evento" title and pre-filled data when in edit mode', () => {
    const eventToEdit = {
      id: 'evt-1',
      titulo: 'Evento Existente',
      descricao: 'Descrição Existente',
      categoria: 'SOCIAL',
      organizacaoResponsavelId: 'org-1',
      projetoId: 'proj-1',
      nomeProjeto: 'Projeto 1',
      localId: 'loc-1',
      inicio: '2026-05-20T10:00:00Z',
      fim: '2026-05-20T12:00:00Z',
      status: 'CONFIRMADO',
      conflictState: null,
      conflictReason: null,
    };
    render(<EventRegistrationModal {...defaultProps} initialEvent={eventToEdit} />);
    
    expect(screen.getByText(/Editar Evento/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/título do evento/i)).toHaveValue('Evento Existente');
    expect(screen.getByLabelText(/descrição/i)).toHaveValue('Descrição Existente');
  });

  it('renders Local, Projeto and Participantes fields', () => {
    const mockLocais = [{ id: 'loc-1', nome: 'Salão', ativo: true, tipo: 'SALAO', endereco: '', capacidade: 100, caracteristicas: '' }];
    const mockProjetos = [{ id: 'proj-1', nome: 'Projeto A', ativo: true, descricao: '', dataInicio: '', dataFim: '', organizacaoResponsavelId: 'org-1' }];
    
    render(<EventRegistrationModal {...defaultProps} locais={mockLocais} projetos={mockProjetos} />);

    expect(screen.getByLabelText(/Local/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Projeto/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Organizações de Apoio/i)).toBeInTheDocument();
  });
});
