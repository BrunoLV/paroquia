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

  it('calls onClose when cancel button is clicked', () => {
    render(<EventRegistrationModal {...defaultProps} />);
    const cancelButton = screen.getByRole('button', { name: /cancelar/i });
    fireEvent.click(cancelButton);
    expect(mockOnClose).toHaveBeenCalled();
  });
});
