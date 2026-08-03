export type TipoOrganismoParoquial =
  | 'MATRIZ'
  | 'COMUNIDADE'
  | 'CLERO'
  | 'CONSELHO_PASTORAL'
  | 'CONSELHO_ECONOMICO'
  | 'PASTORAL'
  | 'MOVIMENTO'
  | 'OUTRO';

export type SetorPastoral =
  | 'SANTIFICACAO_LITURGIA'
  | 'FORMACAO_ENSINAR'
  | 'SOCIAL_CARIDADE';

export type PapelEclesial =
  | 'ADMIN'
  | 'BISPO_DIOCESANO'
  | 'PAROCO'
  | 'VIGARIO'
  | 'DIACONO'
  | 'PADRE'
  | 'MESA_CPP'
  | 'MEMBRO_CPP'
  | 'MEMBRO_CAE'
  | 'PRESIDENTE_CPC'
  | 'MEMBRO_CPC'
  | 'COORD_PASTORAL_GLOBAL'
  | 'COORD_PASTORAL_LOCAL'
  | 'COORD_PASTORAL'
  | 'COORD_MOVIMENTO'
  | 'VOLUNTARIO_PASTORAL_LOCAL'
  | 'MEMBRO_PASTORAL'
  | 'MEMBRO_MOVIMENTO'
  | 'SECRETARIO';

export const PAPEL_ECLESIAL_LABELS: Record<string, string> = {
  ADMIN: 'Administrador do Sistema',
  BISPO_DIOCESANO: 'Bispo Diocesano',
  PAROCO: 'Pároco',
  VIGARIO: 'Vigário Paroquial',
  DIACONO: 'Diácono',
  PADRE: 'Padre',
  MESA_CPP: 'Mesa Diretora do CPP',
  MEMBRO_CPP: 'Membro do CPP',
  MEMBRO_CAE: 'Membro do CAE',
  PRESIDENTE_CPC: 'Presidente do CPC',
  MEMBRO_CPC: 'Membro do CPC',
  COORD_PASTORAL_GLOBAL: 'Coordenador Pastoral Global',
  COORD_PASTORAL_LOCAL: 'Coordenador Pastoral Local',
  COORD_PASTORAL: 'Coordenador Pastoral',
  COORD_MOVIMENTO: 'Coordenador de Movimento',
  VOLUNTARIO_PASTORAL_LOCAL: 'Voluntário Pastoral Local',
  MEMBRO_PASTORAL: 'Membro de Pastoral',
  MEMBRO_MOVIMENTO: 'Membro de Movimento',
  SECRETARIO: 'Secretário',

  // Aliases for stored values / legacy compatibility
  MESA_DIRETORA_CPP: 'Mesa Diretora do CPP',
  COORDENADOR_PASTORAL_GLOBAL: 'Coordenador Pastoral Global',
  COORDENADOR_PASTORAL_LOCAL: 'Coordenador Pastoral Local',
  COORDENADOR_PASTORAL: 'Coordenador Pastoral',
  COORDENADOR_MOVIMENTO: 'Coordenador de Movimento',
};

export interface Organizacao {
  id: string;
  nome: string;
  tipo: TipoOrganismoParoquial | string;
  contato: string | null;
  coordenador?: string | null;
  viceCoordenador?: string | null;
  ativo: boolean;
  parentId?: string | null;
  pastoralOficialId?: string | null;
  setorPastoral?: SetorPastoral | null;
}

export interface PastoralMovimentoOficial {
  id: string;
  nome: string;
  tipo: 'PASTORAL' | 'MOVIMENTO';
}

export interface Local {
  id: string;
  nome: string;
  tipo: string;
  endereco: string | null;
  capacidade: number | null;
  caracteristicas: string | null;
  ativo: boolean;
  organizacaoId?: string | null;
  organizacaoNome?: string | null;
}

export interface LocalFormValues {
  nome: string;
  tipo: string;
  endereco?: string;
  capacidade?: number | null;
  caracteristicas?: string;
  ativo?: boolean;
  organizacaoId?: string | null;
}

export interface Projeto {
  id: string;
  nome: string;
  descricao: string | null;
  inicio: string;
  fim: string;
  organizacaoResponsavelId: string;
  status: 'ABERTO' | 'EM_ANDAMENTO' | 'CANCELADO' | 'CONCLUIDO';
  updated?: boolean;
}

export type CategoriaEvento =
  | 'SOLENIDADE'
  | 'PASTORAL'
  | 'SOCIAL'
  | 'LITURGICO'
  | 'ADMINISTRATIVO'
  | 'SACRAMENTAL'
  | 'FORMATIVO'
  | 'ASSISTENCIAL'
  | 'MOVIMENTO';

export interface Evento {
  id: string;
  titulo: string;
  descricao: string;
  categoria: CategoriaEvento | string;
  organizacaoResponsavelId: string;
  projetoId: string | null;
  nomeProjeto: string | null;
  localId: string | null;
  inicio: string;
  fim: string;
  status: string;
  participantes?: string[];
  recorrenciaId?: string | null;
  recorrencia?: any;
  estadoConflito: string | null;
  motivoConflito: string | null;
  adicionadoExtraJustificativa?: string | null;
  igrejaVinculoId?: string | null;
  aprovacaoPendenteId?: string;
  solicitantePendenteId?: string;
  tipoSolicitacaoPendente?: string;
  tipoAcaoPendente?: string;
  /** Nível de prioridade da agenda (1=Solenidade, 2=Preceito, 3=Pastoral, 4=Movimento) */
  nivelPrioridade?: 1 | 2 | 3 | 4;
  /** Liberado pelo Conselho Pastoral Paroquial (CPP) para conflitos de agenda */
  autorizadoPeloCpp?: boolean;
  setorPastoral?: SetorPastoral | null;
  /** Permite ao Pároco ignorar conflitos de agenda eclesial */
  bypassAgendaConflict?: boolean;
}

export interface CreateEventoPayload {
  titulo: string;
  descricao?: string;
  categoria: string;
  organizacaoResponsavelId: string;
  projetoId?: string | null;
  localId?: string | null;
  inicio: string;
  fim: string;
  status?: string;
  participantes?: string[];
  adicionadoExtraJustificativa?: string | null;
  igrejaVinculoId?: string | null;
  bypassAgendaConflict?: boolean;
}

export interface UpdateEventoPayload {
  titulo?: string;
  descricao?: string;
  categoria?: string;
  inicio?: string;
  fim?: string;
  status?: string;
  adicionadoExtraJustificativa?: string | null;
  canceladoMotivo?: string | null;
  organizacaoResponsavelId?: string;
  participantes?: string[];
  editScope?: 'THIS_EVENT' | 'THIS_AND_FUTURE' | 'ALL_EVENTS';
  projetoId?: string | null;
  localId?: string | null;
  igrejaVinculoId?: string | null;
  bypassAgendaConflict?: boolean;
}

export interface AprovacaoResponse {
  id: string;
  eventoId: string | null;
  tipoSolicitacao: 'CRIACAO_EVENTO' | 'EDICAO_EVENTO' | 'ALTERACAO_HORARIO' | 'CANCELAMENTO' | 'RECLASSIFICACAO' | 'ALOCACAO_ATIVO_EMPRESTIMO' | 'OUTRO' | 'UNKNOWN_LEGACY';
  status: 'PENDENTE' | 'APROVADA' | 'REPROVADA' | 'FALHA_EXECUCAO';
  aprovadorPapel: string | null;
  criadoEmUtc: string;
  decididoEmUtc: string | null;
  solicitanteId: string;
  aprovadorId: string | null;
  mensagemErroExecucao: string | null;
  actionPayloadJson: string | null;
}

export interface AprovacaoDecisionRequest {
  status: 'APROVADA' | 'REPROVADA';
  observacao?: string;
}

export interface AprovacaoDecisionResponse {
  id: string;
  status: string;
  actionExecution: {
    outcome: string;
    eventoId: string;
    eventStatus: string;
    errorCode: string | null;
  } | null;
}

export type AnoParoquialStatus = 'EM_PLANEJAMENTO' | 'FECHADO' | 'CONCLUIDO';

export interface AnoParoquial {
  ano: number;
  status: AnoParoquialStatus;
  dataFechamentoUtc: string | null;
  updatedAt: string;
}

export interface ProjetoAprovacaoResponse {
  id: string;
  projetoId: string | null;
  tipoSolicitacao: 'CRIACAO' | 'CANCELAMENTO';
  status: 'PENDENTE' | 'APROVADA' | 'REPROVADA';
  solicitanteId: string;
  criadoEmUtc: string;
  decididoEmUtc: string | null;
  actionPayloadJson: string | null;
  cancelarEventosVinculados: boolean;
}

export interface GeneralSummary {
  totalEventosMes: number;
  projetosAtivos: number;
  solicitacoesPendentes: number;
}

export interface EventDistribution {
  organizacaoNome: string;
  quantidadeEventos: number;
}

export interface ProjectDetail {
  nome: string;
  status: string;
  percentualTempoDecorrido: number;
  totalEventos: number;
  eventosRealizados: number;
  emRisco: boolean;
  envolvidos: string[];
}

export interface WeeklyEvent {
  titulo: string;
  inicio: string;
  localNome: string;
  organizacaoNome: string;
}

export interface DashboardMetrics {
  resumoGeral: GeneralSummary;
  eventosPorOrganizacao: EventDistribution[];
  projetosAtivosDetalhes: ProjectDetail[];
  eventosSemana: WeeklyEvent[];
}

export type TipoObservacao = 'NOTA' | 'JUSTIFICATIVA' | 'APROVACAO' | 'REPROVACAO' | 'CANCELAMENTO' | 'AJUSTE_HORARIO';

export interface Observacao {
  id: string;
  eventoId: string;
  usuarioId: string;
  tipo: TipoObservacao;
  conteudo: string;
  criadoEmUtc: string;
}
export interface OrganogramaNode {
  id: string;
  label: string;
  type: 'PAROQUIA' | 'ORGANIZACAO' | 'MEMBRO' | 'GRUPO';
  subtype: string;
  responsavelNome: string | null;
  responsavelPapel: string | null;
  viceResponsavelNome: string | null;
  children: OrganogramaNode[];
  orgChildren?: OrganogramaNode[];
}

export type StatusHomologacao = 'PENDENTE_HOMOLOGACAO' | 'HOMOLOGADO' | 'VETADO';

export interface CpcDecisao {
  id: string;
  titulo: string;
  pautaDescricao: string;
  dataReuniao: string;
  comunidadeId: string;
  conselhoTipo: string;
  statusHomologacao: StatusHomologacao;
  solicitanteId: string;
  solicitanteNome?: string | null;
  homologadorId?: string | null;
  motivoVeto?: string | null;
  criadoEmUtc: string;
  decididoEmUtc?: string | null;
}

export type AtivoCategoria = 'ELETRONICO' | 'MOBILIARIO' | 'PARAMENTO' | 'INFRAESTRUTURA';

export type AtivoStatus = 'DISPONIVEL' | 'EM_MANUTENCAO' | 'BAIXADO' | 'ALOCADO';

export interface AtivoPatrimonial {
  id: string;
  nome: string;
  numeroTombamento: string;
  categoria: AtivoCategoria;
  status: AtivoStatus;
  reservavel: boolean;
  organizacaoId: string;
  dataAquisicao?: string | null;
  valorEstimado?: number | null;
  criadoEmUtc: string;
}

export interface CreateAtivoPayload {
  nome: string;
  categoria: AtivoCategoria;
  organizacaoId: string;
  reservavel: boolean;
  dataAquisicao?: string | null;
  valorEstimado?: number | null;
}

export interface TransferirAtivoPayload {
  novaOrganizacaoId: string;
  justificativa: string;
}

export interface AlocarAtivoPayload {
  eventoId?: string | null;
  justificativaAlocacao: string;
  dataInicio: string;
  dataFim: string;
}

export interface DesalocarAtivoPayload {
  observacao?: string | null;
}

export interface IniciarManutencaoPayload {
  dataInicio: string;
  dataFim: string;
  motivo: string;
}

export interface CancelarAlocacaoPayload {
  motivoCancelamento: string;
}

export type AtivoAlocacaoTipo = 'ALOCACAO_EVENTO' | 'MANUTENCAO';

export type AtivoAlocacaoStatus = 'PENDENTE_APROVACAO' | 'CONFIRMADA' | 'ATRASADA' | 'DEVOLVIDA' | 'CANCELADA';

export interface AtivoAlocacaoResponse {
  id: string;
  ativoId: string;
  ativoNome: string | null;
  numeroTombamento: string | null;
  tipo: AtivoAlocacaoTipo;
  eventoId: string | null;
  organizacaoOrigemId: string;
  organizacaoDestinoId: string | null;
  emprestimo: boolean;
  dataInicio: string;
  dataFim: string;
  status: AtivoAlocacaoStatus;
  impactadaPorAtraso: boolean;
  criadoEmUtc: string;
  devolvidoEmUtc: string | null;
}

export interface AlocacaoAtivoAprovacaoPendenteResponse {
  solicitacaoAprovacaoId: string;
  status: string;
  ativoId: string;
  eventoId: string;
  mensagem: string;
}

export type AlocarAtivoResultado =
  | { kind: 'ALOCADO'; ativo: AtivoPatrimonial }
  | { kind: 'PENDENTE'; pendente: AlocacaoAtivoAprovacaoPendenteResponse };

export interface MetricasOciosidade {
  totalAtivos: number;
  totalDisponiveis: number;
  totalAlocados: number;
  totalEmManutencao: number;
  totalBaixados: number;
  taxaOciosidade: number;
  distribuicaoPorCategoria: Record<string, number>;
}

