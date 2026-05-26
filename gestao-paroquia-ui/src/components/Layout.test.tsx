import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Layout from './Layout';
import { AuthProvider, useAuth } from '../auth/AuthContext';
import { QueryProvider } from '../api/QueryProvider';

jest.mock('../auth/AuthContext', () => {
  const actual = jest.requireActual('../auth/AuthContext');
  return {
    ...actual,
    useAuth: jest.fn(),
  };
});

describe('Layout component link visibility', () => {
  const mockUseAuth = useAuth as jest.Mock;

  test('renders "Aprovações" link for users with approval permissions', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Padre', roles: ['ROLE_CLERO_PAROCO'] },
      logout: jest.fn(),
      isLoading: false,
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
      logout: jest.fn(),
      isLoading: false,
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
});
