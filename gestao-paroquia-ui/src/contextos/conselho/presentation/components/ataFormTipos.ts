import { Organizacao } from '@/shared/domain/types/types';
import { TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';

/**
 * Pessoa que pode constar na presença ou declarar conflito: membro ativo do órgão convocante ou associado, ou
 * clérigo. O Pároco (`paroco`) preside por direito próprio os conselhos.
 */
export interface Candidato {
  id: string;
  nome: string;
  clerigo?: boolean;
  paroco?: boolean;
}

/** Órgãos que o tipo aceita como convocante e pelos quais o usuário pode redigir, conforme o servidor. */
export const convocantesDoTipo = (organizacoes: Organizacao[], regras: TipoReuniaoInfo | undefined): Organizacao[] => {
  if (!regras) return [];
  return organizacoes.filter(
    (o) =>
      o.ativo &&
      regras.organizacoesRedacao.includes(o.id) &&
      regras.tiposConvocantes.includes(o.tipo) &&
      (o.tipo !== 'CONSELHO' ||
        regras.subtiposConselho.length === 0 ||
        (o.subtipoConselho != null && regras.subtiposConselho.includes(o.subtipoConselho))),
  );
};

/** Órgão associado da reunião conjunta: o CAEP da mesma unidade (mesma organização-mãe) do órgão convocante. */
export const associadosDoTipo = (organizacoes: Organizacao[], convocante: Organizacao | undefined): Organizacao[] =>
  convocante
    ? organizacoes.filter((o) => o.ativo && o.tipo === 'CONSELHO' && o.subtipoConselho === 'CAEP'
      && (o.parentId ?? null) === (convocante.parentId ?? null))
    : [];
