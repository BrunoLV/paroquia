import '@testing-library/jest-dom';
import React from 'react';
import {vi} from 'vitest';

// Provide global jest alias for Vitest
(globalThis as any).jest = vi;

// Global mock for @mui/x-charts to avoid jsdom rendering issues in tests
vi.mock('@mui/x-charts', () => {
  return {
    PieChart: ({ series }: any) => {
      const children = series?.[0]?.data?.map((d: any) =>
        React.createElement('span', { key: d.label }, `${d.label}: ${d.value}`)
      ) || [];
      return React.createElement('div', { 'data-testid': 'mock-pie-chart' }, ...children);
    },
    BarChart: ({ series }: any) => {
      const children = series?.map((s: any) =>
        React.createElement('span', { key: s.label }, `${s.label}: ${s.data?.[0]}`)
      ) || [];
      return React.createElement('div', { 'data-testid': 'mock-bar-chart' }, ...children);
    },
  };
});

// Global mock for recharts to avoid jsdom rendering issues in tests
vi.mock('recharts', () => {
  const Passthrough = ({ children }: any) => React.createElement('div', null, children);
  return {
    ResponsiveContainer: Passthrough,
    PieChart: Passthrough,
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

// Global mock for react-apexcharts to avoid jsdom rendering issues in tests
vi.mock('react-apexcharts', () => {
  return {
    default: ({ options, series }: any) => {
      const categories = options?.xaxis?.categories || [];
      const children = series?.[0]?.data?.map((value: number, index: number) =>
        React.createElement('span', { key: categories[index] }, `${categories[index]}: ${value}`)
      ) || [];
      return React.createElement(
        'div',
        { 'data-testid': 'mock-apex-bar-chart', 'data-theme-mode': options?.theme?.mode },
        ...children
      );
    },
  };
});
