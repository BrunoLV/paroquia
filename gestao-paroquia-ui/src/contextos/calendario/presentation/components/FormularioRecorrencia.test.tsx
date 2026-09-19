import React from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import FormularioRecorrencia, {RegraRecorrencia} from './FormularioRecorrencia';

describe('FormularioRecorrencia', () => {
  const mockOnChange = jest.fn();

  const defaultRule: RegraRecorrencia = {
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
    render(<FormularioRecorrencia {...defaultProps} />);
    expect(screen.getByText(/Configurações de Repetição/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Frequência/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/A cada \(semanas\)/i)).toBeInTheDocument();
  });

  it('calls onChange when frequency changes', () => {
    render(<FormularioRecorrencia {...defaultProps} />);
    
    const select = screen.getByLabelText(/Frequência/i);
    fireEvent.mouseDown(select);
    fireEvent.click(screen.getByText(/Diária/i));
    
    expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({ frequencia: 'DIARIA' }));
  });

  it('calls onChange when interval changes', () => {
    render(<FormularioRecorrencia {...defaultProps} />);
    
    const input = screen.getByLabelText(/A cada \(semanas\)/i);
    fireEvent.change(input, { target: { value: '2' } });
    
    expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({ intervalo: 2 }));
  });

  it('shows days of week only for weekly frequency', () => {
    const { rerender } = render(<FormularioRecorrencia {...defaultProps} />);
    expect(screen.getByText(/Seg/i)).toBeInTheDocument();
    
    rerender(<FormularioRecorrencia {...defaultProps} rule={{ ...defaultRule, frequencia: 'DIARIA' }} />);
    expect(screen.queryByText(/Seg/i)).not.toBeInTheDocument();
  });

  it('handles termination type changes', () => {
    render(<FormularioRecorrencia {...defaultProps} />);
    
    // Switch to Count termination
    const countRadio = screen.getByLabelText(/Após:/i);
    fireEvent.click(countRadio);
    
    expect(mockOnChange).toHaveBeenCalledWith(expect.objectContaining({ dataLimite: undefined }));
  });
});
