import React from 'react';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import Layout from './Layout';
import {useAuth} from '@/auth/AuthContext';
import {QueryProvider} from '@/api/QueryProvider';
import {vi} from 'vitest';

vi.mock('../auth/AuthContext', async () => {
  const actual = await vi.importActual('../auth/AuthContext');
  return {
    ...(actual as any),
    useAuth: vi.fn(),
  };
});

describe('Layout component link visibility', () => {
  const mockUseAuth = vi.mocked(useAuth);

  test('renders "Aprovações" link for users with approval permissions', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Padre', roles: ['ROLE_CLERO_PAROCO'] },
      logout: vi.fn(),
      isLoading: false,
      isAuthenticated: true,
      token: 'fake',
      login: vi.fn(),
      setParoquiaAtiva: vi.fn(),
    });

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    expect(screen.getByText('Aprovações')).toBeInTheDocument();
  });

  test('does not render "Aprovações" link for users without approval permissions', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '2', name: 'Membro', roles: ['ROLE_PASTORAL_MEMBRO'] },
      logout: vi.fn(),
      isLoading: false,
      isAuthenticated: true,
      token: 'fake',
      login: vi.fn(),
      setParoquiaAtiva: vi.fn(),
    });

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    expect(screen.queryByText('Aprovações')).not.toBeInTheDocument();
  });

  test('places Fiéis and Pastorais e Movimentos in gestão paroquial', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Padre', roles: ['ROLE_CLERO_PAROCO'] },
      logout: vi.fn(),
      isLoading: false,
      isAuthenticated: true,
      token: 'fake',
      login: vi.fn(),
      setParoquiaAtiva: vi.fn(),
    });

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    expect(screen.getByText('Projetos')).toBeInTheDocument();
    expect(screen.getByText('Fiéis')).toBeInTheDocument();
    expect(screen.getByText('Pastorais e Movimentos')).toBeInTheDocument();
    expect(screen.getByLabelText('abrir menu')).toBeInTheDocument();
  });

  test('renders "Configurações" link for users with management permissions', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Padre', roles: ['ROLE_CLERO_PAROCO'] },
      logout: vi.fn(),
      isLoading: false,
      isAuthenticated: true,
      token: 'fake',
      login: vi.fn(),
      setParoquiaAtiva: vi.fn(),
    });

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    expect(screen.getByText('Configurações')).toBeInTheDocument();
  });

  test('does not render "Configurações" link for users without management permissions', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '2', name: 'Membro', roles: ['ROLE_PASTORAL_MEMBRO'] },
      logout: vi.fn(),
      isLoading: false,
      isAuthenticated: true,
      token: 'fake',
      login: vi.fn(),
      setParoquiaAtiva: vi.fn(),
    });

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    expect(screen.queryByText('Configurações')).not.toBeInTheDocument();
  });

  test('renders menu items in alphabetical order within each category', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Padre', roles: ['ROLE_CLERO_PAROCO'] },
      logout: vi.fn(),
      isLoading: false,
      isAuthenticated: true,
      token: 'fake',
      login: vi.fn(),
      setParoquiaAtiva: vi.fn(),
    });

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    // Pastoral items expected in alphabetical order
    const expectedPastoral = [
      'Calendário',
      'Clero',
      'Conselhos & Atas',
      'Fiéis',
      'Notificações',
      'Pastorais e Movimentos',
      'Projetos',
      'Reservas',
    ];

    const pastoralElements = expectedPastoral.map((name) => screen.getByText(name));
    for (let i = 0; i < pastoralElements.length - 1; i++) {
      expect(
        pastoralElements[i].compareDocumentPosition(pastoralElements[i + 1]) &
          Node.DOCUMENT_POSITION_FOLLOWING
      ).toBeTruthy();
    }

    // Administrative items expected in alphabetical order
    const expectedAdmin = [
      'Ajustes cadastrais',
      'Aprovações',
      'Configurações',
      'Locais',
      'Organograma',
      'Patrimônio',
    ];

    const adminElements = expectedAdmin.map((name) => screen.getByText(name));
    for (let i = 0; i < adminElements.length - 1; i++) {
      expect(
        adminElements[i].compareDocumentPosition(adminElements[i + 1]) &
          Node.DOCUMENT_POSITION_FOLLOWING
      ).toBeTruthy();
    }
  });

  test('toggles category expansion when clicking category header', async () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Padre', roles: ['ROLE_CLERO_PAROCO'] },
      logout: vi.fn(),
      isLoading: false,
      isAuthenticated: true,
      token: 'fake',
      login: vi.fn(),
      setParoquiaAtiva: vi.fn(),
    });

    const { fireEvent } = await import('@testing-library/react');

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    const categoriaPastoral = screen.getByText('Gestão pastoral');
    expect(categoriaPastoral).toBeInTheDocument();

    // Click to collapse
    fireEvent.click(categoriaPastoral);

    // Click again to expand
    fireEvent.click(categoriaPastoral);
    expect(screen.getByText('Fiéis')).toBeInTheDocument();
  });
});
