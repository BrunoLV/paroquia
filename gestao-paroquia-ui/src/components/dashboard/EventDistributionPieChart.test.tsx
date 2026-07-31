import React from 'react';
import {vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {ThemeProvider, createTheme} from '@mui/material/styles';
import EventDistributionPieChart from './EventDistributionPieChart';
import {EventDistribution} from '../../api/types';

vi.mock('recharts', () => {
  const Passthrough = ({ children }: any) => <div>{children}</div>;
  return {
    ResponsiveContainer: Passthrough,
    PieChart: Passthrough,
    Pie: ({ data, children }: any) => (
      <div data-testid="mock-recharts-pie">
        {data?.map((d: any) => (
          <span key={d.label}>{d.label}: {d.value}</span>
        ))}
        {children}
      </div>
    ),
    Cell: ({ fill }: any) => <i data-testid="mock-recharts-cell" data-fill={fill} />,
    Tooltip: () => null,
    Legend: () => null,
  };
});

const renderChart = (data: EventDistribution[], mode: 'light' | 'dark' = 'light') => {
  const theme = createTheme({ palette: { mode } });
  return render(
    <ThemeProvider theme={theme}>
      <EventDistributionPieChart data={data} />
    </ThemeProvider>
  );
};

describe('EventDistributionPieChart', () => {
  test('renders slices from EventDistribution[]', () => {
    renderChart([
      { organizacaoNome: 'Pastoral A', quantidadeEventos: 8 },
      { organizacaoNome: 'Pastoral B', quantidadeEventos: 4 },
    ]);

    expect(screen.getByText(/Pastoral A: 8/)).toBeInTheDocument();
    expect(screen.getByText(/Pastoral B: 4/)).toBeInTheDocument();
  });

  test('groups organizations below 3% representativeness into "Outras"', () => {
    renderChart([
      { organizacaoNome: 'Pastoral A', quantidadeEventos: 95 },
      { organizacaoNome: 'Pastoral B', quantidadeEventos: 3 },
      { organizacaoNome: 'Pastoral C', quantidadeEventos: 2 },
    ]);

    expect(screen.getByText(/Outras: 2/)).toBeInTheDocument();
    expect(screen.getByText(/Pastoral B: 3/)).toBeInTheDocument();
    expect(screen.queryByText(/Pastoral C/)).not.toBeInTheDocument();
  });

  test('does not show "Outras" slice when no organization is below the threshold', () => {
    renderChart([
      { organizacaoNome: 'Pastoral A', quantidadeEventos: 60 },
      { organizacaoNome: 'Pastoral B', quantidadeEventos: 40 },
    ]);

    expect(screen.queryByText(/Outras/)).not.toBeInTheDocument();
  });

  test('shows empty state message when there is no data', () => {
    renderChart([]);

    expect(screen.getByText('Sem dados de eventos para exibir')).toBeInTheDocument();
  });

  test('shows empty state message when all values are zero', () => {
    renderChart([{ organizacaoNome: 'Pastoral A', quantidadeEventos: 0 }]);

    expect(screen.getByText('Sem dados de eventos para exibir')).toBeInTheDocument();
  });

  test('applies MUI theme palette colors to slices and reacts to palette.mode', () => {
    const { rerender } = renderChart([
      { organizacaoNome: 'Pastoral A', quantidadeEventos: 8 },
      { organizacaoNome: 'Pastoral B', quantidadeEventos: 4 },
    ], 'light');

    const lightTheme = createTheme({ palette: { mode: 'light' } });
    const lightFills = screen.getAllByTestId('mock-recharts-cell').map((el) => el.getAttribute('data-fill'));
    expect(lightFills[0]).toBe(lightTheme.palette.primary.main);
    expect(lightFills[1]).toBe(lightTheme.palette.secondary.main);

    const darkTheme = createTheme({ palette: { mode: 'dark' } });
    rerender(
      <ThemeProvider theme={darkTheme}>
        <EventDistributionPieChart
          data={[
            { organizacaoNome: 'Pastoral A', quantidadeEventos: 8 },
            { organizacaoNome: 'Pastoral B', quantidadeEventos: 4 },
          ]}
        />
      </ThemeProvider>
    );

    const darkFills = screen.getAllByTestId('mock-recharts-cell').map((el) => el.getAttribute('data-fill'));
    expect(darkFills[0]).toBe(darkTheme.palette.primary.main);
  });
});
