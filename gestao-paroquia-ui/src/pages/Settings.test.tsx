import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { QueryProvider } from '../api/QueryProvider';
import Settings from './Settings';
import { getAnosParoquiais, updateAnoParoquialStatus } from '../api/anosParoquiais';
import { getLocais, createLocal, updateLocal, deleteLocal } from '../api/locais';
import { getOrganizacoes, createOrganizacao, updateOrganizacao, getOrganizacaoMembros } from '../api/organizacoes';
import { getUsuarios, approveUsuario, getMemberships, addMembership, removeMembership } from '../api/usuarios';
import { getPastoraisOficiais, createPastoralOficial, updatePastoralOficial, deletePastoralOficial } from '../api/pastoraisOficiais';
import { MemoryRouter } from 'react-router-dom';
import { NotificationProvider } from '../context/NotificationContext';
import { useAuth } from '../auth/AuthContext';

jest.mock('../api/anosParoquiais', () => ({
  getAnosParoquiais: jest.fn(),
  updateAnoParoquialStatus: jest.fn(),
}));

jest.mock('../api/locais', () => ({
  getLocais: jest.fn(),
  createLocal: jest.fn(),
  updateLocal: jest.fn(),
  deleteLocal: jest.fn(),
}));

jest.mock('../api/organizacoes', () => ({
  getOrganizacoes: jest.fn(),
  createOrganizacao: jest.fn(),
  updateOrganizacao: jest.fn(),
  getOrganizacaoMembros: jest.fn(),
}));

jest.mock('../api/usuarios', () => ({
  getUsuarios: jest.fn(),
  approveUsuario: jest.fn(),
  getMemberships: jest.fn(),
  addMembership: jest.fn(),
  removeMembership: jest.fn(),
}));

jest.mock('../auth/AuthContext', () => ({
  useAuth: jest.fn(),
}));

jest.mock('../api/pastoraisOficiais', () => ({
  getPastoraisOficiais: jest.fn(),
  createPastoralOficial: jest.fn(),
  updatePastoralOficial: jest.fn(),
  deletePastoralOficial: jest.fn(),
}));

const mockGetAnosParoquiais = getAnosParoquiais as jest.Mock;
const mockUpdateAnoParoquialStatus = updateAnoParoquialStatus as jest.Mock;
const mockGetLocais = getLocais as jest.Mock;
const mockCreateLocal = createLocal as jest.Mock;
const mockUpdateLocal = updateLocal as jest.Mock;
const mockDeleteLocal = deleteLocal as jest.Mock;
const mockGetOrganizacoes = getOrganizacoes as jest.Mock;
const mockCreateOrganizacao = createOrganizacao as jest.Mock;
const mockUpdateOrganizacao = updateOrganizacao as jest.Mock;
const mockGetOrganizacaoMembros = getOrganizacaoMembros as jest.Mock;
const mockGetUsuarios = getUsuarios as jest.Mock;
const mockApproveUsuario = approveUsuario as jest.Mock;
const mockGetMemberships = getMemberships as jest.Mock;
const mockAddMembership = addMembership as jest.Mock;
const mockRemoveMembership = removeMembership as jest.Mock;
const mockUseAuth = useAuth as jest.Mock;
const mockGetPastoraisOficiais = getPastoraisOficiais as jest.Mock;
const mockCreatePastoralOficial = createPastoralOficial as jest.Mock;
const mockUpdatePastoralOficial = updatePastoralOficial as jest.Mock;
const mockDeletePastoralOficial = deletePastoralOficial as jest.Mock;

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

const mockLocais = [
  {
    id: 'loc-1',
    nome: 'Igreja Matriz',
    tipo: 'IGREJA',
    capacidade: 400,
    endereco: 'Praça da Matriz, 1',
    caracteristicas: 'Altar, Som, Bancos',
    ativo: true,
  },
  {
    id: 'loc-2',
    nome: 'Salão Paroquial',
    tipo: 'SALAO',
    capacidade: 150,
    endereco: 'Rua Lateral, 50',
    caracteristicas: 'Cozinha, Mesas',
    ativo: false,
  },
];

describe('Settings page - Locais CRUD', () => {
  const mockUseAuth = useAuth as jest.Mock;

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

  test('switches tabs to Locais and renders locations list', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetLocais.mockResolvedValueOnce(mockLocais);

    renderPage();

    const locaisTab = screen.getByRole('tab', { name: /Locais/i });
    fireEvent.click(locaisTab);

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz')).toBeInTheDocument();
      expect(screen.getByText('Salão Paroquial')).toBeInTheDocument();
      expect(screen.getByText('400')).toBeInTheDocument();
      expect(screen.getByText('150')).toBeInTheDocument();
      expect(screen.getByText('Ativo')).toBeInTheDocument();
      expect(screen.getByText('Inativo')).toBeInTheDocument();
    });
  });

  test('allows creating a location via the form modal', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetLocais.mockResolvedValue(mockLocais);
    mockCreateLocal.mockResolvedValueOnce({
      id: 'loc-3',
      nome: 'Sala 1',
      tipo: 'SALA',
      capacidade: 30,
      endereco: '2º Andar',
      caracteristicas: 'Quadro branco',
      ativo: true,
    });

    renderPage();

    const locaisTab = screen.getByRole('tab', { name: /Locais/i });
    fireEvent.click(locaisTab);

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz')).toBeInTheDocument();
    });

    const newBtn = screen.getByRole('button', { name: /Novo Local/i });
    fireEvent.click(newBtn);

    expect(screen.getByRole('heading', { name: 'Novo Local' })).toBeInTheDocument();

    const nomeInput = screen.getByLabelText(/Nome do Local/i);
    const tipoSelect = screen.getByLabelText(/Tipo/i);
    const capacidadeInput = screen.getByLabelText(/Capacidade/i);
    const enderecoInput = screen.getByLabelText(/Endereço/i);

    fireEvent.change(nomeInput, { target: { value: 'Sala 1' } });
    fireEvent.mouseDown(tipoSelect);
    const optionSala = screen.getByRole('option', { name: /Sala/i });
    fireEvent.click(optionSala);
    fireEvent.change(capacidadeInput, { target: { value: '30' } });
    fireEvent.change(enderecoInput, { target: { value: '2º Andar' } });

    const submitBtn = screen.getByRole('button', { name: /Cadastrar/i });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockCreateLocal).toHaveBeenCalledWith({
        nome: 'Sala 1',
        tipo: 'SALA',
        capacidade: 30,
        endereco: '2º Andar',
        caracteristicas: null,
        ativo: true,
      });
    });
  });

  test('allows editing a location via the form modal', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetLocais.mockResolvedValue(mockLocais);
    mockUpdateLocal.mockResolvedValueOnce({
      ...mockLocais[0],
      nome: 'Igreja Matriz Renovada',
    });

    renderPage();

    const locaisTab = screen.getByRole('tab', { name: /Locais/i });
    fireEvent.click(locaisTab);

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz')).toBeInTheDocument();
    });

    const rowMatriz = screen.getAllByRole('row').find(row => row.textContent?.includes('Igreja Matriz'));
    expect(rowMatriz).toBeDefined();

    if (rowMatriz) {
      const editBtn = within(rowMatriz).getByRole('button', { name: /Editar/i });
      fireEvent.click(editBtn);
    }

    expect(screen.getByText('Editar Local')).toBeInTheDocument();
    const nomeInput = screen.getByLabelText(/Nome do Local/i);
    expect(nomeInput).toHaveValue('Igreja Matriz');

    fireEvent.change(nomeInput, { target: { value: 'Igreja Matriz Renovada' } });

    const submitBtn = screen.getByRole('button', { name: /Salvar Alterações/i });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockUpdateLocal).toHaveBeenCalledWith('loc-1', {
        nome: 'Igreja Matriz Renovada',
        tipo: 'IGREJA',
        capacidade: 400,
        endereco: 'Praça da Matriz, 1',
        caracteristicas: 'Altar, Som, Bancos',
        ativo: true,
      });
    });
  });

  test('allows toggling status (inactivating and activating)', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetLocais.mockResolvedValue(mockLocais);
    mockDeleteLocal.mockResolvedValueOnce({});
    mockUpdateLocal.mockResolvedValueOnce({
      ...mockLocais[1],
      ativo: true,
    });

    renderPage();

    const locaisTab = screen.getByRole('tab', { name: /Locais/i });
    fireEvent.click(locaisTab);

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz')).toBeInTheDocument();
    });

    // Test Inactivate
    const rowActive = screen.getAllByRole('row').find(row => row.textContent?.includes('Igreja Matriz'));
    if (rowActive) {
      const toggleBtn = within(rowActive).getByRole('button', { name: /Inativar/i });
      fireEvent.click(toggleBtn);
    }

    await waitFor(() => {
      expect(mockDeleteLocal).toHaveBeenCalledWith('loc-1');
    });

    // Test Activate
    const rowInactive = screen.getAllByRole('row').find(row => row.textContent?.includes('Salão Paroquial'));
    if (rowInactive) {
      const toggleBtn = within(rowInactive).getByRole('button', { name: /Ativar/i });
      fireEvent.click(toggleBtn);
    }

    await waitFor(() => {
      expect(mockUpdateLocal).toHaveBeenCalledWith('loc-2', {
        nome: 'Salão Paroquial',
        tipo: 'SALAO',
        capacidade: 150,
        endereco: 'Rua Lateral, 50',
        caracteristicas: 'Cozinha, Mesas',
        ativo: true,
      });
    });
  });

  test('hides management actions for unauthorized users', async () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '2',
        name: 'Regular Member',
        roles: ['ROLE_PASTORAL_MEMBRO'],
      },
    });
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetLocais.mockResolvedValueOnce(mockLocais);

    renderPage();

    const locaisTab = screen.getByRole('tab', { name: /Locais/i });
    fireEvent.click(locaisTab);

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz')).toBeInTheDocument();
    });

    // Verify "Novo Local" button is not present
    expect(screen.queryByRole('button', { name: /Novo Local/i })).toBeNull();

    // Verify row actions "Editar" and "Inativar"/"Ativar" are not present
    expect(screen.queryByRole('button', { name: /Editar/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /Inativar/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /Ativar/i })).toBeNull();
  });
});

describe('Settings page - Membros / Usuários', () => {
  const mockUseAuth = useAuth as jest.Mock;

  const mockUsers = [
    {
      id: 'user-1',
      username: 'pending@example.com',
      nome: 'João Pending',
      telefone: '11999999999',
      dataNascimento: '1990-05-10',
      enabled: false,
    },
    {
      id: 'user-2',
      username: 'active@example.com',
      nome: 'Maria Active',
      telefone: '11888888888',
      dataNascimento: '1988-10-20',
      enabled: true,
    },
  ];

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

  test('switches tabs to Membros and renders users list', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetUsuarios.mockResolvedValueOnce(mockUsers);

    renderPage();

    const membrosTab = screen.getByRole('tab', { name: /Membros \/ Usuários/i });
    fireEvent.click(membrosTab);

    await waitFor(() => {
      expect(screen.getByText('João Pending')).toBeInTheDocument();
      expect(screen.getByText('Maria Active')).toBeInTheDocument();
      expect(screen.getByText('Pendente')).toBeInTheDocument();
      expect(screen.getByText('Ativo')).toBeInTheDocument();
    });
  });

  test('allows approving a pending user', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetUsuarios.mockResolvedValue(mockUsers);
    mockApproveUsuario.mockResolvedValueOnce({});

    renderPage();

    const membrosTab = screen.getByRole('tab', { name: /Membros \/ Usuários/i });
    fireEvent.click(membrosTab);

    await waitFor(() => {
      expect(screen.getByText('João Pending')).toBeInTheDocument();
    });

    const approveBtn = screen.getByRole('button', { name: /Aprovar/i });
    fireEvent.click(approveBtn);

    await waitFor(() => {
      expect(mockApproveUsuario).toHaveBeenCalledWith('user-1');
    });
  });
});

describe('Settings page - Membros por Pastoral', () => {
  const mockUseAuth = useAuth as jest.Mock;

  const mockOrgs = [
    {
      id: 'org-1',
      nome: 'Pastoral Familiar',
      tipo: 'PASTORAL',
      contato: 'familiar@example.com',
      coordenador: 'Coordenador Mock',
      viceCoordenador: 'Vice Mock',
      ativo: true,
    },
  ];

  const mockOrgMembros = [
    {
      membershipId: 'memb-1',
      usuarioId: 'user-active',
      usuarioNome: 'Maria Membro',
      papel: 'MEMBRO',
      ativo: true,
    },
  ];

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

  test('opens members dialog for selected pastoral and lists members', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetOrganizacoes.mockResolvedValueOnce(mockOrgs);
    mockGetOrganizacaoMembros.mockResolvedValueOnce(mockOrgMembros);

    renderPage();

    const orgsTab = screen.getByRole('tab', { name: /Pastorais e Movimentos/i });
    fireEvent.click(orgsTab);

    await waitFor(() => {
      expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
    });

    const membrosBtn = screen.getByRole('button', { name: /Membros/i });
    fireEvent.click(membrosBtn);

    await waitFor(() => {
      expect(screen.getByText('Membros - Pastoral Familiar')).toBeInTheDocument();
      expect(screen.getByText('Maria Membro')).toBeInTheDocument();
      expect(screen.getByText('MEMBRO')).toBeInTheDocument();
    });
  });

  test('requires igrejaVinculoId when adding membership for pastoral/movimento', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockGetOrganizacoes.mockResolvedValueOnce(mockOrgs);
    mockGetUsuarios.mockResolvedValueOnce([
      { id: 'user-1', nome: 'Fiel Teste', username: 'fiel@test.com', enabled: true }
    ]);
    mockGetOrganizacaoMembros.mockResolvedValueOnce([]);

    renderPage();

    const orgsTab = screen.getByRole('tab', { name: /Pastorais e Movimentos/i });
    fireEvent.click(orgsTab);

    await waitFor(() => {
      expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
    });

    const membrosBtn = screen.getByRole('button', { name: /Membros/i });
    fireEvent.click(membrosBtn);

    await waitFor(() => {
      expect(screen.getByText('Membros - Pastoral Familiar')).toBeInTheDocument();
      expect(screen.getByText('Vincular Novo Membro:')).toBeInTheDocument();
    });

    expect(screen.getByTestId('add-member-church-select')).toBeInTheDocument();
  });

  test('allows editing a pastoral and saves coordinator/vice-coordinator directly', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    const mockChurchesAndPastoral = [
      {
        id: 'pastoral-1',
        nome: 'Catequese',
        tipo: 'PASTORAL',
        contato: 'catequese@example.com',
        coordenador: null,
        viceCoordenador: null,
        ativo: true,
        pastoralOficialId: 'po-1',
      }
    ];
    mockGetOrganizacoes.mockResolvedValue(mockChurchesAndPastoral);
    mockGetUsuarios.mockResolvedValue([]);
    mockGetOrganizacaoMembros.mockResolvedValue([]);
    mockGetPastoraisOficiais.mockResolvedValue([
      { id: 'po-1', nome: 'Catequese Oficial', tipo: 'PASTORAL' }
    ]);

    renderPage();

    const orgsTab = screen.getByRole('tab', { name: /Pastorais e Movimentos/i });
    fireEvent.click(orgsTab);

    await waitFor(() => {
      expect(screen.getByText('Catequese')).toBeInTheDocument();
    });

    const editBtn = screen.getAllByRole('button', { name: /Editar/i })[0];
    fireEvent.click(editBtn);

    await waitFor(() => {
      expect(screen.getByText('Editar Pastoral / Movimento')).toBeInTheDocument();
    });

    // Verify coordinator/vice-coordinator inputs are displayed
    const coordInput = screen.getByLabelText(/^Coordenador$/);
    const viceInput = screen.getByLabelText(/^Vice-Coordenador$/);

    expect(coordInput).toBeInTheDocument();
    expect(viceInput).toBeInTheDocument();

    fireEvent.change(coordInput, { target: { value: 'João Coordinator' } });
    fireEvent.change(viceInput, { target: { value: 'Maria Vice' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar Alterações/i }));

    await waitFor(() => {
      expect(mockUpdateOrganizacao).toHaveBeenCalledWith('pastoral-1', expect.objectContaining({
        nome: 'Catequese',
        coordenador: 'João Coordinator',
        viceCoordenador: 'Maria Vice'
      }));
    });
  });

  test('hides Catálogo Oficial tab for non-admin users', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        name: 'Padre',
        roles: ['ROLE_CLERO_PAROCO'],
        organizations: [],
      },
    });

    renderPage();

    await waitFor(() => {
      expect(screen.queryByRole('tab', { name: /Catálogo Oficial/i })).not.toBeInTheDocument();
    });
  });

  test('renders Catálogo Oficial tab and list for admin users', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        name: 'Administrador',
        roles: ['ROLE_ADMIN'],
        organizations: [],
      },
    });

    const mockPastoraisOficiais = [
      { id: '1', nome: 'Pastoral da Juventude Oficial', tipo: 'PASTORAL' },
      { id: '2', nome: 'Pastoral Familiar Oficial', tipo: 'PASTORAL' },
      { id: '3', nome: 'EJC Oficial', tipo: 'MOVIMENTO' }
    ];
    mockGetPastoraisOficiais.mockResolvedValueOnce(mockPastoraisOficiais);

    renderPage();

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Catálogo Oficial/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('tab', { name: /Catálogo Oficial/i }));

    await waitFor(() => {
      expect(screen.getByText('Pastoral da Juventude Oficial')).toBeInTheDocument();
      expect(screen.getByText('Pastoral Familiar Oficial')).toBeInTheDocument();
      expect(screen.getByText('EJC Oficial')).toBeInTheDocument();
    });
  });

  test('allows creating, editing, and deleting official types in catalog tab', async () => {
    mockGetAnosParoquiais.mockResolvedValueOnce(mockAnos);
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        name: 'Administrador',
        roles: ['ROLE_ADMIN'],
        organizations: [],
      },
    });

    const mockPastoraisOficiais = [
      { id: '1', nome: 'Pastoral da Juventude Oficial', tipo: 'PASTORAL' }
    ];
    mockGetPastoraisOficiais.mockResolvedValue(mockPastoraisOficiais);
    mockCreatePastoralOficial.mockResolvedValueOnce({ id: '2', nome: 'Pastoral Familiar Oficial', tipo: 'PASTORAL' });
    mockUpdatePastoralOficial.mockResolvedValueOnce({ id: '1', nome: 'PJ Oficial Editada', tipo: 'PASTORAL' });
    mockDeletePastoralOficial.mockResolvedValueOnce(undefined);

    window.confirm = jest.fn().mockImplementation(() => true);
    renderPage();

    fireEvent.click(await screen.findByRole('tab', { name: /Catálogo Oficial/i }));

    await waitFor(() => {
      expect(screen.getByText('Pastoral da Juventude Oficial')).toBeInTheDocument();
    });

    // Create
    fireEvent.click(screen.getByRole('button', { name: /Novo Tipo/i }));
    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'Pastoral Familiar Oficial' } });
    
    // Select type PASTORAL
    const tipoSelect = screen.getByLabelText(/^Tipo\s*\*?$/);
    fireEvent.mouseDown(tipoSelect);
    fireEvent.click(screen.getByRole('option', { name: 'Pastoral' }));

    fireEvent.click(screen.getByRole('button', { name: /Salvar/i }));

    await waitFor(() => {
      expect(mockCreatePastoralOficial).toHaveBeenCalledWith({
        nome: 'Pastoral Familiar Oficial',
        tipo: 'PASTORAL'
      });
    });

    // Wait for creation dialog to close and aria-hidden to clear
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    // Edit
    const catalogPanel = document.getElementById('settings-tabpanel-4') as HTMLElement;
    const editBtn = within(catalogPanel).getByRole('button', { name: /Editar/i });
    fireEvent.click(editBtn);
    fireEvent.change(screen.getByLabelText(/Nome/i), { target: { value: 'PJ Oficial Editada' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar/i }));

    await waitFor(() => {
      expect(mockUpdatePastoralOficial).toHaveBeenCalledWith('1', {
        nome: 'PJ Oficial Editada',
        tipo: 'PASTORAL'
      });
    });

    // Wait for edit dialog to close and aria-hidden to clear
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    // Delete
    const deleteBtn = within(catalogPanel).getByRole('button', { name: /Excluir/i });
    fireEvent.click(deleteBtn);

    await waitFor(() => {
      expect(mockDeletePastoralOficial).toHaveBeenCalledWith('1');
    });
  });
});
