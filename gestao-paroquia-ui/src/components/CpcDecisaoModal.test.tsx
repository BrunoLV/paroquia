import React from 'react';
import {render, screen} from '@testing-library/react';
import {CpcDecisaoModal} from './CpcDecisaoModal';

describe('CpcDecisaoModal', () => {
  const mockOnClose = jest.fn();
  const mockOnSubmit = jest.fn();

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    onSubmit: mockOnSubmit,
    communities: [],
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders Cancelar and Submeter Ata buttons with their semantic icons', () => {
    render(<CpcDecisaoModal {...defaultProps} />);
    const cancelButton = screen.getByRole('button', { name: /^Cancelar$/i });
    const submitButton = screen.getByRole('button', { name: /Submeter Ata para Homologação/i });
    expect(cancelButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
    expect(submitButton.querySelector('[data-testid="SendIcon"]')).toBeInTheDocument();
  });
});
