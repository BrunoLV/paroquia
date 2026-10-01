import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Configuracoes from './Configuracoes';
import * as anosApi from '@/contextos/governanca/infrastructure/http/anosParoquiais';
import * as usuariosApi from '@/contextos/iam/infrastructure/http/usuarios';
import * as organizacoesApi from '@/contextos/organizacao/infrastructure/http/organizacoes';
import * as pastoraisOficiaisApi from '@/contextos/organizacao/infrastructure/http/pastoraisOficiais';
import * as fieisApi from '@/contextos/fiel/infrastructure/http/fieis';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

let mockCurrentUser = {
  id: '1',
  name: 'Administrador',
  roles: ['ROLE_ADMIN', 'ADMIN'],
  organizations: [],
};

vi.mock('@/contextos/iam/presentation/auth/AuthContext', () => ({
  useAuth: () => ({
    user: mockCurrentUser,
    isAuthenticated: true,
  }),
}));

vi.mock('@/contextos/governanca/infrastructure/http/anosParoquiais');
vi.mock('@/contextos/iam/infrastructure/http/usuarios');
vi.mock('@/contextos/organizacao/infrastructure/http/organizacoes');
vi.mock('@/contextos/organizacao/infrastructure/http/pastoraisOficiais');
vi.mock('@/contextos/fiel/infrastructure/http/fieis');

const mockAnos = [
  {
    ano: 2026,
    status: 'EM_PLANEJAMENTO' as const,
    dataFechamentoUtc: null,
    updatedAt: '2026-05-31T20:00:00Z',
  },
  {
    ano: 2025,
    status: 'CONCLUIDO' as const,
    dataFechamentoUtc: '2025-12-31T23:59:59Z',
    updatedAt: '2025-12-31T23:59:59Z',
  },
];

const mockUsersData = [
  {
    id: 'user-1',
    nome: 'João Usuário',
    username: 'joao@paroquia.com',
    telefone: '(11) 99999-9999',
    dataNascimento: '1990-01-01',
    enabled: false,
    batizado: true,
    primeiraComunhao: true,
    crismado: true,
    casadoIgrejaCatolica: false,
  },
];

const mockCatalogData = [
  {
    id: 'po-1',
    nome: 'Pastoral da Juventude Oficial',
    tipo: 'PASTORAL' as const,
  },
];

describe('Configuracoes page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockCurrentUser = {
      id: '1',
      name: 'Administrador',
      roles: ['ROLE_ADMIN', 'ADMIN'],
      organizations: [],
    };
    vi.spyOn(anosApi, 'getAnosParoquiais').mockResolvedValue(mockAnos as any);
    vi.spyOn(usuariosApi, 'getUsuarios').mockResolvedValue(mockUsersData as any);
    vi.spyOn(organizacoesApi, 'getOrganizacoes').mockResolvedValue([]);
    vi.spyOn(pastoraisOficiaisApi, 'getPastoraisOficiais').mockResolvedValue(mockCatalogData as any);
  });

  const renderPage = () => {
    return render(
      <QueryProvider>
        <NotificationProvider>
          <MemoryRouter>
            <Configuracoes />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );
  };

  it('renders page title, tabs, and default Ano Paroquial list', async () => {
    renderPage();

    expect(screen.getByText('Configurações do Sistema')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Ano Paroquial/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Membros \/ Usuários/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Catálogo Oficial/i })).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument();
      expect(screen.getByText('2025')).toBeInTheDocument();
    });
  });

  it('calls update status when Fechar Ano button is clicked by authorized user (PAROCO)', async () => {
    mockCurrentUser = {
      id: '1',
      name: 'Pároco',
      roles: ['PAROCO'],
      organizations: [],
    };
    const updateSpy = vi.spyOn(anosApi, 'updateAnoParoquialStatus').mockResolvedValueOnce({
      ano: 2026,
      status: 'FECHADO',
      dataFechamentoUtc: '2026-05-31T23:00:00Z',
      updatedAt: '2026-05-31T23:00:00Z',
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument();
    });

    const fecharBtn = screen.getByRole('button', { name: /Fechar Ano/i });
    fireEvent.click(fecharBtn);

    await waitFor(() => {
      expect(updateSpy).toHaveBeenCalledWith(2026, 'FECHADO');
    });
  });

  it('switches tabs to Membros e Usuários and renders users list', async () => {
    renderPage();

    const usersTab = screen.getByRole('tab', { name: /Membros \/ Usuários/i });
    fireEvent.click(usersTab);

    await waitFor(() => {
      expect(screen.getByText('João Usuário')).toBeInTheDocument();
      expect(screen.getByText('joao@paroquia.com')).toBeInTheDocument();
    });
  });

  it('allows approving a pending user', async () => {
    const approveSpy = vi.spyOn(usuariosApi, 'approveUsuario').mockResolvedValueOnce(undefined as any);

    renderPage();

    const usersTab = screen.getByRole('tab', { name: /Membros \/ Usuários/i });
    fireEvent.click(usersTab);

    await waitFor(() => {
      expect(screen.getByText('João Usuário')).toBeInTheDocument();
    });

    const actionBtn = screen.getByRole('button', { name: /Ações/i });
    fireEvent.click(actionBtn);

    const aprovarMenuItem = await screen.findByRole('menuitem', { name: /Aprovar/i });
    fireEvent.click(aprovarMenuItem);

    await waitFor(() => {
      expect(approveSpy).toHaveBeenCalledWith('user-1');
    });
  });

  it('switches to Catálogo Oficial tab and allows creating a new type', async () => {
    const createSpy = vi.spyOn(pastoraisOficiaisApi, 'createPastoralOficial').mockResolvedValueOnce({
      id: 'po-2',
      nome: 'Pastoral da Saúde',
      tipo: 'PASTORAL',
    } as any);

    renderPage();

    const catalogTab = screen.getByRole('tab', { name: /Catálogo Oficial/i });
    fireEvent.click(catalogTab);

    await waitFor(() => {
      expect(screen.getByText('Pastoral da Juventude Oficial')).toBeInTheDocument();
    });

    const novoBtn = screen.getByRole('button', { name: /Novo Tipo/i });
    fireEvent.click(novoBtn);

    await waitFor(() => {
      expect(screen.getByText('Novo Tipo Oficial')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/Nome do Tipo Oficial/i), { target: { value: 'Pastoral da Saúde' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar/i }));

    await waitFor(() => {
      expect(createSpy).toHaveBeenCalled();
    });
  });

  it('hides Catálogo Oficial tab for non-admin users', async () => {
    mockCurrentUser = {
      id: '2',
      name: 'Membro',
      roles: ['MEMBRO_ATIVO'],
      organizations: [],
    };

    renderPage();

    expect(screen.queryByRole('tab', { name: /Catálogo Oficial/i })).not.toBeInTheDocument();
  });

  it('allows opening user registration modal and selecting a fiel with fielId and cpf', async () => {
    vi.spyOn(fieisApi, 'getFieis').mockResolvedValueOnce([
      {
        fielId: 'fiel-123',
        nome: 'Maria da Silva',
        dadosIdentificacao: { cpfFormatado: '123.456.789-00' },
        contato: { email: 'maria.silva@paroquia.com' },
      } as any,
    ]);
    const createSpy = vi.spyOn(usuariosApi, 'createUsuario').mockResolvedValueOnce({
      id: 'usr-new',
      username: 'maria.silva@paroquia.com',
      enabled: true,
    } as any);

    renderPage();

    const usersTab = screen.getByRole('tab', { name: /Membros \/ Usuários/i });
    fireEvent.click(usersTab);

    const cadastrarBtn = await screen.findByRole('button', { name: /Cadastrar Usuário/i });
    fireEvent.click(cadastrarBtn);

    await waitFor(() => {
      expect(screen.getByText('Cadastrar Novo Usuário do Sistema')).toBeInTheDocument();
    });

    const autocomplete = screen.getByTestId('admin-reg-fiel-autocomplete');
    fireEvent.focus(autocomplete);
    fireEvent.change(autocomplete, { target: { value: 'Maria' } });

    const option = await screen.findByRole('option', { name: /Maria da Silva/i });
    fireEvent.click(option);

    await waitFor(() => {
      expect(screen.getByDisplayValue('maria.silva@paroquia.com')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/Senha de Acesso/i), { target: { value: 'Senha@123' } });

    const submitBtn = screen.getByRole('button', { name: /^Cadastrar$/i });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(createSpy).toHaveBeenCalledWith({
        fielId: 'fiel-123',
        username: 'maria.silva@paroquia.com',
        password: 'Senha@123',
      });
    });
  });
});
