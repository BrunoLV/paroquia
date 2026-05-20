import React from 'react';
import { render, screen, act } from '@testing-library/react';
import App from './App';
import apiClient from './api/client';

jest.mock('./api/client');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

test('renders Gestão Paróquia heading', async () => {
  mockedApiClient.get.mockResolvedValueOnce({ data: null });

  await act(async () => {
    render(<App />);
  });

  const headerElement = screen.getByText(/Gestão Paróquia/i);
  expect(headerElement).toBeInTheDocument();
});
