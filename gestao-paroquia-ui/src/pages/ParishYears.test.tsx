import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { QueryProvider } from '../api/QueryProvider';
import ParishYears from './ParishYears';
import { getAnosParoquiais, updateAnoParoquialStatus } from '../api/anosParoquiais';
import { MemoryRouter } from 'react-router-dom';
import { NotificationProvider } from '../context/NotificationContext';
import { useAuth } from '../auth/AuthContext';

jest.mock('../api/anosParoquiais', () => ({
  getAnosParoquiais: jest.fn(),
  updateAnoParoquialStatus: jest.fn(),
}));

jest.mock('../auth/AuthContext', () => ({
  useAuth: jest.fn(),
}));

const mockGetAnosParoquiais = getAnosParoquiais as jest.Mock;
const mockUpdateAnoParoquialStatus = updateAnoParoquialStatus as jest.Mock;
const mockUseAuth = useAuth as jest.Mock;

const mockAnos = [
  {
    ano: 2026,
    status: 'EM_PLANEJAMENTO',
    dataFechamentoUtc: null,
    updatedAt: '2026-05-31T20:00:00Z',
  },
  {
    ano: 2025,
    status: 'CONCLUIDO',
    dataFechamentoUtc: '2025-12-31T23:59:59Z',
    updatedAt: '2025-12-31T23:59:59Z',
  },
];

describe('ParishYears page', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        name: 'Padre',
        roles: ['ROLE_CLERO_PAROCO'],
        organizations: [],
      },
    });
  });

  const renderPage = () => {
    return render(
      <QueryProvider>
        <NotificationProvider>
          <MemoryRouter>
            <ParishYears />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );
  };

  test('renders page title and list of years', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Gestão do Ano Paroquial')).toBeInTheDocument();
      expect(screen.getByText('2026')).toBeInTheDocument();
      expect(screen.getByText('2025')).toBeInTheDocument();
      expect(screen.getByText('Planejamento Aberto')).toBeInTheDocument();
      expect(screen.getByText('Concluído')).toBeInTheDocument();
    });
  });

  test('calls update status when Fechar button is clicked by authorized user', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockUpdateAnoParoquialStatus.mockResolvedValueOnce({
      ano: 2026,
      status: 'FECHADO',
      dataFechamentoUtc: '2026-05-31T23:00:00Z',
      updatedAt: '2026-05-31T23:00:00Z',
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument();
    });

    const fecharBtn = screen.getByRole('button', { name: /Fechar/i });
    expect(screen.queryByRole('button', { name: /Reabrir/i })).toBeNull();
    
    fireEvent.click(fecharBtn);

    await waitFor(() => {
      expect(mockUpdateAnoParoquialStatus).toHaveBeenCalledWith(2026, 'FECHADO');
    });
  });

  test('calls update status when Reabrir button is clicked by authorized user', async () => {
    const mockAnosFechados = [
      {
        ano: 2026,
        status: 'FECHADO',
        dataFechamentoUtc: '2026-05-31T23:00:00Z',
        updatedAt: '2026-05-31T23:00:00Z',
      },
      {
        ano: 2025,
        status: 'CONCLUIDO',
        dataFechamentoUtc: '2025-12-31T23:59:59Z',
        updatedAt: '2025-12-31T23:59:59Z',
      },
    ];
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnosFechados);
    mockUpdateAnoParoquialStatus.mockResolvedValueOnce({
      ano: 2026,
      status: 'EM_PLANEJAMENTO',
      dataFechamentoUtc: null,
      updatedAt: '2026-06-01T00:00:00Z',
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument();
    });

    const reabrirBtn = screen.getByRole('button', { name: /Reabrir/i });
    expect(screen.queryByRole('button', { name: /Fechar/i })).toBeNull();

    fireEvent.click(reabrirBtn);

    await waitFor(() => {
      expect(mockUpdateAnoParoquialStatus).toHaveBeenCalledWith(2026, 'EM_PLANEJAMENTO');
    });
  });

  test('opens confirmation modal and concludes year when button is clicked', async () => {
    mockGetAnosParoquiais.mockResolvedValue(mockAnos);
    mockUpdateAnoParoquialStatus.mockResolvedValueOnce({
      ano: 2026,
      status: 'CONCLUIDO',
      dataFechamentoUtc: '2026-05-31T23:00:00Z',
      updatedAt: '2026-05-31T23:00:00Z',
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument();
    });

    const concludeButton = screen.getByRole('button', { name: /Concluir/i });
    fireEvent.click(concludeButton);

    // Confirmation dialog opens
    expect(screen.getByText(/Confirmar Conclusão/i)).toBeInTheDocument();

    const confirmBtn = screen.getByRole('button', { name: /Confirmar/i });
    fireEvent.click(confirmBtn);

    await waitFor(() => {
      expect(mockUpdateAnoParoquialStatus).toHaveBeenCalledWith(2026, 'CONCLUIDO');
    });
  });

  test('disables toggle actions for unauthorized user', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '2',
        name: 'Membro Regular',
        roles: ['ROLE_PASTORAL_MEMBRO'],
        organizations: [],
      },
    });
    mockGetAnosParoquiais.mockResolvedValue(mockAnos);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument();
    });

    // Buttons should not be rendered
    expect(screen.queryByRole('button', { name: /Fechar/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /Reabrir/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /Concluir/i })).toBeNull();

    // Ações column header should not be rendered
    const acoesHeader = screen.queryByText('Ações');
    expect(acoesHeader).toBeNull();
  });

  test('disables Fechar and Concluir buttons if prior year is not concluded', async () => {
    const mockAnosNotConcluido = [
      {
        ano: 2026,
        status: 'EM_PLANEJAMENTO',
        dataFechamentoUtc: null,
        updatedAt: '2026-05-31T20:00:00Z',
      },
      {
        ano: 2025,
        status: 'EM_PLANEJAMENTO',
        dataFechamentoUtc: null,
        updatedAt: '2025-05-31T20:00:00Z',
      },
    ];
    mockGetAnosParoquiais.mockResolvedValue(mockAnosNotConcluido);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument();
      expect(screen.getByText('2025')).toBeInTheDocument();
    });

    // For 2026, buttons should be disabled because 2025 is not concluded
    const row2026 = screen.getAllByRole('row').find(row => row.textContent?.includes('2026'));
    expect(row2026).toBeDefined();
    
    if (row2026) {
      const fecharBtn = within(row2026).getByRole('button', { name: /Fechar/i });
      const concluirBtn = within(row2026).getByRole('button', { name: /Concluir/i });
      expect(fecharBtn).toBeDisabled();
      expect(concluirBtn).toBeDisabled();
    }

    // For 2025 (no prior year), buttons should be active
    const row2025 = screen.getAllByRole('row').find(row => row.textContent?.includes('2025'));
    expect(row2025).toBeDefined();

    if (row2025) {
      const fecharBtn = within(row2025).getByRole('button', { name: /Fechar/i });
      const concluirBtn = within(row2025).getByRole('button', { name: /Concluir/i });
      expect(fecharBtn).not.toBeDisabled();
      expect(concluirBtn).not.toBeDisabled();
    }
  });
});
