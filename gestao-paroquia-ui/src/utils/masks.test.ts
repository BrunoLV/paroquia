import { formatCep, formatCpf, formatDateBr, formatPhone, formatRg, maskRgPartial } from './masks';

describe('masks utility', () => {
  describe('formatCpf', () => {
    it('returns empty string for null/undefined/empty', () => {
      expect(formatCpf(null)).toBe('');
      expect(formatCpf(undefined)).toBe('');
      expect(formatCpf('')).toBe('');
    });

    it('formats 11 digit CPF correctly', () => {
      expect(formatCpf('12345678901')).toBe('123.456.789-01');
      expect(formatCpf('123.456.789-01')).toBe('123.456.789-01');
    });

    it('formats partial CPF progressively', () => {
      expect(formatCpf('123')).toBe('123');
      expect(formatCpf('1234')).toBe('123.4');
      expect(formatCpf('123456')).toBe('123.456');
      expect(formatCpf('123456789')).toBe('123.456.789');
      expect(formatCpf('1234567890')).toBe('123.456.789-0');
    });
  });
  describe('formatRg', () => {
    it('returns empty string for null/undefined/empty', () => {
      expect(formatRg(null)).toBe('');
      expect(formatRg(undefined)).toBe('');
      expect(formatRg('')).toBe('');
    });

    it('formats 9 character RG correctly with digits and check letter', () => {
      expect(formatRg('123456789')).toBe('12.345.678-9');
      expect(formatRg('12345678x')).toBe('12.345.678-X');
      expect(formatRg('12.345.678-9')).toBe('12.345.678-9');
    });

    it('formats partial input progressively', () => {
      expect(formatRg('12')).toBe('12');
      expect(formatRg('123')).toBe('12.3');
      expect(formatRg('12345')).toBe('12.345');
      expect(formatRg('123456')).toBe('12.345.6');
      expect(formatRg('12345678')).toBe('12.345.678');
    });
  });

  describe('maskRgPartial', () => {
    it('masks middle section of full RG', () => {
      expect(maskRgPartial('123456789')).toBe('**.345.678-*');
    });

    it('returns raw format if shorter than full length', () => {
      expect(maskRgPartial('123')).toBe('12.3');
    });
  });

  describe('formatPhone', () => {
    it('formats 11 digit mobile phone', () => {
      expect(formatPhone('11987654321')).toBe('(11) 98765-4321');
    });

    it('formats 10 digit landline phone', () => {
      expect(formatPhone('1133334444')).toBe('(11) 3333-4444');
    });
  });

  describe('formatCep', () => {
    it('formats 8 digit CEP', () => {
      expect(formatCep('01310100')).toBe('01310-100');
    });
  });

  describe('formatDateBr', () => {
    it('formats YYYY-MM-DD to DD/MM/YYYY', () => {
      expect(formatDateBr('1990-05-20')).toBe('20/05/1990');
    });
    it('formats ISO datetime to DD/MM/YYYY', () => {
      expect(formatDateBr('2026-09-10T14:30:00')).toBe('10/09/2026');
    });
  });
});
