import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import Fieis from './Fieis';
import * as fieisApi from '../api/fieis';
import * as organizacoesApi from '../api/organizacoes';
import { Fiel } from '../api/types';

vi.mock('../api/fieis');
vi.mock('../api/organizacoes');
vi.mock('../api/membros');

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'u-1',
      username: 'admin',
      roles: ['ADMIN'],
    },
    isAuthenticated: true,
  }),
}));

const mockShowSuccess = vi.fn();
const mockShowError = vi.fn();
vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showSuccess: mockShowSuccess,
    showError: mockShowError,
  }),
}));

const mockFiel: Fiel = {
  id: 'fiel-1',
  nome: 'Maria da Silva',
  cpf: '123.456.789-00',
  dataNascimento: '1990-01-01',
  email: 'maria@test.com',
  telefone: '(11) 91234-5678',
  endereco: 'Rua A, 100',
  vocacao: 'LEIGO',
  ativo: true,
  batizado: true,
  primeiraComunhao: true,
  crismado: true,
  casadoIgrejaCatolica: false,
};

describe('Fieis Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (fieisApi.getFieis as any).mockResolvedValue([mockFiel]);
    (organizacoesApi.getOrganizacoes as any).mockResolvedValue([]);
  });

  test('renders page title and list of fiéis', async () => {
    render(<Fieis />);

    expect(screen.getByText('Gestão de Fiéis')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Maria da Silva')).toBeInTheDocument();
    });
    expect(screen.getByText('123.456.789-00')).toBeInTheDocument();
  });

  test('opens create modal when clicking "Novo Fiel" button', async () => {
    render(<Fieis />);

    await waitFor(() => {
      expect(screen.getByText('Maria da Silva')).toBeInTheDocument();
    });

    const newBtn = screen.getByRole('button', { name: /Novo Fiel/i });
    fireEvent.click(newBtn);

    expect(screen.getByText('Cadastrar Fiel')).toBeInTheDocument();
    expect(screen.getByLabelText(/Nome Completo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/CPF/i)).toBeInTheDocument();
  });
});
