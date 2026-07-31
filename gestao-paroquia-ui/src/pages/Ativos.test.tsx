import { vi } from 'vitest';
import React from 'react';
import { act, fireEvent, render, screen } from '@testing-library/react';
import Ativos from './Ativos';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '../api/client';
import { AuthProvider } from '../auth/AuthContext';

vi.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = jest.fn();
const mockShowSuccess = jest.fn();
vi.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
    showSuccess: mockShowSuccess,
  }),
}));

describe('Ativos Page', () => {
  const mockOrgs = [
    { id: 'org-1', nome: 'Comunidade Santo Agostinho', ativo: true },
    { id: 'org-2', nome: 'Matriz', ativo: true },
  ];

  const mockAtivos = [
    {
      id: 'ativo-1',
      nome: 'TV 55 polegadas',
      numeroTombamento: 'PAT-2026-0001',
      categoria: 'ELETRONICO',
      status: 'DISPONIVEL',
      reservavel: true,
      organizacaoId: 'org-1',
      dataAquisicao: '2024-01-10',
      valorEstimado: 3500,
      criadoEmUtc: '2026-01-01T00:00:00Z',
    },
    {
      id: 'ativo-2',
      nome: 'Projetor Epson',
      numeroTombamento: 'PAT-2026-0002',
      categoria: 'ELETRONICO',
      status: 'ALOCADO',
      reservavel: true,
      organizacaoId: 'org-1',
      dataAquisicao: '2023-05-20',
      valorEstimado: 2000,
      criadoEmUtc: '2026-01-02T00:00:00Z',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    mockShowError.mockClear();
    mockShowSuccess.mockClear();
  });

  const mockEventos = [
    {
      id: 'evento-1',
      titulo: 'Encontro de Catequistas',
      inicio: '2026-02-10T14:00:00Z',
      fim: '2026-02-10T16:00:00Z',
      status: 'CONFIRMADO',
      organizacaoResponsavelId: 'org-1',
    },
  ];

  const renderAtivos = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') {
        return Promise.resolve({ data: user });
      }
      if (url === '/organizacoes') {
        return Promise.resolve({ data: mockOrgs });
      }
      if (url === '/ativos/organizacao/org-1') {
        return Promise.resolve({ data: mockAtivos });
      }
      if (url === '/eventos') {
        return Promise.resolve({ data: mockEventos });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <Ativos />
            </ThemeProvider>
          </AuthProvider>
        </MemoryRouter>
      );
    });
  };

  const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
  const memberUser = {
    id: '2',
    name: 'Membro',
    roles: ['ROLE_PASTORAL_MEMBRO'],
    organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', ativo: true }],
  };

  test('renders page heading', async () => {
    await renderAtivos(adminUser);

    expect(screen.getByRole('heading', { name: /Ativos Patrimoniais/i })).toBeInTheDocument();
  });

  test('lists ativos after selecting an organization', async () => {
    await renderAtivos(adminUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByText('Comunidade Santo Agostinho').click();
    });

    expect(screen.getByText('TV 55 polegadas')).toBeInTheDocument();
    expect(screen.getByText('Projetor Epson')).toBeInTheDocument();
  });

  test('shows Novo Ativo button only for authorized users', async () => {
    await renderAtivos(memberUser);
    expect(screen.queryByRole('button', { name: /Novo Ativo/i })).not.toBeInTheDocument();
  });

  test('shows Novo Ativo button for admin', async () => {
    await renderAtivos(adminUser);
    expect(screen.getByRole('button', { name: /Novo Ativo/i })).toBeInTheDocument();
  });

  test('only allows Alocar action for DISPONIVEL ativos', async () => {
    const coordenadorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', ativo: true }],
    };
    await renderAtivos(coordenadorUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByText('Comunidade Santo Agostinho').click();
    });

    const alocarButtons = screen.getAllByRole('button', { name: /Alocar/i });
    expect(alocarButtons).toHaveLength(1);
  });

  test('allocates an ativo by selecting an evento from the period filter', async () => {
    const coordenadorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', ativo: true }],
    };
    mockedApiClient.post.mockResolvedValue({ data: { ...mockAtivos[0], status: 'ALOCADO' } });

    await renderAtivos(coordenadorUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByText('Comunidade Santo Agostinho').click();
    });

    await act(async () => {
      screen.getAllByRole('button', { name: /Alocar/i })[0].click();
    });

    expect(mockedApiClient.get).toHaveBeenCalledWith('/eventos', expect.objectContaining({
      params: expect.objectContaining({ organizacaoIds: ['org-1'] }),
    }));

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Evento'));
    });
    await act(async () => {
      screen.getByText(/Encontro de Catequistas/i).click();
    });

    fireEvent.change(screen.getByLabelText('Justificativa de Utilização'), {
      target: { value: 'Uso do projetor no encontro de catequistas' },
    });

    await act(async () => {
      screen.getByRole('button', { name: /^Alocar$/i }).click();
    });

    expect(mockedApiClient.post).toHaveBeenCalledWith('/ativos/ativo-1/alocar', {
      eventoId: 'evento-1',
      justificativaAlocacao: 'Uso do projetor no encontro de catequistas',
    });
    expect(mockShowSuccess).toHaveBeenCalled();
  });
});
