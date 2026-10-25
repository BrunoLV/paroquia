import { Organizacao } from '@/shared/domain/types/types';
import { TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';

/** Pessoa que pode constar na presença ou declarar conflito: membro ativo do órgão convocante ou associado. */
export interface Candidato {
  id: string;
  nome: string;
}

/** Órgãos que o tipo de reunião aceita como convocante, conforme as regras vindas do servidor. */
export const convocantesDoTipo = (organizacoes: Organizacao[], regras: TipoReuniaoInfo | undefined): Organizacao[] => {
  if (!regras) return [];
  return organizacoes.filter(
    (o) =>
      o.ativo &&
      regras.tiposConvocantes.includes(o.tipo) &&
      (o.tipo !== 'CONSELHO' ||
        regras.subtiposConselho.length === 0 ||
        (o.subtipoConselho != null && regras.subtiposConselho.includes(o.subtipoConselho))),
  );
};

/** Órgão associado da reunião conjunta: sempre o conselho de assuntos econômicos (CAEP). */
export const associadosDoTipo = (organizacoes: Organizacao[]): Organizacao[] =>
  organizacoes.filter((o) => o.ativo && o.tipo === 'CONSELHO' && o.subtipoConselho === 'CAEP');
