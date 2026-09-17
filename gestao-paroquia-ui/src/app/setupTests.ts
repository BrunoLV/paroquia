import '@testing-library/jest-dom';
import React from 'react';
import {vi} from 'vitest';

// Provide global jest alias for Vitest
(globalThis as any).jest = vi;

// Global mock for recharts to avoid jsdom rendering issues in tests
vi.mock('recharts', () => {
  const Passthrough = ({ children }: any) => React.createElement('div', null, children);
  return {
    ResponsiveContainer: Passthrough,
    PieChart: Passthrough,
    BarChart: Passthrough,
    Bar: Passthrough,
    XAxis: Passthrough,
    YAxis: Passthrough,
    Pie: ({ data, children }: any) => {
      const slices = data?.map((d: any) =>
        React.createElement('span', { key: d.label }, `${d.label}: ${d.value}`)
      ) || [];
      return React.createElement('div', { 'data-testid': 'mock-recharts-pie' }, ...slices, children);
    },
    Cell: ({ fill }: any) => React.createElement('i', { 'data-testid': 'mock-recharts-cell', 'data-fill': fill }),
    Tooltip: () => null,
    Legend: () => null,
  };
});


