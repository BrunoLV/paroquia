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

test('theme should have Roboto or Inter typography', () => {
  expect(theme.typography.fontFamily).toContain('Roboto');
});

test('theme should standardize buttons to avoid all-caps and use 8px border-radius', () => {
  const buttonStyle: any = theme.components?.MuiButton?.styleOverrides?.root;
  expect(buttonStyle?.textTransform).toBe('none');
  expect(buttonStyle?.borderRadius).toBe('8px');
});

test('theme should standardize text fields to use 8px border-radius', () => {
  const inputStyle: any = theme.components?.MuiOutlinedInput?.styleOverrides?.root;
  expect(inputStyle?.borderRadius).toBe('8px');
});

test('theme should standardize links to underline on hover only', () => {
  const linkProps: any = theme.components?.MuiLink?.defaultProps;
  expect(linkProps?.underline).toBe('hover');
});



