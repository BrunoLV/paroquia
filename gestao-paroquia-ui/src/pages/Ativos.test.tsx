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
    { id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true },
    { id: 'org-2', nome: 'Matriz', tipo: 'MATRIZ', ativo: true },
    { id: 'org-3', nome: 'Pastoral da Juventude', tipo: 'PASTORAL', ativo: true },
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
    organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
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
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    expect(screen.getByText('TV 55 polegadas')).toBeInTheDocument();
    expect(screen.getByText('Projetor Epson')).toBeInTheDocument();
  });

  test('only offers Matriz/Comunidade organizations in the top organization selector', async () => {
    await renderAtivos(adminUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });

    const options = screen.getAllByRole('option');
    const optionLabels = options.map((option) => option.textContent);
    expect(optionLabels).toContain('Comunidade Santo Agostinho');
    expect(optionLabels).toContain('Matriz');
    expect(optionLabels).not.toContain('Pastoral da Juventude');
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
      organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
    };
    await renderAtivos(coordenadorUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    expect(screen.getByRole('menuitem', { name: /^Alocar$/i })).toBeInTheDocument();
    await act(async () => {
      fireEvent.keyDown(document.activeElement || document.body, { key: 'Escape' });
    });

    await act(async () => {
      screen.getAllByLabelText('Ações')[1].click();
    });
    expect(screen.queryByRole('menuitem', { name: /^Alocar$/i })).not.toBeInTheDocument();
  });

  test('allocates an ativo by selecting an evento from the period filter', async () => {
    const coordenadorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
    };
    mockedApiClient.post.mockResolvedValue({ data: { ...mockAtivos[0], status: 'ALOCADO' } });

    await renderAtivos(coordenadorUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    await act(async () => {
      screen.getByRole('menuitem', { name: /^Alocar$/i }).click();
    });

    expect(mockedApiClient.get).toHaveBeenCalledWith('/eventos', expect.objectContaining({
      params: expect.objectContaining({ dataInicio: expect.any(String), dataFim: expect.any(String) }),
    }));

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText(/Evento/i));
    });
    await act(async () => {
      screen.getByText(/Encontro de Catequistas/i).click();
    });

    fireEvent.change(screen.getByLabelText(/Justificativa de Utilização/), {
      target: { value: 'Uso do projetor no encontro de catequistas' },
    });

    await act(async () => {
      screen.getByRole('button', { name: /Alocar Ativo/i }).click();
    });

    expect(mockedApiClient.post).toHaveBeenCalledWith('/ativos/ativo-1/alocar', expect.objectContaining({
      eventoId: 'evento-1',
      justificativaAlocacao: 'Uso do projetor no encontro de catequistas',
      dataInicio: expect.any(String),
      dataFim: expect.any(String),
    }));
    expect(mockShowSuccess).toHaveBeenCalled();
  });

  test('shows pending-approval message when allocation returns 202 (empréstimo)', async () => {
    const coordenadorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
    };
    mockedApiClient.post.mockResolvedValue({
      status: 202,
      data: { solicitacaoAprovacaoId: 'aprov-1', status: 'PENDENTE', ativoId: 'ativo-1', eventoId: 'evento-1' },
    });

    await renderAtivos(coordenadorUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    await act(async () => {
      screen.getByRole('menuitem', { name: /^Alocar$/i }).click();
    });

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText(/Evento/i));
    });
    await act(async () => {
      screen.getByText(/Encontro de Catequistas/i).click();
    });

    fireEvent.change(screen.getByLabelText(/Justificativa de Utilização/), {
      target: { value: 'Uso do projetor no encontro de catequistas' },
    });

    await act(async () => {
      screen.getByRole('button', { name: /Alocar Ativo/i }).click();
    });

    expect(mockShowSuccess).toHaveBeenCalledWith(expect.stringMatching(/aguardando aprova/i));
  });

  test('allocates/reserves an ativo without attaching an evento, sending eventoId null', async () => {
    const coordenadorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
    };
    mockedApiClient.post.mockResolvedValue({ data: { ...mockAtivos[0], status: 'ALOCADO' } });

    await renderAtivos(coordenadorUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    await act(async () => {
      screen.getByRole('menuitem', { name: /^Alocar$/i }).click();
    });

    fireEvent.change(screen.getByLabelText(/Justificativa de Utilização/), {
      target: { value: 'Reserva para uso futuro sem evento definido' },
    });

    await act(async () => {
      screen.getByRole('button', { name: /Alocar Ativo/i }).click();
    });

    expect(mockedApiClient.post).toHaveBeenCalledWith('/ativos/ativo-1/alocar', expect.objectContaining({
      eventoId: null,
      justificativaAlocacao: 'Reserva para uso futuro sem evento definido',
    }));
    expect(mockShowSuccess).toHaveBeenCalled();
  });

  test('renders the Valor Estimado column formatted as BRL currency', async () => {
    await renderAtivos(adminUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    expect(screen.getByText('R$ 3.500,00')).toBeInTheDocument();
    expect(screen.getByText('R$ 2.000,00')).toBeInTheDocument();
  });

  test('only offers Matriz/Comunidade organizations as asset owner in the create dialog', async () => {
    await renderAtivos(adminUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    await act(async () => {
      screen.getByRole('button', { name: /Novo Ativo/i }).click();
    });

    await screen.findByText('Novo Ativo Patrimonial');
    const donaLabel = await screen.findByLabelText(/Organização Dona/i);
    await act(async () => {
      fireEvent.mouseDown(donaLabel);
    });

    const options = screen.getAllByRole('option');
    const optionLabels = options.map((option) => option.textContent);
    expect(optionLabels).toContain('Comunidade Santo Agostinho');
    expect(optionLabels).toContain('Matriz');
  });

  test('renders Desalocar and Iniciar Manutenção menu items with semantic icons', async () => {
    const coordenadorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Comunidade Santo Agostinho', tipo: 'COMUNIDADE', ativo: true }],
    };
    await renderAtivos(coordenadorUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    // ativo[0] is DISPONIVEL: expect "Iniciar Manutenção" with icon
    await act(async () => {
      screen.getAllByLabelText('Ações')[0].click();
    });
    const manutencaoItem = screen.getByRole('menuitem', { name: /Iniciar Manutenção/i });
    expect(manutencaoItem.querySelector('[data-testid="BuildIcon"]')).toBeInTheDocument();
    await act(async () => {
      fireEvent.keyDown(document.activeElement || document.body, { key: 'Escape' });
    });

    // ativo[1] is ALOCADO: expect "Desalocar" with icon
    await act(async () => {
      screen.getAllByLabelText('Ações')[1].click();
    });
    const desalocarItem = screen.getByRole('menuitem', { name: /Desalocar/i });
    expect(desalocarItem.querySelector('[data-testid="EventBusyIcon"]')).toBeInTheDocument();
  });

  test('renders create dialog Cancelar and Salvar buttons with semantic icons', async () => {
    await renderAtivos(adminUser);

    await act(async () => {
      fireEvent.mouseDown(screen.getByLabelText('Organização'));
    });
    await act(async () => {
      screen.getByRole('option', { name: 'Comunidade Santo Agostinho' }).click();
    });

    await act(async () => {
      screen.getByRole('button', { name: /Novo Ativo/i }).click();
    });

    await screen.findByText('Novo Ativo Patrimonial');
    const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
    const saveButton = screen.getByRole('button', { name: /Salvar Ativo/i });
    expect(cancelButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
    expect(saveButton.querySelector('[data-testid="SaveIcon"]')).toBeInTheDocument();
  });
});
