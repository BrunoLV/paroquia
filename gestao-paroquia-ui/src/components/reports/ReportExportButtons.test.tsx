import React from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import ReportExportButtons from './ReportExportButtons';

describe('ReportExportButtons', () => {
  const defaultProps = {
    onExportCSV: jest.fn(),
    onExportPDF: jest.fn(),
    isExporting: false,
  };

  it('renders Export CSV and Export PDF buttons', () => {
    render(<ReportExportButtons {...defaultProps} />);

    expect(screen.getByRole('button', { name: /exportar csv/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /exportar pdf/i })).toBeInTheDocument();
  });

  it('triggers onExportCSV when CSV button clicked', () => {
    render(<ReportExportButtons {...defaultProps} />);

    const csvButton = screen.getByRole('button', { name: /exportar csv/i });
    fireEvent.click(csvButton);
    expect(defaultProps.onExportCSV).toHaveBeenCalled();
  });

  it('triggers onExportPDF when PDF button clicked', () => {
    render(<ReportExportButtons {...defaultProps} />);

    const pdfButton = screen.getByRole('button', { name: /exportar pdf/i });
    fireEvent.click(pdfButton);
    expect(defaultProps.onExportPDF).toHaveBeenCalled();
  });

  it('disables buttons when isExporting is true', () => {
    render(<ReportExportButtons {...defaultProps} isExporting={true} />);

    expect(screen.getByRole('button', { name: /exportar csv/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /exportar pdf/i })).toBeDisabled();
  });

  it('uses the semantic info color instead of hardcoded hex colors', () => {
    render(<ReportExportButtons {...defaultProps} />);

    const csvButton = screen.getByRole('button', { name: /exportar csv/i });
    const pdfButton = screen.getByRole('button', { name: /exportar pdf/i });
    expect(csvButton.className).toContain('MuiButton-colorInfo');
    expect(pdfButton.className).toContain('MuiButton-colorInfo');
  });
});
