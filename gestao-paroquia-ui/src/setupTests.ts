// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';
import React from 'react';

// Global mock for @mui/x-charts to avoid jsdom and ES module parsing errors in tests
jest.mock('@mui/x-charts', () => {
  const React = require('react');
  return {
    PieChart: ({ series }: any) => {
      const children = series?.[0]?.data?.map((d: any) =>
        React.createElement('span', { key: d.label }, `${d.label}: ${d.value}`)
      ) || [];
      return React.createElement('div', { 'data-testid': 'mock-pie-chart' }, ...children);
    }
  };
});
