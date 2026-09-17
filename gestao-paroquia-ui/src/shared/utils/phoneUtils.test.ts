import { describe, expect, test } from 'vitest';
import { formatPhone } from './phoneUtils';

describe('phoneUtils', () => {
  test('deve retornar "-" para valores nulos, indefinidos ou vazios', () => {
    expect(formatPhone(null)).toBe('-');
    expect(formatPhone(undefined)).toBe('-');
    expect(formatPhone('')).toBe('-');
    expect(formatPhone('   ')).toBe('-');
  });

  test('deve formatar telefone celular (11 dígitos)', () => {
    expect(formatPhone('11987654321')).toBe('(11) 98765-4321');
    expect(formatPhone('11 98765 4321')).toBe('(11) 98765-4321');
  });

  test('deve formatar telefone fixo (10 dígitos)', () => {
    expect(formatPhone('1133334444')).toBe('(11) 3333-4444');
    expect(formatPhone('11 3333 4444')).toBe('(11) 3333-4444');
  });

  test('deve preservar a string se já estiver formatada', () => {
    expect(formatPhone('(11) 98765-4321')).toBe('(11) 98765-4321');
    expect(formatPhone('(11) 3333-4444')).toBe('(11) 3333-4444');
  });
});
