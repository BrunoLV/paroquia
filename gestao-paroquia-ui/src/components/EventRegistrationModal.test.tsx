import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import EventRegistrationModal from './EventRegistrationModal';

describe('EventRegistrationModal', () => {
  const mockOnClose = jest.fn();
  const mockOnSubmit = jest.fn();

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onSubmit: mockOnSubmit,
    isSubmitting: false,
    organizations: [],
  };

  it('renders modal when open is true', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    expect(screen.getByText(/Criar Novo Evento/i)).toBeInTheDocument();
  });

  it('does not render modal when open is false', () => {
    render(<EventRegistrationModal {...defaultProps} open={false} />);
    expect(screen.queryByText(/Criar Novo Evento/i)).not.toBeInTheDocument();
  });

  it('renders basic form fields', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    expect(screen.getByLabelText(/título do evento/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/descrição/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/categoria/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/organização responsável/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/data e hora de início/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/data e hora de término/i)).toBeInTheDocument();
  });

  it('renders with "Editar Evento" title and pre-filled data when in edit mode', () => {
    const eventToEdit = {
      id: 'evt-1',
      titulo: 'Evento Existente',
      descricao: 'Descrição Existente',
      categoria: 'SOCIAL',
      organizacaoResponsavelId: 'org-1',
      inicio: '2026-05-20T10:00:00Z',
      fim: '2026-05-20T12:00:00Z',
      status: 'CONFIRMADO',
    };
    render(<EventRegistrationModal {...defaultProps} initialEvent={eventToEdit} />);
    
    expect(screen.getByText(/Editar Evento/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/título do evento/i)).toHaveValue('Evento Existente');
    expect(screen.getByLabelText(/descrição/i)).toHaveValue('Descrição Existente');
  });
});
