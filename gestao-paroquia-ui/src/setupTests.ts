import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Polyfill jest global for complete compatibility with existing Jest tests
if (typeof (globalThis as any).jest === 'undefined' || (globalThis as any).jest === vi) {
  (globalThis as any).jest = {
    fn: (...args: any[]) => vi.fn(...args),
    spyOn: (...args: any[]) => (vi.spyOn as any)(...args),
    mock: (...args: any[]) => (vi.mock as any)(...args),
    unmock: (...args: any[]) => (vi.unmock as any)(...args),
    clearAllMocks: () => vi.clearAllMocks(),
    resetAllMocks: () => vi.resetAllMocks(),
    restoreAllMocks: () => vi.restoreAllMocks(),
    useFakeTimers: () => vi.useFakeTimers(),
    useRealTimers: () => vi.useRealTimers(),
    advanceTimersByTime: (ms: number) => vi.advanceTimersByTime(ms),
    runAllTimers: () => vi.runAllTimers(),
    requireActual: (importPath: string) => vi.importActual(importPath),
  };
}

// Global mock for @mui/x-charts to avoid jsdom rendering issues in tests
vi.mock('@mui/x-charts', () => {
  const React = require('react');
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
