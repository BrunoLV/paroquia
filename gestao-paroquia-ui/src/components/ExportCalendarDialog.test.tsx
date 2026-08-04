import React from 'react';
import {render, screen} from '@testing-library/react';
import {ExportCalendarDialog} from './ExportCalendarDialog';

describe('ExportCalendarDialog', () => {
  const mockOnClose = jest.fn();

  const defaultProps = {
    open: true,
    onClose: mockOnClose,
    organizations: [],
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders Cancelar and Exportar buttons with their semantic icons', () => {
    render(<ExportCalendarDialog {...defaultProps} />);
    const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
    const exportButton = screen.getByRole('button', { name: /Exportar/i });
    expect(cancelButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
    expect(exportButton.querySelector('[data-testid="DownloadIcon"]')).toBeInTheDocument();
  });
});
