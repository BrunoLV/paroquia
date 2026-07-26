import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import ReportFilters from './ReportFilters';

describe('ReportFilters', () => {
  const defaultProps = {
    startDate: '2026-01-01',
    endDate: '2026-12-31',
    onApplyFilters: jest.fn(),
    onClearFilters: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders date filter inputs, apply button, and clear button', () => {
    render(<ReportFilters {...defaultProps} />);

    expect(screen.getByLabelText(/data início/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/data fim/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^filtrar$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /limpar filtros/i })).toBeInTheDocument();
  });

  it('calls onApplyFilters only when the filter button is clicked', () => {
    render(<ReportFilters {...defaultProps} />);

    const startInput = screen.getByLabelText(/data início/i);
    fireEvent.change(startInput, { target: { value: '2026-06-01' } });
    expect(defaultProps.onApplyFilters).not.toHaveBeenCalled();

    const filterButton = screen.getByRole('button', { name: /^filtrar$/i });
    fireEvent.click(filterButton);
    expect(defaultProps.onApplyFilters).toHaveBeenCalledWith('2026-06-01', '2026-12-31');
  });

  it('calls onClearFilters when clear button is clicked', () => {
    render(<ReportFilters {...defaultProps} />);

    const clearButton = screen.getByRole('button', { name: /limpar filtros/i });
    fireEvent.click(clearButton);
    expect(defaultProps.onClearFilters).toHaveBeenCalled();
  });
});
