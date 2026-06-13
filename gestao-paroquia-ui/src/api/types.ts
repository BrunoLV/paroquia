export interface Organizacao {
  id: string;
  nome: string;
  tipo: string;
  contato: string | null;
  ativo: boolean;
}

export interface Local {
  id: string;
  nome: string;
  tipo: string;
  endereco: string | null;
  capacidade: number | null;
  caracteristicas: string | null;
  ativo: boolean;
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

export interface Evento {
  id: string;
  titulo: string;
  descricao: string;
  categoria: string;
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
  conflictState: string | null;
  conflictReason: string | null;
  adicionadoExtraJustificativa?: string | null;
  pendingApprovalId?: string;
  pendingSolicitanteId?: string;
  pendingTipoSolicitacao?: string;
  pendingActionType?: string;
}

export interface AprovacaoResponse {
  id: string;
  eventoId: string | null;
  tipoSolicitacao: 'CRIACAO_EVENTO' | 'EDICAO_EVENTO' | 'ALTERACAO_HORARIO' | 'CANCELAMENTO' | 'RECLASSIFICACAO' | 'OUTRO' | 'UNKNOWN_LEGACY';
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

