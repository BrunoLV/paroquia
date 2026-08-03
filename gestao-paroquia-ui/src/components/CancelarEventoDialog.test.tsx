import {vi} from "vitest";
import React from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import CancelarEventoDialog from './CancelarEventoDialog';

const mockShowError = jest.fn();
vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
  }),
}));

describe('CancelarEventoDialog', () => {
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
    render(<CancelarEventoDialog {...defaultProps} />);
    expect(screen.getByText(/Cancelar Evento/i)).toBeInTheDocument();
    expect(screen.getByText(/Missa de Domingo/i)).toBeInTheDocument();
  });

  it('calls onClose when clicking "Voltar"', () => {
    render(<CancelarEventoDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Voltar/i));
    expect(mockOnClose).toHaveBeenCalled();
  });

  it('shows error and does not call onConfirm if reason is empty', () => {
    render(<CancelarEventoDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Confirmar Cancelamento/i));
    
    expect(mockShowError).toHaveBeenCalledWith('O motivo do cancelamento é obrigatório.');
    expect(mockOnConfirm).not.toHaveBeenCalled();
  });

  it('calls onConfirm with reason when filled', () => {
    render(<CancelarEventoDialog {...defaultProps} />);
    
    const input = screen.getByLabelText(/Motivo do Cancelamento/i);
    fireEvent.change(input, { target: { value: 'Imprevisto' } });
    
    fireEvent.submit(screen.getByRole('button', { name: /Confirmar Cancelamento/i }));
    
    expect(mockOnConfirm).toHaveBeenCalledWith('Imprevisto');
  });

  it('disables buttons when isSubmitting is true', () => {
    render(<CancelarEventoDialog {...defaultProps} isSubmitting={true} />);
    expect(screen.getByText(/Voltar/i)).toBeDisabled();
    expect(screen.getByText(/Cancelando.../i)).toBeDisabled();
  });
});
