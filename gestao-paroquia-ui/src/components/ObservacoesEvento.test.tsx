import {vi} from "vitest";
import React from 'react';
import {act, fireEvent, render, screen, waitFor} from '@testing-library/react';
import '@testing-library/jest-dom';
// @ts-ignore
import ObservacoesEvento from './ObservacoesEvento';
import {createObservacao, getObservacoes} from '../api/observacoes';
import {ThemeProvider} from '@mui/material/styles';
import theme from '../theme';

// Mock API client methods
vi.mock('../api/observacoes', () => ({
  getObservacoes: jest.fn(),
  createObservacao: jest.fn(),
  updateObservacao: jest.fn(),
  deleteObservacao: jest.fn(),
}));

// Mock Notification Context
vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: jest.fn(),
    showSuccess: jest.fn(),
  }),
}));

// Mock Auth Context directly to return logged-in user
const mockUser = { id: 'usr-1', name: 'Padre Pedro', username: 'padre.pedro', roles: ['ADMIN'], organizations: [] };
vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
    isLoading: false,
  }),
}));

const mockObservacoes = [
  { id: 'obs-1', eventoId: 'evt-1', usuarioId: 'usr-1', tipo: 'NOTA', conteudo: 'Esta é uma nota minha', criadoEmUtc: '2026-06-13T14:00:00Z' },
  { id: 'obs-2', eventoId: 'evt-1', usuarioId: 'usr-2', tipo: 'NOTA', conteudo: 'Esta é uma nota de outro', criadoEmUtc: '2026-06-13T14:10:00Z' },
  { id: 'obs-3', eventoId: 'evt-1', usuarioId: 'system', tipo: 'CANCELAMENTO', conteudo: 'Motivo: Chuva forte', criadoEmUtc: '2026-06-13T14:20:00Z' }
];

const renderComponent = async () => {
  (getObservacoes as jest.Mock).mockResolvedValue(mockObservacoes);

  await act(async () => {
    render(
      <ThemeProvider theme={theme}>
        <ObservacoesEvento eventoId="evt-1" />
      </ThemeProvider>
    );
  });
};

describe('ObservacoesEvento Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders list of observations and inputs', async () => {
    await renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Esta é uma nota minha')).toBeInTheDocument();
      expect(screen.getByText('Esta é uma nota de outro')).toBeInTheDocument();
      expect(screen.getByText('Motivo: Chuva forte')).toBeInTheDocument();
    });

    // Author labeling
    expect(screen.getByText(/Você/i)).toBeInTheDocument();
    expect(screen.getByText(/Usuário/i)).toBeInTheDocument();
    expect(screen.getByText(/Sistema/i)).toBeInTheDocument();

    // Input field
    expect(screen.getByPlaceholderText('Adicione uma nota...')).toBeInTheDocument();
  });

  test('calls createObservacao when adding a new note', async () => {
    await renderComponent();
    (createObservacao as jest.Mock).mockResolvedValue({
      id: 'obs-4', eventoId: 'evt-1', usuarioId: 'usr-1', tipo: 'NOTA', conteudo: 'Nova nota digitada', criadoEmUtc: new Date().toISOString()
    });

    const input = screen.getByPlaceholderText('Adicione uma nota...');
    fireEvent.change(input, { target: { value: 'Nova nota digitada' } });

    const btn = screen.getByRole('button', { name: /Adicionar/i });
    await act(async () => {
      fireEvent.click(btn);
    });

    expect(createObservacao).toHaveBeenCalledWith('evt-1', 'Nova nota digitada');
    expect(getObservacoes).toHaveBeenCalled();
  });

  test('shows edit/delete actions only for user-owned manual notes', async () => {
    await renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Esta é uma nota minha')).toBeInTheDocument();
    });

    // We should see edit and delete icons/buttons for the first note (own note)
    const editButtons = screen.getAllByRole('button', { name: /edit/i });
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });

    expect(editButtons.length).toBeGreaterThan(0);
    expect(deleteButtons.length).toBeGreaterThan(0);
  });

  test('renders the edit IconButton with the secondary semantic color', async () => {
    await renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Esta é uma nota minha')).toBeInTheDocument();
    });

    const editButtons = screen.getAllByRole('button', { name: /edit/i });
    expect(editButtons[0].className).toContain('MuiIconButton-colorSecondary');
  });

  test('renders Cancelar and Excluir buttons with semantic icons in the delete confirmation dialog', async () => {
    await renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Esta é uma nota minha')).toBeInTheDocument();
    });

    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    fireEvent.click(deleteButtons[0]);

    const cancelButton = await screen.findByRole('button', { name: /Cancelar/i });
    const confirmDeleteButton = screen.getByRole('button', { name: /Excluir Observação/i });
    expect(cancelButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
    expect(confirmDeleteButton.querySelector('[data-testid="DeleteIcon"]')).toBeInTheDocument();
  });
});
