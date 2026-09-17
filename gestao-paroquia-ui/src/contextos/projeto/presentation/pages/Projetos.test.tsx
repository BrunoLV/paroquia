import {vi} from "vitest";
import React from 'react';
import {act, fireEvent, render, screen, waitFor, within} from '@testing-library/react';
import Projetos from './Projetos';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/shared/presentation/theme/theme';
import {MemoryRouter} from 'react-router-dom';
import apiClient from '@/shared/infrastructure/http/client';
import {AuthProvider} from '@/contextos/iam/presentation/auth/AuthContext';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = jest.fn();
const mockShowSuccess = jest.fn();
vi.mock('@/shared/presentation/context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
    showSuccess: mockShowSuccess,
  }),
}));

describe('Projetos Page', () => {
  const mockOrgs = [
    { id: 'org-1', nome: 'Pastoral Familiar', ativo: true },
    { id: 'org-2', nome: 'Pastoral da Juventude', ativo: true }
  ];

  const mockProjects = [
    {
      id: 'proj-1',
      nome: 'Reforma do Salão',
      descricao: 'Projeto de reforma estrutural',
      inicio: '2026-05-01T00:00:00Z',
      fim: '2026-08-30T00:00:00Z',
      grupoParoquialResponsavelId: 'org-1',
      status: 'EM_ANDAMENTO'
    },
    {
      id: 'proj-2',
      nome: 'Missão Jovem',
      descricao: 'Encontro de jovens',
      inicio: '2026-06-01T00:00:00Z',
      fim: '2026-06-15T00:00:00Z',
      grupoParoquialResponsavelId: 'org-2',
      status: 'CONCLUIDO'
    }
  ];

  const mockResumo = {
    statusExecucao: {
      totalEventos: 10,
      eventosRealizados: 4,
      eventosPendentes: 6
    },
    mapaColaboracao: {
      envolvidos: ['Pastoral Familiar', 'Pastoral da Juventude']
    },
    saudeTemporal: {
      percentualTempoDecorrido: 35.5,
      emRisco: false
    }
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockShowError.mockClear();
    mockShowSuccess.mockClear();
  });

  const renderProjects = async (user: any) => {
    mockedApiClient.get.mockImplementation((url) => {
      if (url === '/auth/me') {
        return Promise.resolve({ data: user });
      }
      if (url === '/organizacoes') {
        return Promise.resolve({ data: mockOrgs });
      }
      if (url === '/projetos') {
        return Promise.resolve({ data: mockProjects });
      }
      if (url.startsWith('/projetos/') && url.endsWith('/resumo')) {
        return Promise.resolve({ data: mockResumo });
      }
      return Promise.reject(new Error('Not found'));
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <ThemeProvider theme={theme}>
              <QueryProvider>
                <Projetos />
              </QueryProvider>
            </ThemeProvider>
          </AuthProvider>
        </MemoryRouter>
      );
    });
  };

  test('renders page heading and projects list', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderProjects(adminUser);

    expect(screen.getByRole('heading', { name: /Projetos Paroquiais/i })).toBeInTheDocument();
    expect(screen.getByText('Reforma do Salão')).toBeInTheDocument();
    expect(screen.getByText('Missão Jovem')).toBeInTheDocument();
    expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
  });

  test('opens details view showing metrics on project click', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderProjects(adminUser);

    const projectRow = screen.getByText('Reforma do Salão');
    await act(async () => {
      projectRow.click();
    });

    // Verify indicators fetch call
    expect(mockedApiClient.get).toHaveBeenCalledWith('/projetos/proj-1/resumo');
    
    // Check elements in Details Drawer
    expect(screen.getByText('Detalhes do Projeto')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('35.5%')).toBeInTheDocument(); // percentualTempoDecorrido
    });
    expect(screen.getAllByText('10').length).toBeGreaterThan(0); // totalEventos e TablePagination
    expect(screen.getByText('4')).toBeInTheDocument(); // eventosRealizados
  });

  test('shows Novo Projeto button only for authorized users', async () => {
    // 1. Non-authorized user (regular member)
    const regularMember = {
      id: '2',
      name: 'Membro',
      roles: ['ROLE_PASTORAL_MEMBRO'],
      organizations: [{ id: 'org-1', nome: 'Pastoral Familiar', ativo: true }]
    };
    await renderProjects(regularMember);
    expect(screen.queryByRole('button', { name: /Novo Projeto/i })).not.toBeInTheDocument();

    // 2. Authorized user (Coordinator)
    jest.clearAllMocks();
    const coordinatorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Pastoral Familiar', ativo: true }]
    };
    await renderProjects(coordinatorUser);
    expect(screen.getByRole('button', { name: /Novo Projeto/i })).toBeInTheDocument();
  });

  test('locks organization select to user organizations for local coordinators', async () => {
    const coordinatorUser = {
      id: '3',
      name: 'Coordenador',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Pastoral Familiar', ativo: true }]
    };
    await renderProjects(coordinatorUser);

    const newProjectBtn = screen.getByRole('button', { name: /Novo Projeto/i });
    await act(async () => {
      newProjectBtn.click();
    });

    expect(screen.getByText(/Cadastrar Projeto/i)).toBeInTheDocument();

    // Verify select options contains only user organizations (Pastoral Familiar)
    const select = screen.getByLabelText(/Organização Responsável/i);
    fireEvent.mouseDown(select);

    const listbox = screen.getByRole('listbox');
    expect(within(listbox).getByText('Pastoral Familiar')).toBeInTheDocument();
    expect(within(listbox).queryByText('Pastoral da Juventude')).not.toBeInTheDocument();
  });

  test('renders create dialog Cancelar and Salvar buttons with semantic icons', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderProjects(adminUser);

    const newProjectBtn = screen.getByRole('button', { name: /Novo Projeto/i });
    await act(async () => {
      newProjectBtn.click();
    });

    const cancelButton = screen.getByRole('button', { name: /^Cancelar$/i });
    const saveButton = screen.getByRole('button', { name: /^Salvar$/i });
    expect(cancelButton.querySelector('[data-testid="CloseIcon"]')).toBeInTheDocument();
    expect(saveButton.querySelector('[data-testid="SaveIcon"]')).toBeInTheDocument();
  });

  test('shows error notification when save project fails', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderProjects(adminUser);

    const newProjectBtn = screen.getByRole('button', { name: /Novo Projeto/i });
    await act(async () => {
      newProjectBtn.click();
    });

    // Mock apiClient.post to throw error
    mockedApiClient.post.mockRejectedValueOnce({
      response: {
        data: {
          message: 'Erro de validação de negócios específico do backend'
        }
      }
    });

    // Fill project fields
    fireEvent.change(screen.getByLabelText(/Nome do Projeto/i), { target: { value: 'Projeto Teste Erro' } });
    fireEvent.change(screen.getByLabelText(/Descrição/i), { target: { value: 'Desc' } });
    
    // Select org
    const orgSelect = screen.getByLabelText(/Organização Responsável/i);
    fireEvent.mouseDown(orgSelect);
    const listbox = screen.getByRole('listbox');
    fireEvent.click(within(listbox).getByText('Pastoral Familiar'));

    // Input dates
    fireEvent.change(screen.getByLabelText(/Data de Início/i), { target: { value: '2026-05-01' } });
    fireEvent.change(screen.getByLabelText(/Data de Término/i), { target: { value: '2026-08-30' } });

    const salvarBtn = screen.getByRole('button', { name: /Salvar/i });
    await act(async () => {
      salvarBtn.click();
    });

    expect(mockShowError).toHaveBeenCalledWith('Erro de validação de negócios específico do backend');
  });

  test('shows client-side validation error when project end date is before start date', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderProjects(adminUser);

    const newProjectBtn = screen.getByRole('button', { name: /Novo Projeto/i });
    await act(async () => {
      newProjectBtn.click();
    });

    // Fill fields
    fireEvent.change(screen.getByLabelText(/Nome do Projeto/i), { target: { value: 'Projeto Invalido' } });
    
    // Select org
    const orgSelect = screen.getByLabelText(/Organização Responsável/i);
    fireEvent.mouseDown(orgSelect);
    const listbox = screen.getByRole('listbox');
    fireEvent.click(within(listbox).getByText('Pastoral Familiar'));

    // Input invalid dates (end before start)
    fireEvent.change(screen.getByLabelText(/Data de Início/i), { target: { value: '2026-05-10' } });
    fireEvent.change(screen.getByLabelText(/Data de Término/i), { target: { value: '2026-05-01' } });

    const salvarBtn = screen.getByRole('button', { name: /Salvar/i });
    await act(async () => {
      salvarBtn.click();
    });

    expect(mockShowError).toHaveBeenCalledWith('A data de início não pode ser posterior à data de término.');
  });

  test('shows validation error when submitting empty project form', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderProjects(adminUser);

    const newProjectBtn = screen.getByRole('button', { name: /Novo Projeto/i });
    await act(async () => {
      newProjectBtn.click();
    });

    // Clear inputs to trigger empty validation
    fireEvent.change(screen.getByLabelText(/Nome do Projeto/i), { target: { value: '' } });
    fireEvent.change(screen.getByLabelText(/Data de Início/i), { target: { value: '' } });
    fireEvent.change(screen.getByLabelText(/Data de Término/i), { target: { value: '' } });

    const salvarBtn = screen.getByRole('button', { name: /Salvar/i });
    await act(async () => {
      salvarBtn.click();
    });

    expect(mockShowError).toHaveBeenCalledWith('O nome do projeto é obrigatório.');
  });

  test('renders action buttons in details drawer and handles status transition', async () => {
    const adminUser = { id: '1', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    await renderProjects(adminUser);

    const projectRow = screen.getByText('Reforma do Salão');
    await act(async () => {
      projectRow.click();
    });

    // Drawer is open for proj-1 (status: EM_ANDAMENTO)
    expect(screen.getByRole('button', { name: /^Concluir Projeto$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Cancelar Projeto$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Editar$/i })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /^Fechar$/i }).length).toBeGreaterThan(0);

    mockedApiClient.post.mockResolvedValueOnce({ data: { ...mockProjects[0], status: 'CONCLUIDO' } });

    // Open transition dialog
    const concluirBtn = screen.getByRole('button', { name: /^Concluir Projeto$/i });
    await act(async () => {
      concluirBtn.click();
    });

    expect(screen.getByText(/Tem certeza de que deseja concluir este projeto\?/i)).toBeInTheDocument();

    const confirmBtn = screen.getByRole('button', { name: /^Concluir Projeto$/i });
    await act(async () => {
      confirmBtn.click();
    });

    expect(mockedApiClient.post).toHaveBeenCalledWith('/projetos/proj-1/concluir');
    expect(mockShowSuccess).toHaveBeenCalledWith('Projeto concluído com sucesso!');
  });
});

