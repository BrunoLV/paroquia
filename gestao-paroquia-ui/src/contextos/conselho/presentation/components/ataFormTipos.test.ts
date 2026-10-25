import { describe, expect, it } from 'vitest';
import { funcaoSugerida } from '@/contextos/conselho/presentation/components/ataFormTipos';

describe('funcaoSugerida', () => {
  it('no conselho, o coordenador e o vice coordenam o conselho e o secretário secretaria', () => {
    expect(funcaoSugerida('CONSELHO', 'COORDENADOR')).toBe('Coordenador(a) do conselho');
    expect(funcaoSugerida('CONSELHO', 'secretario-grupo')).toBe('Vice-coordenador(a) do conselho');
    expect(funcaoSugerida('CONSELHO', 'SECRETARIO_CONSELHO')).toBe('Secretário(a)');
  });

  it('na pastoral, o coordenador coordena a pastoral e o secretário de grupo secretaria', () => {
    expect(funcaoSugerida('PASTORAL', 'COORDENADOR')).toBe('Coordenador(a) de pastoral ou organismo');
    expect(funcaoSugerida('PASTORAL', 'SECRETARIO_GRUPO')).toBe('Secretário(a)');
  });

  it('membro comum não sugere função', () => {
    expect(funcaoSugerida('CONSELHO', 'MEMBRO_CONSELHO_PASTORAL')).toBeUndefined();
    expect(funcaoSugerida('PASTORAL', 'VOLUNTARIO')).toBeUndefined();
  });
});
