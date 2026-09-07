import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Settings from './Settings';
import * as anosApi from '@/api/anosParoquiais';
import * as usuariosApi from '@/api/usuarios';
import * as organizacoesApi from '@/api/organizacoes';
import * as pastoraisOficiaisApi from '@/api/pastoraisOficiais';
import { NotificationProvider } from '@/context/NotificationContext';
import { QueryProvider } from '@/api/QueryProvider';

let mockCurrentUser = {
  id: '1',
  name: 'Administrador',
  roles: ['ROLE_ADMIN', 'ADMIN'],
  organizations: [],
};

vi.mock('@/auth/AuthContext', () => ({
  useAuth: () => ({
    user: mockCurrentUser,
    isAuthenticated: true,
  }),
}));

vi.mock('@/api/anosParoquiais');
vi.mock('@/api/usuarios');
vi.mock('@/api/organizacoes');
vi.mock('@/api/pastoraisOficiais');

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

describe('Settings page', () => {
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
            <Settings />
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

  it('calls update status when Fechar Ano button is clicked by authorized user', async () => {
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
      roles: ['ROLE_MEMBRO'],
      organizations: [],
    };

    renderPage();

    expect(screen.queryByRole('tab', { name: /Catálogo Oficial/i })).not.toBeInTheDocument();
  });
});
