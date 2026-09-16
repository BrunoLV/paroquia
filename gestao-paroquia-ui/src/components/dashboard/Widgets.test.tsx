import React from 'react';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import SummaryCards from './SummaryCards';
import UpcomingEvents from './UpcomingEvents';
import {ThemeProvider} from '@mui/material/styles';
import theme from '@/theme';

describe('Dashboard Widgets', () => {
  describe('SummaryCards', () => {
    test('renders metrics when provided', () => {
      const resumo = {
        totalEventosPeriodo: 5,
        projetosAtivos: 7,
        solicitacoesPendentes: 2,
        totalFieisAtivos: 9,
        novosFieisMes: 1,
        fieisEngajadosPastoral: 4,
        fieisSemVinculoPastoral: 5,
      };
      render(
        <ThemeProvider theme={theme}>
          <SummaryCards resumo={resumo} />
        </ThemeProvider>
      );
      expect(screen.getByText('5')).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument();
      expect(screen.getByText('+1 no mês')).toBeInTheDocument();
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
      const { container } = render(
        <MemoryRouter>
          <ThemeProvider theme={theme}>
            <UpcomingEvents isLoading={true} />
          </ThemeProvider>
        </MemoryRouter>
      );
      expect(container.querySelectorAll('.MuiSkeleton-root').length).toBeGreaterThan(0);
    });

    test('renders list of events', () => {
      const events = [
        { id: '1', titulo: 'Missa de Teste', inicio: '2026-05-21T10:00:00Z', categoria: 'LITURGICO' }
      ];
      render(
        <MemoryRouter>
          <ThemeProvider theme={theme}>
            <UpcomingEvents events={events} isLoading={false} />
          </ThemeProvider>
        </MemoryRouter>
      );
      expect(screen.getByText('Missa de Teste')).toBeInTheDocument();
      expect(screen.getByTestId('ChurchIcon')).toBeInTheDocument();
    });

    test('renders empty state', () => {
      render(
        <MemoryRouter>
          <ThemeProvider theme={theme}>
            <UpcomingEvents events={[]} isLoading={false} />
          </ThemeProvider>
        </MemoryRouter>
      );
      expect(screen.getByText(/Não há eventos próximos/i)).toBeInTheDocument();
    });
  });
});
