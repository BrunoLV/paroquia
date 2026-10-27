import { describe, expect, it } from 'vitest';
import { projetoSchema } from './projetoSchema';

describe('projetoSchema', () => {
  const base = { titulo: 'Festa do padroeiro', dataInicio: '2026-05-01' };

  it('aceita projeto mínimo e assume orçamento zero', () => {
    const resultado = projetoSchema.safeParse(base);

    expect(resultado.success).toBe(true);
    expect(resultado.data?.orcamentoPrevisto).toBe(0);
  });

  it('rejeita término anterior ao início apontando o campo dataFim', () => {
    const resultado = projetoSchema.safeParse({ ...base, dataFim: '2026-04-01' });

    expect(resultado.success).toBe(false);
    expect(resultado.error?.issues[0]?.path).toEqual(['dataFim']);
  });
});
