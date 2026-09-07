import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Locais from './Locais';
import * as locaisApi from '@/api/locais';
import * as organizacoesApi from '@/api/organizacoes';
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

vi.mock('@/api/locais');
vi.mock('@/api/organizacoes');

const mockLocaisData = [
  {
    id: 'loc-1',
    nome: 'Salão Paroquial São José',
    tipo: 'SALAO_PAROQUIAL',
    capacidade: 200,
    organizacaoNome: 'Matriz N. S. de Fátima',
    endereco: 'Rua das Flores, 123',
    ativo: true,
  },
  {
    id: 'loc-2',
    nome: 'Capela Sagrado Coração',
    tipo: 'CAPELA',
    capacidade: 80,
    organizacaoNome: 'Comunidade São Pedro',
    endereco: 'Av. Central, 456',
    ativo: false,
  },
];

describe('Locais page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockCurrentUser = {
      id: '1',
      name: 'Padre',
      roles: ['ROLE_ADMIN', 'ADMIN'],
      organizations: [],
    };
    vi.spyOn(locaisApi, 'getLocais').mockResolvedValue(mockLocaisData as any);
    vi.spyOn(organizacoesApi, 'getOrganizacoes').mockResolvedValue([]);
  });

  const renderPage = () => {
    return render(
      <QueryProvider>
        <NotificationProvider>
          <MemoryRouter>
            <Locais />
          </MemoryRouter>
        </NotificationProvider>
      </QueryProvider>
    );
  };

  it('renders page title, subtitle, filter bar and locations table', async () => {
    renderPage();

    expect(screen.getByText('Locais')).toBeInTheDocument();
    expect(screen.getByText('Gerencie os espaços físicos e a capacidade da paróquia.')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Salão Paroquial São José')).toBeInTheDocument();
      expect(screen.getByText('Capela Sagrado Coração')).toBeInTheDocument();
      expect(screen.getByText('200')).toBeInTheDocument();
      expect(screen.getByText('Matriz N. S. de Fátima')).toBeInTheDocument();
    });
  });

  it('filters locations by search text', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Salão Paroquial São José')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/Buscar local por nome ou endereço.../i);
    fireEvent.change(searchInput, { target: { value: 'Capela' } });

    await waitFor(() => {
      expect(screen.queryByText('Salão Paroquial São José')).not.toBeInTheDocument();
      expect(screen.getByText('Capela Sagrado Coração')).toBeInTheDocument();
    });
  });

  it('allows creating a location via modal', async () => {
    const createSpy = vi.spyOn(locaisApi, 'createLocal').mockResolvedValueOnce({
      id: 'loc-3',
      nome: 'Sala 1 Catequese',
      tipo: 'SALA_REUNIAO',
      capacidade: 30,
      ativo: true,
    } as any);

    renderPage();

    const novoBtn = screen.getByRole('button', { name: /Novo Local/i });
    fireEvent.click(novoBtn);

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /Novo Local/i })).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/Nome do Local/i), { target: { value: 'Sala 1 Catequese' } });
    fireEvent.click(screen.getByRole('button', { name: /^Salvar$/i }));

    await waitFor(() => {
      expect(createSpy).toHaveBeenCalled();
    });
  });

  it('allows editing a location via modal', async () => {
    const updateSpy = vi.spyOn(locaisApi, 'updateLocal').mockResolvedValueOnce({
      ...mockLocaisData[0],
      nome: 'Salão Paroquial Reformado',
    } as any);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Salão Paroquial São José')).toBeInTheDocument();
    });

    const actionButtons = screen.getAllByRole('button', { name: /Ações/i });
    fireEvent.click(actionButtons[0]);

    const editMenuItem = await screen.findByRole('menuitem', { name: /Editar/i });
    fireEvent.click(editMenuItem);

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /Editar Local/i })).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/Nome do Local/i), { target: { value: 'Salão Paroquial Reformado' } });
    fireEvent.click(screen.getByRole('button', { name: /Salvar Alterações/i }));

    await waitFor(() => {
      expect(updateSpy).toHaveBeenCalled();
    });
  });

  it('allows toggling status (inactivating and activating)', async () => {
    const deleteSpy = vi.spyOn(locaisApi, 'deleteLocal').mockResolvedValueOnce(undefined);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Salão Paroquial São José')).toBeInTheDocument();
    });

    const actionButtons = screen.getAllByRole('button', { name: /Ações/i });
    fireEvent.click(actionButtons[0]);

    const inativarMenuItem = await screen.findByRole('menuitem', { name: /Inativar/i });
    fireEvent.click(inativarMenuItem);

    await waitFor(() => {
      expect(deleteSpy).toHaveBeenCalledWith('loc-1');
    });
  });

  it('hides Novo Local button and action menu for unauthorized users', async () => {
    mockCurrentUser = {
      id: '2',
      name: 'Membro',
      roles: ['ROLE_MEMBRO'],
      organizations: [],
    };

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Salão Paroquial São José')).toBeInTheDocument();
    });

    expect(screen.queryByRole('button', { name: /Novo Local/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Ações/i })).not.toBeInTheDocument();
  });
});
