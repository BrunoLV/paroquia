import React from 'react';
import {act, render, screen} from '@testing-library/react';
import App from './App';
import apiClient from '@/shared/infrastructure/http/client';
import {vi} from 'vitest';

vi.mock('@/shared/infrastructure/http/client');
const mockedApiClient = vi.mocked(apiClient);

test('renders Gestão Paróquia heading', async () => {
  // @ts-expect-error fixture parcial: o teste só preenche os campos que exercita
  mockedApiClient.get.mockResolvedValueOnce({ data: null });

  await act(async () => {
    render(<App />);
  });

  const headerElement = await screen.findByText(/Gestão Paroquial/i);
  expect(headerElement).toBeInTheDocument();
});
