import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { QueryProvider } from '../api/QueryProvider';
import Settings from './Settings';
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

describe('Settings page', () => {
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
            <Settings />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );
  };

  test('renders page title, tabs, and default Ano Paroquial list', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);

    renderPage();

    expect(screen.getByText('Configurações do Sistema')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Ano Paroquial/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Locais/i })).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Gestão do Ano Paroquial')).toBeInTheDocument();
      expect(screen.getByText('2026')).toBeInTheDocument();
      expect(screen.getByText('2025')).toBeInTheDocument();
      expect(screen.getByText('Planejamento Aberto')).toBeInTheDocument();
      expect(screen.getByText('Concluído')).toBeInTheDocument();
    });
  });

  test('switches tabs to Locais when clicking the tab', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);

    renderPage();

    const locaisTab = screen.getByRole('tab', { name: /Locais/i });
    fireEvent.click(locaisTab);

    expect(screen.getByText(/Esqueleto da funcionalidade de CRUD de locais/)).toBeInTheDocument();
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
    fireEvent.click(fecharBtn);

    await waitFor(() => {
      expect(mockUpdateAnoParoquialStatus).toHaveBeenCalledWith(2026, 'FECHADO');
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

    expect(screen.getByText(/Confirmar Conclusão/i)).toBeInTheDocument();

    const confirmBtn = screen.getByRole('button', { name: /Confirmar/i });
    fireEvent.click(confirmBtn);

    await waitFor(() => {
      expect(mockUpdateAnoParoquialStatus).toHaveBeenCalledWith(2026, 'CONCLUIDO');
    });
  });
});
