import { describe, it, expect } from 'vitest';
import { unidadeTerritorialSchema } from './unidadeTerritorialSchema';

describe('unidadeTerritorialSchema', () => {
  it('deve validar com sucesso uma Igreja Matriz válida', () => {
    const validData = {
      nome: 'Igreja Matriz Nossa Senhora de Fátima',
      tipo: 'MATRIZ',
      ativo: true,
      padroeiro: 'Nossa Senhora de Fátima',
      dataFestaPadroeira: '05-13',
      possuiSacrario: true,
      possuiPiaBatismal: true,
      cnpj: '12.345.678/0001-90',
      areaTerrenoM2: 1200,
      areaConstruidaM2: 950,
      avcbNumero: '12345',
      avcbValidade: '2027-12-31',
      lotacaoMaximaBombeiros: 400,
      telefone: '11988887777',
      email: 'matriz@nsfatima.org.br',
      enderecoCep: '08000-000',
      enderecoLogradouro: 'Rua Central',
      enderecoNumero: '100',
      enderecoBairro: 'Centro',
      enderecoCidade: 'São Paulo',
      enderecoUf: 'SP',
    };

    const result = unidadeTerritorialSchema.safeParse(validData);
    expect(result.success).toBe(true);
  });

  it('deve rejeitar nome muito curto', () => {
    const invalidData = {
      nome: 'AB',
      tipo: 'COMUNIDADE',
    };

    const result = unidadeTerritorialSchema.safeParse(invalidData);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.flatten().fieldErrors.nome).toBeDefined();
    }
  });

  it('deve rejeitar data de festa padroeira fora do formato MM-DD', () => {
    const invalidData = {
      nome: 'Capela São José',
      tipo: 'COMUNIDADE',
      dataFestaPadroeira: '19/03/2026', // Formato incorreto
    };

    const result = unidadeTerritorialSchema.safeParse(invalidData);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.flatten().fieldErrors.dataFestaPadroeira).toBeDefined();
    }
  });

  it('deve aceitar campos numéricos enviados como string ou vazios', () => {
    const data = {
      nome: 'Capela Santa Luzia',
      tipo: 'COMUNIDADE',
      areaTerrenoM2: '500',
      areaConstruidaM2: '',
      lotacaoMaximaBombeiros: '150',
    };

    const result = unidadeTerritorialSchema.safeParse(data);
    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.areaTerrenoM2).toBe(500);
      expect(result.data.areaConstruidaM2).toBeNull();
      expect(result.data.lotacaoMaximaBombeiros).toBe(150);
    }
  });
});
