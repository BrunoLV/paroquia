import { Organizacao } from '@/shared/domain/types/types';
import { TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import { describe, expect, it } from 'vitest';
import { funcaoSugerida, funcaoSugeridaDeOrganismo, organismosDoConvocante } from '@/contextos/conselho/presentation/components/ataFormTipos';

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

describe('organismos do convocante', () => {
  const org = (id: string, tipo: string, parentId: string | null, ativo = true) =>
    ({ id, tipo, parentId, ativo }) as unknown as Organizacao;
  const organizacoes = [
    org('cpp', 'CONSELHO', null),
    org('catequese', 'PASTORAL', 'cpp'),
    org('rcc', 'MOVIMENTO', 'cpp'),
    org('inativa', 'PASTORAL', 'cpp', false),
    org('caep', 'CONSELHO', 'cpp'),
    org('outra', 'PASTORAL', 'cpc'),
  ];
  const regras = (modoPresenca: string) => ({ modoPresenca }) as unknown as TipoReuniaoInfo;

  it('presença livre traz as pastorais, movimentos e serviços ativos filhos do conselho', () => {
    expect(organismosDoConvocante(organizacoes, regras('NOMINAL_LIVRE'), 'cpp')).toEqual(['catequese', 'rcc']);
  });

  it('presença só de membros ou quantitativa não amplia a lista', () => {
    expect(organismosDoConvocante(organizacoes, regras('NOMINAL_MEMBROS'), 'cpp')).toEqual([]);
    expect(organismosDoConvocante(organizacoes, regras('QUANTITATIVA'), 'cpp')).toEqual([]);
  });

  it('sem convocante escolhido não há organismos', () => {
    expect(organismosDoConvocante(organizacoes, regras('NOMINAL_LIVRE'), '')).toEqual([]);
  });

  it('o coordenador do organismo o representa, o secretário do grupo não vira secretário da reunião', () => {
    expect(funcaoSugeridaDeOrganismo('COORDENADOR')).toBe('Coordenador(a) de pastoral ou organismo');
    expect(funcaoSugeridaDeOrganismo('secretario-grupo')).toBe('Representante de pastoral ou movimento');
    expect(funcaoSugeridaDeOrganismo('MEMBRO_ATIVO')).toBeUndefined();
  });
});
