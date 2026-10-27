import { describe, expect, it } from 'vitest';
import { ativoSchema, manutencaoAtivoSchema } from './ativoSchema';

const ativoValido = {
  nome: 'Projetor',
  categoria: 'ELETRONICO' as const,
  organizacaoId: 'org-1',
  isReservavel: true,
  dataAquisicao: '2026-01-10',
  valorAquisicao: 1500,
};

describe('ativoSchema', () => {
  it('aceita um cadastro completo', () => {
    expect(ativoSchema.safeParse(ativoValido).success).toBe(true);
  });

  it('rejeita valor de aquisição negativo e categoria desconhecida', () => {
    const negativo = ativoSchema.safeParse({ ...ativoValido, valorAquisicao: -1 });
    const categoria = ativoSchema.safeParse({ ...ativoValido, categoria: 'OUTRA' });

    expect(negativo.success).toBe(false);
    expect(categoria.success).toBe(false);
  });
});

describe('manutencaoAtivoSchema', () => {
  const manutencao = { descricaoReparo: 'Troca da lâmpada do projetor', dataInicio: '2026-02-01' };

  it('exige previsão de término posterior ao início', () => {
    const invalido = manutencaoAtivoSchema.safeParse({ ...manutencao, dataFimPrevista: '2026-02-01' });
    const valido = manutencaoAtivoSchema.safeParse({ ...manutencao, dataFimPrevista: '2026-02-05' });

    expect(invalido.success).toBe(false);
    expect(valido.success).toBe(true);
  });
});
