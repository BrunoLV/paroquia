import React from 'react';
import { render, screen } from '@testing-library/react';
import Dashboard from './Dashboard';
import { ThemeProvider } from '@mui/material/styles';
import theme from '../theme';
import { MemoryRouter } from 'react-router-dom';

describe('Dashboard Page', () => {
  test('renders dashboard layout structure', () => {
    render(
      <MemoryRouter>
        <ThemeProvider theme={theme}>
          <Dashboard />
        </ThemeProvider>
      </MemoryRouter>
    );

    // Initial check for titles (using headings to be specific)
    expect(screen.getByRole('heading', { name: /Visão Geral/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Ações Rápidas/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Próximos Eventos/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /Aprovações Pendentes/i })).toBeInTheDocument();
  });
});
