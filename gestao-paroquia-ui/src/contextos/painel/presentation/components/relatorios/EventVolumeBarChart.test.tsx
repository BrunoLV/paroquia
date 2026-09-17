import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import EventVolumeBarChart from './EventVolumeBarChart';
import { EventDistribution } from '@/shared/domain/types/types';

const renderChart = (data: EventDistribution[], mode: 'light' | 'dark' = 'light') => {
  const theme = createTheme({ palette: { mode } });
  return render(
    <ThemeProvider theme={theme}>
      <EventVolumeBarChart data={data} />
    </ThemeProvider>
  );
};

describe('EventVolumeBarChart', () => {
  test('renders a bar series from eventosPorOrganizacao data', () => {
    renderChart([
      { grupoParoquialNome: 'Pastoral A', quantidadeEventos: 8 },
      { grupoParoquialNome: 'Pastoral B', quantidadeEventos: 4 },
    ]);

    expect(screen.getByText('Volume de Eventos por Organização')).toBeInTheDocument();
  });

  test('shows empty state message when there is no data', () => {
    renderChart([]);

    expect(screen.getByText('Nenhum dado disponível para exibição no gráfico de barras.')).toBeInTheDocument();
  });
});
