import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import ReportFilters from './ReportFilters';

describe('ReportFilters', () => {
  const defaultProps = {
    startDate: '2026-01-01',
    endDate: '2026-12-31',
    onStartDateChange: jest.fn(),
    onEndDateChange: jest.fn(),
    onClearFilters: jest.fn(),
  };

  it('renders date filter inputs and clear button', () => {
    render(<ReportFilters {...defaultProps} />);

    expect(screen.getByLabelText(/data início/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/data fim/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /limpar filtros/i })).toBeInTheDocument();
  });

  it('calls change callbacks when dates are updated', () => {
    render(<ReportFilters {...defaultProps} />);

    const startInput = screen.getByLabelText(/data início/i);
    fireEvent.change(startInput, { target: { value: '2026-06-01' } });
    expect(defaultProps.onStartDateChange).toHaveBeenCalledWith('2026-06-01');

    const endInput = screen.getByLabelText(/data fim/i);
    fireEvent.change(endInput, { target: { value: '2026-06-30' } });
    expect(defaultProps.onEndDateChange).toHaveBeenCalledWith('2026-06-30');
  });

  it('calls onClearFilters when clear button is clicked', () => {
    render(<ReportFilters {...defaultProps} />);

    const clearButton = screen.getByRole('button', { name: /limpar filtros/i });
    fireEvent.click(clearButton);
    expect(defaultProps.onClearFilters).toHaveBeenCalled();
  });
});
