import { formatCurrencyBRL } from './currencyUtils';

describe('formatCurrencyBRL', () => {
  it('formats a positive value as BRL currency', () => {
    expect(formatCurrencyBRL(1234.56)).toBe('R$ 1.234,56');
  });

  it('formats zero as BRL currency', () => {
    expect(formatCurrencyBRL(0)).toBe('R$ 0,00');
  });

  it('returns a dash for null', () => {
    expect(formatCurrencyBRL(null)).toBe('—');
  });

  it('returns a dash for undefined', () => {
    expect(formatCurrencyBRL(undefined)).toBe('—');
  });
});
