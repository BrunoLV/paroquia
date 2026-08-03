import React from 'react';
import {render, screen} from '@testing-library/react';
import SummaryCards from './SummaryCards';
import UpcomingEvents from './UpcomingEvents';
import {ThemeProvider} from '@mui/material/styles';
import theme from '../../theme';

describe('Dashboard Widgets', () => {
  describe('SummaryCards', () => {
    test('renders metrics when provided', () => {
      const metrics = { monthlyEvents: 5, aprovacoesPendentes: 2 };
      render(
        <ThemeProvider theme={theme}>
          <SummaryCards metrics={metrics} />
        </ThemeProvider>
      );
      expect(screen.getByText('5')).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument();
    });

    test('renders placeholders when metrics are missing', () => {
      render(
        <ThemeProvider theme={theme}>
          <SummaryCards />
        </ThemeProvider>
      );
      const placeholders = screen.getAllByText('--');
      expect(placeholders.length).toBeGreaterThanOrEqual(2);
    });
  });

  describe('UpcomingEvents', () => {
    test('renders loading state', () => {
      render(
        <ThemeProvider theme={theme}>
          <UpcomingEvents isLoading={true} />
        </ThemeProvider>
      );
      expect(screen.getByText(/Carregando eventos/i)).toBeInTheDocument();
    });

    test('renders list of events', () => {
      const events = [
        { id: '1', titulo: 'Missa de Teste', inicio: '2026-05-21T10:00:00Z', categoria: 'LITURGICO' }
      ];
      render(
        <ThemeProvider theme={theme}>
          <UpcomingEvents events={events} isLoading={false} />
        </ThemeProvider>
      );
      expect(screen.getByText('Missa de Teste')).toBeInTheDocument();
      expect(screen.getByText(/LITURGICO/i)).toBeInTheDocument();
    });

    test('renders empty state', () => {
      render(
        <ThemeProvider theme={theme}>
          <UpcomingEvents events={[]} isLoading={false} />
        </ThemeProvider>
      );
      expect(screen.getByText(/Não há eventos próximos/i)).toBeInTheDocument();
    });
  });
});
