import '@testing-library/jest-dom';
import React from 'react';
import { vi } from 'vitest';

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
