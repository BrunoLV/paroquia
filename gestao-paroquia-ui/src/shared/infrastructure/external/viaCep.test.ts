import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchAddressByCep } from './viaCep';

describe('fetchAddressByCep', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('deve retornar null se o CEP tiver menos de 8 dígitos', async () => {
    const result = await fetchAddressByCep('12345');
    expect(result).toBeNull();
  });

  it('deve retornar dados do endereço quando o CEP for válido e encontrado', async () => {
    const mockData = {
      cep: '01001-000',
      logradouro: 'Praça da Sé',
      bairro: 'Sé',
      localidade: 'São Paulo',
      uf: 'SP',
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockData,
    } as Response);

    const result = await fetchAddressByCep('01001000');
    expect(result).toEqual({
      cep: '01001-000',
      logradouro: 'Praça da Sé',
      bairro: 'Sé',
      cidade: 'São Paulo',
      uf: 'SP',
    });
  });

  it('deve retornar null se a API retornar erro: true', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ erro: true }),
    } as Response);

    const result = await fetchAddressByCep('99999999');
    expect(result).toBeNull();
  });

  it('deve retornar null em caso de falha na rede', async () => {
    globalThis.fetch = vi.fn().mockRejectedValue(new Error('Network error'));

    const result = await fetchAddressByCep('01001000');
    expect(result).toBeNull();
  });
});
