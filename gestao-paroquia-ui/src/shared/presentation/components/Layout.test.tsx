import React from 'react';
import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router-dom';
import Layout from './Layout';
import {useAuth} from '@/contextos/iam/presentation/auth/AuthContext';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';
import {vi} from 'vitest';

vi.mock('@/contextos/iam/presentation/auth/AuthContext', async () => {
  const actual = await vi.importActual('@/contextos/iam/presentation/auth/AuthContext');
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

  test('renders "Governo & Território" link for users with territorial management permissions', () => {
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

    expect(screen.getByText('Governo & Território')).toBeInTheDocument();
  });

  test('does not render "Governo & Território" link for users without territorial management permissions', () => {
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

    expect(screen.queryByText('Governo & Território')).not.toBeInTheDocument();
  });

  test('renders menu items in functional order across sections', () => {
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

    const expectedPastoral = [
      'Painel',
      'Notificações',
      'Relatórios',
      'Fiéis',
      'Clero',
      'Pastorais e Movimentos',
      'Calendário',
      'Projetos',
      'Governo & Território',
      'Organograma',
      'Conselhos & Atas',
    ];

    const pastoralElements = expectedPastoral.map((name) => screen.getByText(name));
    for (let i = 0; i < pastoralElements.length - 1; i++) {
      expect(
        pastoralElements[i].compareDocumentPosition(pastoralElements[i + 1]) &
          Node.DOCUMENT_POSITION_FOLLOWING
      ).toBeTruthy();
    }

    const expectedAdmin = [
      'Locais',
      'Patrimônio',
      'Reservas',
      'Aprovações',
      'Ajustes cadastrais',
      'Configurações',
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

    const keyboard = userEvent.setup();

    render(
      <QueryProvider>
        <MemoryRouter>
          <Layout>
            <div>Content</div>
          </Layout>
        </MemoryRouter>
      </QueryProvider>
    );

    await keyboard.click(screen.getByRole('button', { name: 'abrir menu' }));
    const categoriaPastoral = screen.getByRole('button', { name: 'Pessoas e comunidades' });
    expect(categoriaPastoral).toBeInTheDocument();

    await keyboard.click(categoriaPastoral);
    expect(categoriaPastoral).toHaveAttribute('aria-expanded', 'false');

    await keyboard.click(categoriaPastoral);
    expect(categoriaPastoral).toHaveAttribute('aria-expanded', 'true');
    expect(screen.getByText('Fiéis')).toBeInTheDocument();
    await keyboard.click(screen.getByRole('link', { name: 'Fiéis' }));
    await waitFor(() => expect(screen.getByRole('button', { name: 'abrir menu' })).toBeInTheDocument());
  });
});
