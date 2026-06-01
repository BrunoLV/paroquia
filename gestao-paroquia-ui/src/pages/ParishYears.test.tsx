import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
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

  test('toggles status when switch is clicked by authorized user', async () => {
    mockGetAnosParoquiais.mockResolvedValue(mockAnos);
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

    const switches = screen.getAllByRole('switch');
    // 2026 has the toggle switch (2025 is CONCLUIDO and read-only, so no toggle switch)
    expect(switches).toHaveLength(1);
    
    fireEvent.click(switches[0]);

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

    const concludeButton = screen.getByRole('button', { name: /Concluir Ano/i });
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

    // Switches should not be rendered
    const switches = screen.queryAllByRole('switch');
    expect(switches).toHaveLength(0);

    // Conclude button should not be rendered
    const concludeButton = screen.queryByRole('button', { name: /Concluir Ano/i });
    expect(concludeButton).toBeNull();

    // Ações column header should not be rendered
    const acoesHeader = screen.queryByText('Ações');
    expect(acoesHeader).toBeNull();
  });
});
