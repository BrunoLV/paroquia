import React from 'react';
import { render, screen, act } from '@testing-library/react';
import App from './App';
import apiClient from './api/client';
import { vi } from 'vitest';

vi.mock('./api/client');
const mockedApiClient = vi.mocked(apiClient);

test('renders Gestão Paróquia heading', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });

  await act(async () => {
    render(<App />);
  });

  const headerElement = screen.getByText(/Gestão Paroquial/i);
  expect(headerElement).toBeInTheDocument();
});
