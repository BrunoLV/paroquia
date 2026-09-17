import React from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import RecurrenceForm, {RecurrenceRule} from './RecurrenceForm';

describe('RecurrenceForm', () => {
  const mockOnChange = jest.fn();

  const defaultRule: RecurrenceRule = {
    frequencia: 'SEMANAL',
    intervalo: 1,
    diasDaSemana: ['MONDAY'],
  };

  const defaultProps = {
    rule: defaultRule,
    onChange: mockOnChange,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders correctly', () => {
    render(<RecurrenceForm {...defaultProps} />);
    expect(screen.getByText(/Configurações de Repetição/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Frequência/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/A cada \(semanas\)/i)).toBeInTheDocument();
  });

  it('calls onChange when frequency changes', () => {
    render(<RecurrenceForm {...defaultProps} />);
    
    const select = screen.getByLabelText(/Frequência/i);
    fireEvent.mouseDown(select);
    fireEvent.click(screen.getByText(/Diária/i));
    
    expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({ frequencia: 'DIARIA' }));
  });

  it('calls onChange when interval changes', () => {
    render(<RecurrenceForm {...defaultProps} />);
    
    const input = screen.getByLabelText(/A cada \(semanas\)/i);
    fireEvent.change(input, { target: { value: '2' } });
    
    expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({ intervalo: 2 }));
  });

  it('shows days of week only for weekly frequency', () => {
    const { rerender } = render(<RecurrenceForm {...defaultProps} />);
    expect(screen.getByText(/Seg/i)).toBeInTheDocument();
    
    rerender(<RecurrenceForm {...defaultProps} rule={{ ...defaultRule, frequencia: 'DIARIA' }} />);
    expect(screen.queryByText(/Seg/i)).not.toBeInTheDocument();
  });

  it('handles termination type changes', () => {
    render(<RecurrenceForm {...defaultProps} />);
    
    // Switch to Count termination
    const countRadio = screen.getByLabelText(/Após:/i);
    fireEvent.click(countRadio);
    
    expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({ dataLimite: undefined }));
  });
});
