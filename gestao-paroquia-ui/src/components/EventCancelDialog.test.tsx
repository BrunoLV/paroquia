import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import EventCancelDialog from './EventCancelDialog';

describe('EventCancelDialog', () => {
  const mockOnClose = jest.fn();
  const mockOnConfirm = jest.fn();

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onConfirm: mockOnConfirm,
    eventTitle: 'Missa de Domingo',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders correctly when open', () => {
    render(<EventCancelDialog {...defaultProps} />);
    expect(screen.getByText(/Cancelar Evento/i)).toBeInTheDocument();
    expect(screen.getByText(/Missa de Domingo/i)).toBeInTheDocument();
  });

  it('calls onClose when clicking "Voltar"', () => {
    render(<EventCancelDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Voltar/i));
    expect(mockOnClose).toHaveBeenCalled();
  });

  it('shows error and does not call onConfirm if reason is empty', () => {
    render(<EventCancelDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Confirmar Cancelamento/i));
    
    expect(screen.getByText(/O motivo é obrigatório/i)).toBeInTheDocument();
    expect(mockOnConfirm).not.toHaveBeenCalled();
  });

  it('calls onConfirm with reason when filled', () => {
    render(<EventCancelDialog {...defaultProps} />);
    
    const input = screen.getByLabelText(/Motivo do Cancelamento/i);
    fireEvent.change(input, { target: { value: 'Imprevisto' } });
    
    fireEvent.click(screen.getByText(/Confirmar Cancelamento/i));
    
    expect(mockOnConfirm).toHaveBeenCalledWith('Imprevisto');
  });

  it('disables buttons when isSubmitting is true', () => {
    render(<EventCancelDialog {...defaultProps} isSubmitting={true} />);
    expect(screen.getByText(/Voltar/i)).toBeDisabled();
    expect(screen.getByText(/Cancelando.../i)).toBeDisabled();
  });
});
