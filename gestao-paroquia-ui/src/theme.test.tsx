import React from 'react';
import { render, screen } from '@testing-library/react';
import { useTheme, ThemeProvider } from '@mui/material/styles';
import theme from './theme';

const ThemeColorChecker = () => {
  const currentTheme = useTheme();
  return <div data-testid="primary-color">{currentTheme.palette.primary.main}</div>;
};

test('theme should have custom primary color', () => {
  render(
    <ThemeProvider theme={theme}>
      <ThemeColorChecker />
    </ThemeProvider>
  );
  const colorElement = screen.getByTestId('primary-color');
  expect(colorElement.textContent).toBe('#1A237E');
});


