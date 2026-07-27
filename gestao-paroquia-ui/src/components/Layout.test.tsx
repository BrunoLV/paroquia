import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Layout from './Layout';
import { useAuth } from '../auth/AuthContext';
import { QueryProvider } from '../api/QueryProvider';
import { vi } from 'vitest';

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

  test('renders "Projetos" link and hamburger menu button in layout', () => {
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
});
