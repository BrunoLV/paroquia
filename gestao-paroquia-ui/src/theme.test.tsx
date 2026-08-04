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

test('theme should have Roboto or Inter typography', () => {
  expect(theme.typography.fontFamily).toContain('Roboto');
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

test('theme should reduce heading font sizes by 20% for a more compact layout', () => {
  expect(theme.typography.h1.fontSize).toBe('2rem');
  expect(theme.typography.h2.fontSize).toBe('3rem');
  expect(theme.typography.h3.fontSize).toBe('2.4rem');
  expect(theme.typography.h4.fontSize).toBe('1.7rem');
  expect(theme.typography.h5.fontSize).toBe('1.2rem');
  expect(theme.typography.h6.fontSize).toBe('1rem');
});

test('theme should keep body1 font size at the 14px accessibility floor', () => {
  expect(theme.typography.body1.fontSize).toBe('0.875rem');
});

test('theme should reduce secondary text font sizes by 20% for a more compact layout', () => {
  expect(theme.typography.subtitle1.fontSize).toBe('0.8rem');
  expect(theme.typography.subtitle2.fontSize).toBe('0.7rem');
  expect(theme.typography.body2.fontSize).toBe('0.7rem');
  expect(theme.typography.button.fontSize).toBe('0.7rem');
  expect(theme.typography.caption.fontSize).toBe('0.6rem');
  expect(theme.typography.overline.fontSize).toBe('0.6rem');
});

test('theme should reduce the base spacing unit by 20% for a more compact layout', () => {
  expect(theme.spacing(1)).toBe('6.4px');
  expect(theme.spacing(2)).toBe('12.8px');
});

test('theme should reduce button padding by 20% for a more compact layout', () => {
  const buttonStyle: any = theme.components?.MuiButton?.styleOverrides?.root;
  expect(buttonStyle?.padding).toBe('5px 13px');
});

test('theme should reduce outlined input padding by 20% for a more compact layout', () => {
  const inputStyle: any = theme.components?.MuiOutlinedInput?.styleOverrides?.input;
  expect(inputStyle?.padding).toBe('13px 11px');
});

test('theme should reduce table cell padding by 20% for a more compact layout', () => {
  const cellStyle: any = theme.components?.MuiTableCell?.styleOverrides?.root;
  expect(cellStyle?.padding).toBe('13px');
});

test('theme should reduce chip height by 20% for a more compact layout', () => {
  const chipStyle: any = theme.components?.MuiChip?.styleOverrides?.root;
  expect(chipStyle?.height).toBe('26px');
});

