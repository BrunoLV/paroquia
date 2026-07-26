import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import { OrganogramaExportButton } from '../OrganogramaExportButton';

describe('OrganogramaExportButton', () => {
  const defaultProps = {
    onExport: vi.fn(),
    isExporting: false,
  };

  test('deve renderizar o botão de exportação', () => {
    render(<OrganogramaExportButton {...defaultProps} />);
    expect(screen.getByRole('button', { name: /exportar/i })).toBeInTheDocument();
  });

  test('deve abrir menu de opções ao clicar no botão', () => {
    render(<OrganogramaExportButton {...defaultProps} />);
    const button = screen.getByRole('button', { name: /exportar/i });
    fireEvent.click(button);

    expect(screen.getByRole('menu')).toBeInTheDocument();
  });

  test('deve exibir opções PNG e SVG no menu', () => {
    render(<OrganogramaExportButton {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: /exportar/i }));

    expect(screen.getByText(/exportar como png/i)).toBeInTheDocument();
    expect(screen.getByText(/exportar como svg/i)).toBeInTheDocument();
  });

  test('deve chamar onExport("png") ao selecionar PNG', () => {
    const onExportMock = vi.fn();
    render(<OrganogramaExportButton {...defaultProps} onExport={onExportMock} />);

    fireEvent.click(screen.getByRole('button', { name: /exportar/i }));
    fireEvent.click(screen.getByText(/exportar como png/i));

    expect(onExportMock).toHaveBeenCalledWith('png');
  });

  test('deve chamar onExport("svg") ao selecionar SVG', () => {
    const onExportMock = vi.fn();
    render(<OrganogramaExportButton {...defaultProps} onExport={onExportMock} />);

    fireEvent.click(screen.getByRole('button', { name: /exportar/i }));
    fireEvent.click(screen.getByText(/exportar como svg/i));

    expect(onExportMock).toHaveBeenCalledWith('svg');
  });

  test('deve exibir indicador de loading e desabilitar o botão quando isExporting = true', () => {
    render(<OrganogramaExportButton {...defaultProps} isExporting={true} />);
    const button = screen.getByRole('button', { name: /exportando/i });

    expect(button).toBeDisabled();
    expect(screen.getByTestId('export-loading-indicator')).toBeInTheDocument();
  });
});
