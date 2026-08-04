import React from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import EditScopeDialog from './EditScopeDialog';

describe('EditScopeDialog', () => {
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
    render(<EditScopeDialog {...defaultProps} />);
    expect(screen.getByText(/Editar Evento Recorrente/i)).toBeInTheDocument();
    expect(screen.getByText(/Apenas este evento/i)).toBeInTheDocument();
    expect(screen.getByText(/Este e os próximos/i)).toBeInTheDocument();
    expect(screen.getByText(/Todos os eventos da série/i)).toBeInTheDocument();
  });

  it('calls onClose when clicking "Cancelar"', () => {
    render(<EditScopeDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Cancelar/i));
    expect(mockOnClose).toHaveBeenCalled();
  });

  it('calls onSelect with "ONLY_THIS" when selected', () => {
    render(<EditScopeDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Apenas este evento/i));
    expect(mockOnSelect).toHaveBeenCalledWith('ONLY_THIS');
  });

  it('calls onSelect with "THIS_AND_FOLLOWING" when selected', () => {
    render(<EditScopeDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Este e os próximos/i));
    expect(mockOnSelect).toHaveBeenCalledWith('THIS_AND_FOLLOWING');
  });

  it('calls onSelect with "EDIT_ALL" when selected', () => {
    render(<EditScopeDialog {...defaultProps} />);
    fireEvent.click(screen.getByText(/Todos os eventos da série/i));
    expect(mockOnSelect).toHaveBeenCalledWith('EDIT_ALL');
  });

  it('renders the Cancelar button with a CloseIcon', () => {
    render(<EditScopeDialog {...defaultProps} />);
    const cancelButton = screen.getByText(/Cancelar/i).closest('button');
    expect(cancelButton?.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
  });
});
