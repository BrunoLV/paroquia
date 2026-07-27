import { vi } from "vitest";
import React from 'react';
import { render } from '@testing-library/react';
import App from './App';
import apiClient from './api/client';
import axios from 'axios';

vi.mock('axios', async (importOriginal) => {
  const actualAxios: any = await importOriginal();
  return {
    ...actualAxios,
    default: {
      ...actualAxios.default,
      create: vi.fn(() => ({
        interceptors: {
          request: { use: vi.fn(), eject: vi.fn() },
          response: { use: vi.fn(), eject: vi.fn() },
        },
        get: vi.fn(),
        post: vi.fn(),
      })),
    },
  };
});

test('app should render without crashing when wrapped in QueryProvider', () => {
  render(<App />);
});

// Since we mock axios.create in the module scope, we might need a different approach 
// to test the interceptors if they are already applied.
// Let's test the interceptor logic by checking if it reads from localStorage.


