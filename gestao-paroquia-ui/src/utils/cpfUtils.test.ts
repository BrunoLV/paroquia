import { describe, expect, it } from 'vitest';
import { cleanCpf, formatCpf, formatProgressiveCpf } from './cpfUtils';

describe('cpfUtils', () => {
  it('cleanCpf extrai apenas os números', () => {
    expect(cleanCpf('123.456.789-00')).toBe('12345678900');
    expect(cleanCpf('abc-123.456-def')).toBe('123456');
    expect(cleanCpf(null)).toBe('');
    expect(cleanCpf(undefined)).toBe('');
  });

  it('formatCpf formata CPF completo de 11 dígitos', () => {
    expect(formatCpf('12345678900')).toBe('123.456.789-00');
    expect(formatCpf('12345678900', 'full')).toBe('123.456.789-00');
  });

  it('formatCpf formata CPF de acordo com a variante de anonimização LGPD', () => {
    const cpf = '12345678900';
    expect(formatCpf(cpf, 'clean')).toBe('12345678900');
    expect(formatCpf(cpf, 'masked_middle')).toBe('123.***.***-00');
    expect(formatCpf(cpf, 'masked_ends')).toBe('***.456.789-**');
    expect(formatCpf(cpf, 'last_five')).toBe('***.***.789-00');
    expect(formatCpf(cpf, 'masked_dv')).toBe('123.456.789-**');
  });

  it('formatCpf trata valores nulos e vazios', () => {
    expect(formatCpf(null)).toBe('-');
    expect(formatCpf(undefined)).toBe('-');
    expect(formatCpf('')).toBe('-');
    expect(formatCpf('   ')).toBe('-');
  });

  it('formatProgressiveCpf mascara conforme digitação', () => {
    expect(formatProgressiveCpf('123')).toBe('123');
    expect(formatProgressiveCpf('1234')).toBe('123.4');
    expect(formatProgressiveCpf('123456')).toBe('123.456');
    expect(formatProgressiveCpf('1234567')).toBe('123.456.7');
    expect(formatProgressiveCpf('123456789')).toBe('123.456.789');
    expect(formatProgressiveCpf('12345678900')).toBe('123.456.789-00');
  });
});
