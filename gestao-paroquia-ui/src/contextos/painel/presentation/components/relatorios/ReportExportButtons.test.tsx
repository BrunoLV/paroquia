import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import ReportExportButtons from './ReportExportButtons';
import { describe, expect, it, vi } from 'vitest';

describe('ReportExportButtons', () => {
  const defaultProps = {
    onExportCSV: vi.fn(),
    onExportPDF: vi.fn(),
    onExportWord: vi.fn(),
    isExporting: false,
  };

  it('renders Export CSV, Export PDF, and Export Word buttons', () => {
    render(<ReportExportButtons {...defaultProps} />);

    expect(screen.getByRole('button', { name: /exportar csv/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /exportar pdf/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /exportar word/i })).toBeInTheDocument();
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

  it('triggers onExportWord when Word button clicked', () => {
    render(<ReportExportButtons {...defaultProps} />);

    const wordButton = screen.getByRole('button', { name: /exportar word/i });
    fireEvent.click(wordButton);
    expect(defaultProps.onExportWord).toHaveBeenCalled();
  });

  it('disables buttons when isExporting is true', () => {
    render(<ReportExportButtons {...defaultProps} isExporting={true} />);

    expect(screen.getByRole('button', { name: /exportar csv/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /exportar pdf/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /exportar word/i })).toBeDisabled();
  });
});
