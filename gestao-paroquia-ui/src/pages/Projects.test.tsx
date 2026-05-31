import React from 'react';
import { render, screen, act, fireEvent, within } from '@testing-library/react';
import Projects from './Projects';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';
import apiClient from '../api/client';
import { AuthProvider } from '../auth/AuthContext';

jest.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const mockShowError = jest.fn();
const mockShowSuccess = jest.fn();
jest.mock('../context/NotificationContext', () => ({
  useNotification: () => ({
    showError: mockShowError,
    showSuccess: mockShowSuccess,
  }),
}));

describe('Projects Page', () => {
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
      organizacaoResponsavelId: 'org-1',
      status: 'ATIVO'
    },
    {
      id: 'proj-2',
      nome: 'Missão Jovem',
      descricao: 'Encontro de jovens',
      inicio: '2026-06-01T00:00:00Z',
      fim: '2026-06-15T00:00:00Z',
      organizacaoResponsavelId: 'org-2',
      status: 'INATIVO'
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
              <Projects />
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
    expect(screen.getByText('35.5%')).toBeInTheDocument(); // percentualTempoDecorrido
    expect(screen.getByText('10')).toBeInTheDocument(); // totalEventos
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
});
