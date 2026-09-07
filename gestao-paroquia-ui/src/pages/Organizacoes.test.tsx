import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Organizacoes from './Organizacoes';
import * as organizacoesApi from '@/api/organizacoes';
import * as usuariosApi from '@/api/usuarios';
import * as pastoraisOficiaisApi from '@/api/pastoraisOficiais';
import { NotificationProvider } from '@/context/NotificationContext';
import { QueryProvider } from '@/api/QueryProvider';

let mockCurrentUser = {
  id: '1',
  name: 'Padre',
  roles: ['ROLE_ADMIN', 'ADMIN'],
  organizations: [],
};

vi.mock('@/auth/AuthContext', () => ({
  useAuth: () => ({
    user: mockCurrentUser,
    isAuthenticated: true,
  }),
}));

vi.mock('@/api/organizacoes');
vi.mock('@/api/usuarios');
vi.mock('@/api/pastoraisOficiais');

const mockOrgsData = [
  {
    id: 'org-1',
    nome: 'Pastoral Familiar',
    tipo: 'PASTORAL',
    coordenador: 'João Silva',
    viceCoordenador: 'Maria Silva',
    contato: 'familiar@paroquia.com',
    ativo: true,
  },
  {
    id: 'org-2',
    nome: 'Encontro de Casais com Cristo (ECC)',
    tipo: 'MOVIMENTO',
    coordenador: 'Carlos Souza',
    viceCoordenador: 'Ana Souza',
    contato: 'ecc@paroquia.com',
    ativo: false,
  },
  {
    id: 'org-matriz',
    nome: 'Matriz N. S. de Fátima',
    tipo: 'MATRIZ',
    ativo: true,
  },
];

const mockMembrosData = [
  {
    vinculoId: 'v-1',
    usuarioId: 'u-1',
    usuarioNome: 'Maria Membro',
    papel: 'MEMBRO',
    ativo: true,
    igrejaVinculoId: 'org-matriz',
  },
];

describe('Organizacoes page (Pastorais e Movimentos)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockCurrentUser = {
      id: '1',
      name: 'Padre',
      roles: ['ROLE_ADMIN', 'ADMIN'],
      organizations: [],
    };
    vi.spyOn(organizacoesApi, 'getOrganizacoes').mockResolvedValue(mockOrgsData as any);
    vi.spyOn(organizacoesApi, 'getOrganizacaoMembros').mockResolvedValue(mockMembrosData as any);
    vi.spyOn(usuariosApi, 'getUsuarios').mockResolvedValue([
      { id: 'u-1', nome: 'Maria Membro', username: 'maria@test.com', enabled: true },
      { id: 'u-2', nome: 'José Novo', username: 'jose@test.com', enabled: true },
    ] as any);
    vi.spyOn(pastoraisOficiaisApi, 'getPastoraisOficiais').mockResolvedValue([]);
  });

  const renderPage = () => {
    return render(
      <QueryProvider>
        <NotificationProvider>
          <MemoryRouter>
            <Organizacoes />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );
  };

  it('renders page title, subtitle, filter bar and pastorais table', async () => {
    renderPage();

    expect(screen.getByText('Pastorais e Movimentos')).toBeInTheDocument();
    expect(screen.getByText('Gerencie pastorais, movimentos, lideranças e vínculos.')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
      expect(screen.getByText('Encontro de Casais com Cristo (ECC)')).toBeInTheDocument();
      expect(screen.getByText('João Silva')).toBeInTheDocument();
      expect(screen.getByText('Carlos Souza')).toBeInTheDocument();
    });
  });

  it('filters pastorais by search text', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Buscar por nome, coordenador ou tipo.../i);
    fireEvent.change(searchInput, { target: { value: 'ECC' } });

    await waitFor(() => {
      expect(screen.queryByText('Pastoral Familiar')).not.toBeInTheDocument();
      expect(screen.getByText('Encontro de Casais com Cristo (ECC)')).toBeInTheDocument();
    });
  });

  it('opens members dialog for selected pastoral and lists members', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
    });

    const actionButtons = screen.getAllByRole('button', { name: /Ações/i });
    fireEvent.click(actionButtons[0]);

    const membrosMenuItem = await screen.findByRole('menuitem', { name: /Membros/i });
    fireEvent.click(membrosMenuItem);

    await waitFor(() => {
      expect(screen.getByText('Membros - Pastoral Familiar')).toBeInTheDocument();
      expect(screen.getByText('Maria Membro')).toBeInTheDocument();
    });
  });

  it('allows toggling status (inactivating and activating)', async () => {
    const deleteSpy = vi.spyOn(organizacoesApi, 'deleteOrganizacao').mockResolvedValueOnce(undefined);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
    });

    const actionButtons = screen.getAllByRole('button', { name: /Ações/i });
    fireEvent.click(actionButtons[0]);

    const inativarMenuItem = await screen.findByRole('menuitem', { name: /Inativar/i });
    fireEvent.click(inativarMenuItem);

    await waitFor(() => {
      expect(deleteSpy).toHaveBeenCalledWith('org-1');
    });
  });

  it('hides Nova Pastoral button for unauthorized users', async () => {
    mockCurrentUser = {
      id: '2',
      name: 'Membro',
      roles: ['ROLE_MEMBRO'],
      organizations: [],
    };

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Pastoral Familiar')).toBeInTheDocument();
    });

    expect(screen.queryByRole('button', { name: /Nova Pastoral \/ Movimento/i })).not.toBeInTheDocument();
  });
});
