import React from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import DialogEscopoEdicao from './DialogEscopoEdicao';

describe('DialogEscopoEdicao', () => {
  const mockOnClose = jest.fn();
  const mockOnSelect = jest.fn();

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onSelect: mockOnSelect,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders correctly when open', () => {
    render(<DialogEscopoEdicao {...defaultProps} />);
    expect(screen.getByText(/Editar Evento Recorrente/i)).toBeInTheDocument();
    expect(screen.getByText(/Apenas este evento/i)).toBeInTheDocument();
    expect(screen.getByText(/Este e os próximos/i)).toBeInTheDocument();
    expect(screen.getByText(/Todos os eventos da série/i)).toBeInTheDocument();
  });

  it('calls onClose when clicking "Cancelar"', () => {
    render(<DialogEscopoEdicao {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: /Cancelar/i }));
    expect(mockOnClose).toHaveBeenCalled();
  });

  it('calls onSelect with "SOMENTE_ESTE" when selected', () => {
    render(<DialogEscopoEdicao {...defaultProps} />);
    fireEvent.click(screen.getByText(/Apenas este evento/i));
    expect(mockOnSelect).toHaveBeenCalledWith('SOMENTE_ESTE');
  });

  it('calls onSelect with "ESTE_E_PROXIMOS" when selected', () => {
    render(<DialogEscopoEdicao {...defaultProps} />);
    fireEvent.click(screen.getByText(/Este e os próximos/i));
    expect(mockOnSelect).toHaveBeenCalledWith('ESTE_E_PROXIMOS');
  });

  it('calls onSelect with "TODOS" when selected', () => {
    render(<DialogEscopoEdicao {...defaultProps} />);
    fireEvent.click(screen.getByText(/Todos os eventos da série/i));
    expect(mockOnSelect).toHaveBeenCalledWith('TODOS');
  });

  it('renders the Cancelar button with a CloseIcon', () => {
    render(<DialogEscopoEdicao {...defaultProps} />);
    const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
    expect(cancelButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
  });
});
