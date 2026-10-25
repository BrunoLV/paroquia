/**
 * Modelo do contexto de conselho no frontend: atas de reunião da paróquia e das comunidades, por tipo de
 * reunião. Espelha os contratos da API; as regras de autorização vêm prontas em `acoes` (calculadas pelo servidor),
 * e as regras de cada tipo vêm de `GET /conselhos/tipos-reuniao`, de modo que a tela não as duplica.
 */

export type TipoReuniao =
  | 'REUNIAO_CPP'
  | 'REUNIAO_CPC'
  | 'REUNIAO_CAEP'
  | 'REUNIAO_CONJUNTA_CPP_CAEP'
  | 'REUNIAO_PASTORAL'
  | 'REUNIAO_COORDENADORES'
  | 'ASSEMBLEIA_PAROQUIAL'
  | 'ASSEMBLEIA_COMUNITARIA'
  | 'REUNIAO_ORGANISMO'
  | 'PLANEJAMENTO_PASTORAL'
  | 'COMISSAO_ESPECIAL'
  | 'REUNIAO_ANIMADORES';

export type StatusAta = 'RASCUNHO' | 'PENDENTE_HOMOLOGACAO' | 'HOMOLOGADA' | 'VETADA' | 'REGISTRADA';

export type AcaoAta =
  | 'LER'
  | 'REDIGIR'
  | 'SUBMETER'
  | 'HOMOLOGAR'
  | 'VETAR'
  | 'REABRIR'
  | 'RETIFICAR'
  | 'EXPORTAR'
  | 'VER_TRILHA';

export type ModoPresenca = 'NOMINAL_MEMBROS' | 'NOMINAL_LIVRE' | 'QUANTITATIVA';
export type SituacaoPresenca = 'PRESENTE' | 'AUSENTE_JUSTIFICADA' | 'AUSENTE';
export type ResultadoDeliberacao = 'PARECER_FAVORAVEL' | 'PARECER_CONTRARIO' | 'ADIADA' | 'INFORMATIVA';
export type CaraterReuniao = 'ORDINARIA' | 'EXTRAORDINARIA';
export type ModalidadeReuniao = 'PRESENCIAL' | 'VIRTUAL' | 'HIBRIDA';
export type AprovacaoAtaAnterior = 'NAO_APLICAVEL' | 'APROVADA' | 'APROVADA_COM_RESSALVAS';

export type FormatoExportacao = 'PDF' | 'DOCX';
export type LayoutExportacao = 'ESTRUTURADA' | 'NARRATIVA';
export type ConteudoExportacao = 'ATA_COMPLETA' | 'SOMENTE_DECISOES';

export const STATUS_ATA_LABEL: Record<StatusAta, string> = {
  RASCUNHO: 'Rascunho',
  PENDENTE_HOMOLOGACAO: 'Pendente de homologação',
  HOMOLOGADA: 'Homologada',
  VETADA: 'Vetada',
  REGISTRADA: 'Registrada',
};

export const RESULTADO_LABEL: Record<ResultadoDeliberacao, string> = {
  PARECER_FAVORAVEL: 'Parecer favorável',
  PARECER_CONTRARIO: 'Parecer contrário',
  ADIADA: 'Adiada',
  INFORMATIVA: 'Informativa',
};

export const SITUACAO_PRESENCA_LABEL: Record<SituacaoPresenca, string> = {
  PRESENTE: 'Presente',
  AUSENTE_JUSTIFICADA: 'Ausência justificada',
  AUSENTE: 'Ausente',
};

export const CARATER_LABEL: Record<CaraterReuniao, string> = {
  ORDINARIA: 'Ordinária',
  EXTRAORDINARIA: 'Extraordinária',
};

export const MODALIDADE_LABEL: Record<ModalidadeReuniao, string> = {
  PRESENCIAL: 'Presencial',
  VIRTUAL: 'Virtual',
  HIBRIDA: 'Híbrida',
};

export const APROVACAO_ATA_ANTERIOR_LABEL: Record<AprovacaoAtaAnterior, string> = {
  NAO_APLICAVEL: 'Não se aplica',
  APROVADA: 'Aprovada',
  APROVADA_COM_RESSALVAS: 'Aprovada com ressalvas',
};

/** Selada: já produz efeito e não admite edição; correções vêm por ata retificadora. */
export const ataSelada = (status: StatusAta): boolean => status === 'HOMOLOGADA' || status === 'REGISTRADA';

export interface TipoReuniaoInfo {
  codigo: TipoReuniao;
  rotulo: string;
  exigeHomologacao: boolean;
  aplicaQuorum: boolean;
  modoPresenca: ModoPresenca;
  permiteVotacao: boolean;
  exigeOrganizacaoAssociada: boolean;
  tiposConvocantes: string[];
  subtiposConselho: string[];
  papeisRedatores: string[];
}

export interface Votacao {
  favor: number;
  contra: number;
  abstencoes: number;
}

export interface ConflitoInteresse {
  declaranteFielId: string;
  declaranteNome: string;
  descricao: string;
}

export interface Deliberacao {
  ordem: number;
  texto: string;
  resultado: ResultadoDeliberacao;
  votacao: Votacao | null;
  conflitos: ConflitoInteresse[];
}

export interface ItemPauta {
  ordem: number;
  titulo: string;
  relato: string | null;
  deliberacoes: Deliberacao[];
}

export interface Presenca {
  fielId: string;
  nome: string;
  funcao: string | null;
  situacao: SituacaoPresenca;
}

export interface ConteudoAta {
  titulo: string;
  dataReuniao: string;
  local: string;
  carater: CaraterReuniao;
  modalidade: ModalidadeReuniao;
  presencas: Presenca[];
  totalParticipantes: number | null;
  ataAnteriorId: string | null;
  aprovacaoAtaAnterior: AprovacaoAtaAnterior;
  ressalvasAtaAnterior: string | null;
  itens: ItemPauta[];
}

export interface Quorum {
  membrosConvocados: number;
  minimoNecessario: number;
  presentes: number;
  atingido: boolean;
}

export interface Ata {
  id: string;
  tipo: TipoReuniao;
  tipoRotulo: string;
  organizacaoConvocanteId: string;
  organizacaoConvocante: string | null;
  organizacaoAssociadaId: string | null;
  organizacaoAssociada: string | null;
  status: StatusAta;
  /** Número no formato `003/2026`; nulo enquanto a ata é minuta. */
  numero: string | null;
  conteudo: ConteudoAta;
  quorum: Quorum | null;
  redator: string;
  homologador: string | null;
  motivoVeto: string | null;
  decididoEmUtc: string | null;
  ataRetificadaId: string | null;
  justificativaRetificacao: string | null;
  criadoEmUtc: string;
  acoes: AcaoAta[];
}

export interface AtaResumo {
  id: string;
  tipo: TipoReuniao;
  tipoRotulo: string;
  titulo: string;
  dataReuniao: string;
  organizacaoConvocanteId: string;
  organizacaoConvocante: string | null;
  status: StatusAta;
  numero: string | null;
  acoes: AcaoAta[];
}

export interface PaginaAtas {
  itens: AtaResumo[];
  total: number;
  pagina: number;
  tamanho: number;
}

/** Entrada da trilha de auditoria; sem IP nem estados serializados, que ficam só no banco. */
export interface RegistroAuditoria {
  id: string;
  acao: string;
  ator: string;
  perfil: string | null;
  ocorridoEmUtc: string;
}

export interface FiltroAtas {
  organizacaoId?: string;
  tipo?: TipoReuniao;
  status?: StatusAta;
  de?: string;
  ate?: string;
  pagina?: number;
  tamanho?: number;
}

export interface OpcoesExportacao {
  formato: FormatoExportacao;
  layout: LayoutExportacao;
  conteudo: ConteudoExportacao;
}

/** Corpo enviado ao criar ou substituir o conteúdo de uma ata; a numeração segue a posição nas listas. */
export interface ConteudoAtaPayload {
  titulo: string;
  dataReuniao: string;
  local: string;
  carater: CaraterReuniao;
  modalidade: ModalidadeReuniao;
  presenca: {
    nominal?: { fielId: string; funcao?: string; situacao: SituacaoPresenca }[];
    total?: number;
  };
  ataAnterior: { ataId?: string; aprovacao: AprovacaoAtaAnterior; ressalvas?: string };
  itens: {
    titulo: string;
    relato?: string;
    deliberacoes: {
      texto: string;
      resultado: ResultadoDeliberacao;
      votacao?: Votacao;
      conflitos: { declaranteFielId: string; descricao: string }[];
    }[];
  }[];
}

export interface RascunharAtaPayload {
  tipo: TipoReuniao;
  organizacaoConvocanteId: string;
  organizacaoAssociadaId?: string;
  conteudo: ConteudoAtaPayload;
}
