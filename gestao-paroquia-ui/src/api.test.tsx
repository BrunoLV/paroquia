import { vi } from "vitest";
import React from 'react';
import { render } from '@testing-library/react';
import App from './App';
import apiClient from './api/client';
import axios from 'axios';

vi.mock('axios', () => {
  const actualAxios = jest.requireActual('axios');
  return {
    ...actualAxios,
    create: jest.fn(() => ({
      ...actualAxios.create(),
      interceptors: {
        request: { use: jest.fn(), eject: jest.fn() },
        response: { use: jest.fn(), eject: jest.fn() },
      },
      get: jest.fn(),
    })),
  };
});

test('app should render without crashing when wrapped in QueryProvider', () => {
  render(<App />);
});

// Since we mock axios.create in the module scope, we might need a different approach 
// to test the interceptors if they are already applied.
// Let's test the interceptor logic by checking if it reads from localStorage.


