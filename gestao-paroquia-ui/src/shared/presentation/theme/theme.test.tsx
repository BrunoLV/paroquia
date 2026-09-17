import React from 'react';
import {render, screen} from '@testing-library/react';
import {ThemeProvider, useTheme} from '@mui/material/styles';
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

test('theme should have custom secondary gold color', () => {
  expect(theme.palette.secondary.main.toUpperCase()).toBe('#C59B27');
});

test('theme should have custom error crimson red color', () => {
  expect(theme.palette.error?.main?.toUpperCase()).toBe('#C62828');
});

test('theme should have custom background cream/white colors', () => {
  expect(theme.palette.background?.default?.toUpperCase()).toBe('#FFFDF4');
  expect(theme.palette.background?.paper?.toUpperCase()).toBe('#FFFFFF');
});

test('theme should expose semantic surface and focus tokens', () => {
  expect(theme.custom.surfaceMuted).toBe('#F3F5F8');
  expect(theme.custom.focusRing).toBe('rgba(26, 35, 126, 0.28)');
});

test('theme should standardize interactive tables and selected navigation', () => {
  const tableHead = theme.components?.MuiTableHead?.styleOverrides?.root;
  const listItemButton = theme.components?.MuiListItemButton?.styleOverrides?.root;

  expect(tableHead).toBeDefined();
  expect(listItemButton).toBeDefined();
});

test('theme should standardize typography with IBM Plex Sans', () => {
  expect(theme.typography.fontFamily).toContain('IBM Plex Sans');
});

test('theme should have a modern rounded border-radius globally', () => {
  expect(theme.shape.borderRadius).toBe(12);
});

test('theme should standardize buttons to avoid all-caps and use rounded corners', () => {
  const buttonStyle: any = theme.components?.MuiButton?.styleOverrides?.root;
  expect(buttonStyle?.textTransform).toBe('none');
  expect(buttonStyle?.borderRadius).toBe('10px');
});

test('theme should standardize text fields to use rounded corners', () => {
  const inputStyle: any = theme.components?.MuiOutlinedInput?.styleOverrides?.root;
  expect(inputStyle?.borderRadius).toBe('10px');
});

test('theme should standardize links to underline on hover only', () => {
  const linkProps: any = theme.components?.MuiLink?.defaultProps;
  expect(linkProps?.underline).toBe('hover');
});

test('theme should use a consistent administrative heading scale', () => {
  expect(theme.typography.h1.fontSize).toBe('1.75rem');
  expect(theme.typography.h2.fontSize).toBe('1.5rem');
  expect(theme.typography.h3.fontSize).toBe('1.25rem');
  expect(theme.typography.h4.fontSize).toBe('1.125rem');
  expect(theme.typography.h5.fontSize).toBe('1rem');
  expect(theme.typography.h6.fontSize).toBe('0.9375rem');
});

test('theme should keep body text compact and readable', () => {
  expect(theme.typography.body1.fontSize).toBe('0.875rem');
});

test('theme should use a coherent supporting text scale', () => {
  expect(theme.typography.subtitle1.fontSize).toBe('0.875rem');
  expect(theme.typography.subtitle2.fontSize).toBe('0.8125rem');
  expect(theme.typography.body2.fontSize).toBe('0.8125rem');
  expect(theme.typography.button.fontSize).toBe('0.8125rem');
  expect(theme.typography.caption.fontSize).toBe('0.6875rem');
  expect(theme.typography.overline.fontSize).toBe('0.6875rem');
});

test('theme should keep the standard spacing unit', () => {
  expect(theme.spacing(1)).toBe('7px');
  expect(theme.spacing(2)).toBe('14px');
});

test('theme should use comfortable button padding', () => {
  const buttonStyle: any = theme.components?.MuiButton?.styleOverrides?.root;
  expect(buttonStyle?.padding).toBe('4px 10px');
});

test('theme should use comfortable outlined input padding', () => {
  const inputStyle: any = theme.components?.MuiOutlinedInput?.styleOverrides?.input;
  expect(inputStyle?.padding).toBe('11px 12px');
});

test('theme should use the standard table cell padding', () => {
  const cellStyle: any = theme.components?.MuiTableCell?.styleOverrides?.root;
  expect(cellStyle?.padding).toBe('10px 12px');
});

test('theme should use readable chip heights', () => {
  const mediumChipStyle: any = theme.components?.MuiChip?.styleOverrides?.sizeMedium;
  const smallChipStyle: any = theme.components?.MuiChip?.styleOverrides?.sizeSmall;
  expect(mediumChipStyle?.height).toBe('28px');
  expect(smallChipStyle?.height).toBe('22px');
});

test('theme should use comfortable small outlined input padding', () => {
  const inputSmallStyle: any = theme.components?.MuiOutlinedInput?.styleOverrides?.sizeSmall;
  expect(inputSmallStyle?.paddingTop).toBe('6px');
  expect(inputSmallStyle?.paddingBottom).toBe('6px');
});
