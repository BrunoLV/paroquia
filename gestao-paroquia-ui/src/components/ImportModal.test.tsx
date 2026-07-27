import { vi } from "vitest";
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import ImportModal from './ImportModal';
import { downloadTemplate, validarImportacao, confirmarImportacao } from '../api/importacao';

vi.mock('../api/importacao', () => ({
  downloadTemplate: jest.fn(),
  validarImportacao: jest.fn(),
  confirmarImportacao: jest.fn(),
}));

const mockShowSuccess = jest.fn();
const mockShowError = jest.fn();

vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showSuccess: mockShowSuccess,
    showError: mockShowError,
  }),
}));

const mockDownloadTemplate = vi.mocked(downloadTemplate);
const mockValidarImportacao = vi.mocked(validarImportacao);
const mockConfirmarImportacao = vi.mocked(confirmarImportacao);

const createTestQueryClient = () => new QueryClient({
  defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
});

const renderModal = (ui: React.ReactElement) => render(
  <QueryClientProvider client={createTestQueryClient()}>{ui}</QueryClientProvider>
);

describe('ImportModal', () => {
  const mockOnClose = jest.fn();
  const mockOnSuccess = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders modal when open is true', () => {
    renderModal(<ImportModal open={true} onClose={mockOnClose} />);
    expect(screen.getByText(/Importação de Registros em Lote/i)).toBeInTheDocument();
  });

  it('triggers CSV template download', async () => {
    mockDownloadTemplate.mockResolvedValueOnce(undefined);
    renderModal(<ImportModal open={true} onClose={mockOnClose} />);

    const downloadCsvButton = screen.getByRole('button', { name: /Baixar Template CSV/i });
    fireEvent.click(downloadCsvButton);

    expect(mockDownloadTemplate).toHaveBeenCalledWith('MEMBRO', 'CSV');
    await waitFor(() => {
      expect(mockShowSuccess).toHaveBeenCalledWith(expect.stringContaining('Modelo CSV'));
    });
  });

  it('allows file selection and proceeds to validation preview step', async () => {
    mockValidarImportacao.mockResolvedValueOnce({
      totalLinhas: 2,
      totalValidos: 1,
      totalInvalidos: 1,
      itens: [
        { linha: 2, valido: true, mensagemErro: undefined, dados: { nomeCompleto: 'Maria' } },
        { linha: 3, valido: false, mensagemErro: 'E-mail inválido', dados: {} },
      ],
    });

    renderModal(<ImportModal open={true} onClose={mockOnClose} />);

    const file = new File(['nomeCompleto\nMaria'], 'membros.csv', { type: 'text/csv' });
    const fileInput = screen.getByLabelText(/Selecionar Arquivo/i);
    fireEvent.change(fileInput, { target: { files: [file] } });

    const validateButton = screen.getByRole('button', { name: /Validar Arquivo/i });
    fireEvent.click(validateButton);

    await waitFor(() => {
      expect(screen.getByText('1 Válidos')).toBeInTheDocument();
      expect(screen.getByText('1 Inválidos')).toBeInTheDocument();
      expect(screen.getByText('E-mail inválido')).toBeInTheDocument();
    });
  });

  it('executes batch confirmation and calls onSuccess callback', async () => {
    mockValidarImportacao.mockResolvedValueOnce({
      totalLinhas: 1,
      totalValidos: 1,
      totalInvalidos: 0,
      itens: [
        { linha: 2, valido: true, mensagemErro: undefined, dados: { nomeCompleto: 'João' } },
      ],
    });
    mockConfirmarImportacao.mockResolvedValueOnce({ registrosImportados: 1 });

    renderModal(<ImportModal open={true} onClose={mockOnClose} onSuccess={mockOnSuccess} />);

    const file = new File(['nomeCompleto\nJoão'], 'membros.csv', { type: 'text/csv' });
    const fileInput = screen.getByLabelText(/Selecionar Arquivo/i);
    fireEvent.change(fileInput, { target: { files: [file] } });

    fireEvent.click(screen.getByRole('button', { name: /Validar Arquivo/i }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Avançar para Confirmação/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /Avançar para Confirmação/i }));

    const confirmButton = screen.getByRole('button', { name: /Confirmar Importação/i });
    fireEvent.click(confirmButton);

    await waitFor(() => {
      expect(mockConfirmarImportacao).toHaveBeenCalledWith('MEMBRO', [{ nomeCompleto: 'João' }]);
      expect(mockShowSuccess).toHaveBeenCalledWith(expect.stringContaining('1 registro(s) foram cadastrados'));
      expect(mockOnSuccess).toHaveBeenCalled();
    });
  });
});
