import React from 'react';
import {vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {ThemeProvider, createTheme} from '@mui/material/styles';
import EventVolumeBarChart from './EventVolumeBarChart';
import {EventDistribution} from '../../api/types';

vi.mock('react-apexcharts', () => ({
  default: ({ options, series }: any) => (
    <div data-testid="mock-apex-bar-chart" data-theme-mode={options?.theme?.mode}>
      {series?.[0]?.data?.map((value: number, index: number) => (
        <span key={options.xaxis.categories[index]}>
          {options.xaxis.categories[index]}: {value}
        </span>
      ))}
    </div>
  ),
}));

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
      { organizacaoNome: 'Pastoral A', quantidadeEventos: 8 },
      { organizacaoNome: 'Pastoral B', quantidadeEventos: 4 },
    ]);

    expect(screen.getByText('Pastoral A: 8')).toBeInTheDocument();
    expect(screen.getByText('Pastoral B: 4')).toBeInTheDocument();
  });

  test('shows empty state message when there is no data', () => {
    renderChart([]);

    expect(screen.getByText('Nenhum dado disponível para exibição no gráfico de barras.')).toBeInTheDocument();
  });

  test('propagates dark mode to the ApexCharts theme option', () => {
    renderChart([{ organizacaoNome: 'Pastoral A', quantidadeEventos: 8 }], 'dark');

    expect(screen.getByTestId('mock-apex-bar-chart')).toHaveAttribute('data-theme-mode', 'dark');
  });

  test('propagates light mode to the ApexCharts theme option', () => {
    renderChart([{ organizacaoNome: 'Pastoral A', quantidadeEventos: 8 }], 'light');

    expect(screen.getByTestId('mock-apex-bar-chart')).toHaveAttribute('data-theme-mode', 'light');
  });
});
