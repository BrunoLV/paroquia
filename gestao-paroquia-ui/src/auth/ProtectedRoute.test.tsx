import { vi } from "vitest";
import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import ProtectedRoute from './ProtectedRoute';
import apiClient from '../api/client';

vi.mock('../api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

const PrivateComponent = () => <div>Private Content</div>;
const LoginComponent = () => <div>Login Page</div>;

test('redirects to login if not authenticated', async () => {
  mockedApiClient.get.mockRejectedValueOnce({ response: { status: 401 } });

  await act(async () => {
    render(
      <MemoryRouter initialEntries={['/private']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginComponent />} />
            <Route
              path="/private"
              element={
                <ProtectedRoute>
                  <PrivateComponent />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );
  });

  expect(screen.getByText('Login Page')).toBeInTheDocument();
  expect(screen.queryByText('Private Content')).not.toBeInTheDocument();
});

test('renders children if authenticated', async () => {
  const user = { id: '1', name: 'Test User', username: 'testuser', roles: ['ADMIN'] };
  mockedApiClient.get.mockResolvedValueOnce({ data: user });

  await act(async () => {
    render(
      <MemoryRouter initialEntries={['/private']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginComponent />} />
            <Route
              path="/private"
              element={
                <ProtectedRoute>
                  <PrivateComponent />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );
  });

  expect(screen.getByText('Private Content')).toBeInTheDocument();
});
