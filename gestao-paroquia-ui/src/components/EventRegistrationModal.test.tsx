import React from 'react';
import { render, screen, fireEvent, within } from '@testing-library/react';
import EventRegistrationModal from './EventRegistrationModal';
import { useAuth } from '../auth/AuthContext';

jest.mock('../auth/AuthContext');
const mockedUseAuth = useAuth as jest.Mock;

const mockShowError = jest.fn();
jest.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
  }),
}));

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
    mockShowError.mockClear();
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
    const mockProjetos = [{ id: 'proj-1', nome: 'Projeto A', status: 'ATIVO' as const, descricao: '', inicio: '', fim: '', organizacaoResponsavelId: 'org-1' }];
    
    render(<EventRegistrationModal {...defaultProps} locais={mockLocais} projetos={mockProjetos} />);

    expect(screen.getByLabelText(/Local/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Projeto/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Organizações de Apoio/i)).toBeInTheDocument();
  });

  it('pre-fills participantes in edit mode', () => {
    const eventWithParticipants = {
      id: 'evt-2',
      titulo: 'Evento com Apoio',
      descricao: '',
      categoria: 'PASTORAL',
      organizacaoResponsavelId: 'org-1',
      participantes: ['org-2'],
      inicio: '2026-05-20T10:00:00Z',
      fim: '2026-05-20T12:00:00Z',
      status: 'CONFIRMADO',
      projetoId: null,
      nomeProjeto: null,
      localId: null,
      conflictState: null,
      conflictReason: null,
    };
    
    render(<EventRegistrationModal {...defaultProps} initialEvent={eventWithParticipants} />);
    
    // MUI Select with multiple values renders them as Chips or text in the display area.
    // We check if the name of the participant organization is visible.
    expect(screen.getByText('Pastoral B')).toBeInTheDocument();
  });

  it('shows validation error when title is missing', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    
    const titleInput = screen.getByLabelText(/título do evento/i);
    fireEvent.change(titleInput, { target: { value: '' } });

    const submitButton = screen.getByRole('button', { name: /Criar Evento/i });
    fireEvent.click(submitButton);

    expect(screen.getByText('O título do evento é obrigatório.')).toBeInTheDocument();
    expect(mockShowError).not.toHaveBeenCalled();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('shows validation error when organization is missing', () => {
    render(<EventRegistrationModal {...defaultProps} organizations={[]} />);
    
    const titleInput = screen.getByLabelText(/título do evento/i);
    fireEvent.change(titleInput, { target: { value: 'Missa' } });

    const submitButton = screen.getByRole('button', { name: /Criar Evento/i });
    fireEvent.click(submitButton);

    expect(screen.getByText('A organização responsável é obrigatória.')).toBeInTheDocument();
    expect(mockShowError).not.toHaveBeenCalled();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('shows validation error when start date is missing', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    
    const titleInput = screen.getByLabelText(/título do evento/i);
    fireEvent.change(titleInput, { target: { value: 'Missa' } });

    const startInput = screen.getByLabelText(/data e hora de início/i);
    fireEvent.change(startInput, { target: { value: '' } });

    const submitButton = screen.getByRole('button', { name: /Criar Evento/i });
    fireEvent.click(submitButton);

    expect(screen.getByText('A data e hora de início é obrigatória.')).toBeInTheDocument();
    expect(mockShowError).not.toHaveBeenCalled();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('shows validation error when end date is missing (non-recurring)', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    
    const titleInput = screen.getByLabelText(/título do evento/i);
    fireEvent.change(titleInput, { target: { value: 'Missa' } });

    const endInput = screen.getByLabelText(/data e hora de término/i);
    fireEvent.change(endInput, { target: { value: '' } });

    const submitButton = screen.getByRole('button', { name: /Criar Evento/i });
    fireEvent.click(submitButton);

    expect(screen.getByText('A data e hora de término é obrigatória.')).toBeInTheDocument();
    expect(mockShowError).not.toHaveBeenCalled();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('shows validation error when start date is after end date', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    
    const titleInput = screen.getByLabelText(/título do evento/i);
    fireEvent.change(titleInput, { target: { value: 'Missa' } });

    const startInput = screen.getByLabelText(/data e hora de início/i);
    fireEvent.change(startInput, { target: { value: '2026-05-31T12:00' } });

    const endInput = screen.getByLabelText(/data e hora de término/i);
    fireEvent.change(endInput, { target: { value: '2026-05-31T11:00' } });

    const submitButton = screen.getByRole('button', { name: /Criar Evento/i });
    fireEvent.click(submitButton);

    expect(screen.getByText('A data de início não pode ser posterior à data de término.')).toBeInTheDocument();
    expect(mockShowError).not.toHaveBeenCalled();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  describe('Project Date Validation', () => {
    const mockProjetos = [
      {
        id: 'proj-1',
        nome: 'Projeto A',
        status: 'ATIVO' as const,
        descricao: '',
        inicio: '2026-05-10T00:00:00Z',
        fim: '2026-05-20T23:59:59Z',
        organizacaoResponsavelId: 'org-1',
      },
      {
        id: 'proj-2',
        nome: 'Projeto B',
        status: 'ATIVO' as const,
        descricao: '',
        inicio: '',
        fim: '',
        organizacaoResponsavelId: 'org-1',
      }
    ];

    it('disables save button and displays alert when event start is before project start', () => {
      const invalidEvent = {
        id: 'evt-1',
        titulo: 'Evento Mismatched',
        descricao: '',
        categoria: 'PASTORAL',
        organizacaoResponsavelId: 'org-1',
        projetoId: 'proj-1',
        nomeProjeto: 'Projeto A',
        localId: null,
        inicio: '2026-05-05T10:00:00Z',
        fim: '2026-05-05T11:00:00Z',
        status: 'CONFIRMADO',
        conflictState: null,
        conflictReason: null,
      };

      render(
        <EventRegistrationModal
          {...defaultProps}
          projetos={mockProjetos}
          initialEvent={invalidEvent}
        />
      );

      expect(screen.getByText(/é anterior ao início do projeto/i)).toBeInTheDocument();
      const saveButton = screen.getByRole('button', { name: /Salvar Alterações/i });
      expect(saveButton).toBeDisabled();
    });

    it('disables save button and displays alert when event end is after project end', () => {
      const invalidEvent = {
        id: 'evt-1',
        titulo: 'Evento Mismatched',
        descricao: '',
        categoria: 'PASTORAL',
        organizacaoResponsavelId: 'org-1',
        projetoId: 'proj-1',
        nomeProjeto: 'Projeto A',
        localId: null,
        inicio: '2026-05-15T10:00:00Z',
        fim: '2026-05-21T11:00:00Z',
        status: 'CONFIRMADO',
        conflictState: null,
        conflictReason: null,
      };

      render(
        <EventRegistrationModal
          {...defaultProps}
          projetos={mockProjetos}
          initialEvent={invalidEvent}
        />
      );

      expect(screen.getByText(/excede o término do projeto/i)).toBeInTheDocument();
      const saveButton = screen.getByRole('button', { name: /Salvar Alterações/i });
      expect(saveButton).toBeDisabled();
    });

    it('allows event creation when event timeframe matches project duration', () => {
      const validEvent = {
        id: 'evt-1',
        titulo: 'Evento Matched',
        descricao: '',
        categoria: 'PASTORAL',
        organizacaoResponsavelId: 'org-1',
        projetoId: 'proj-1',
        nomeProjeto: 'Projeto A',
        localId: null,
        inicio: '2026-05-15T10:00:00Z',
        fim: '2026-05-15T11:00:00Z',
        status: 'CONFIRMADO',
        conflictState: null,
        conflictReason: null,
      };

      render(
        <EventRegistrationModal
          {...defaultProps}
          projetos={mockProjetos}
          initialEvent={validEvent}
        />
      );

      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
      const saveButton = screen.getByRole('button', { name: /Salvar Alterações/i });
      expect(saveButton).not.toBeDisabled();
    });

    it('allows event creation when project dates are null', () => {
      const validEvent = {
        id: 'evt-1',
        titulo: 'Evento Sem Limites',
        descricao: '',
        categoria: 'PASTORAL',
        organizacaoResponsavelId: 'org-1',
        projetoId: 'proj-2',
        nomeProjeto: 'Projeto B',
        localId: null,
        inicio: '2026-07-15T10:00:00Z',
        fim: '2026-07-15T11:00:00Z',
        status: 'CONFIRMADO',
        conflictState: null,
        conflictReason: null,
      };

      render(
        <EventRegistrationModal
          {...defaultProps}
          projetos={mockProjetos}
          initialEvent={validEvent}
        />
      );

      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
      const saveButton = screen.getByRole('button', { name: /Salvar Alterações/i });
      expect(saveButton).not.toBeDisabled();
    });
  });
});
