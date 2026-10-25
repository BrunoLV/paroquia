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
  /** Função na reunião que o vínculo do membro com o órgão sugere (secretário, coordenador, vice). */
  funcaoSugerida?: string;
  /** Nome da pastoral, movimento ou serviço de origem, quando a pessoa não é do órgão convocante. */
  organismo?: string;
}

/**
 * Função que o papel do membro no órgão sugere. O coordenador e o vice-coordenador (papel de secretário de grupo)
 * são membros do órgão: nos conselhos exercem a coordenação do conselho; nas pastorais, a da pastoral.
 */
export const funcaoSugerida = (tipoOrganizacao: string | undefined, papel: string | undefined): string | undefined => {
  const codigo = (papel ?? '').toLowerCase().replace(/_/g, '-');
  const conselho = tipoOrganizacao === 'CONSELHO';
  if (codigo === 'secretario-conselho') return 'Secretário(a)';
  if (codigo === 'coordenador') return conselho ? 'Coordenador(a) do conselho' : 'Coordenador(a) de pastoral ou organismo';
  if (codigo === 'secretario-grupo') return conselho ? 'Vice-coordenador(a) do conselho' : 'Secretário(a)';
  return undefined;
};

/**
 * Função de quem vem de um organismo para a reunião do conselho: o coordenador representa a pastoral e o vice a
 * representa também; o papel de secretário de grupo é do organismo, não da reunião, então não sugere "Secretário(a)".
 */
export const funcaoSugeridaDeOrganismo = (papel: string | undefined): string | undefined => {
  const codigo = (papel ?? '').toLowerCase().replace(/_/g, '-');
  if (codigo === 'coordenador') return 'Coordenador(a) de pastoral ou organismo';
  if (codigo === 'secretario-grupo') return 'Representante de pastoral ou movimento';
  return undefined;
};

/**
 * Organismos (pastorais, movimentos e serviços) filhos do conselho convocante cujos membros podem constar numa
 * reunião de presença livre. Nas de presença só de membros ou quantitativa, a lista continua restrita ao órgão.
 */
export const organismosDoConvocante = (
  organizacoes: Organizacao[], regras: TipoReuniaoInfo | undefined, convocanteId: string,
): string[] =>
  regras?.modoPresenca === 'NOMINAL_LIVRE' && convocanteId
    ? organizacoes.filter((o) => o.ativo && o.parentId === convocanteId && o.tipo !== 'CONSELHO').map((o) => o.id)
    : [];

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
