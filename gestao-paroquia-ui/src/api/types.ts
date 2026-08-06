export type TipoOrganismoParoquial =
  | 'MATRIZ'
  | 'COMUNIDADE'
  | 'CLERO'
  | 'CONSELHO_PASTORAL'
  | 'CONSELHO_ECONOMICO'
  | 'PASTORAL'
  | 'MOVIMENTO'
  | 'OUTRO';

export type Vocacao = 'LEIGO' | 'SACERDOTE' | 'DIACONO' | 'RELIGIOSO';

export const VOCACAO_LABELS: Record<Vocacao, string> = {
  LEIGO: 'Leigo(a)',
  SACERDOTE: 'Sacerdote',
  DIACONO: 'Diácono',
  RELIGIOSO: 'Religioso(a)',
};

export interface Fiel {
  id: string;
  nome: string;
  cpf: string;
  dataNascimento: string;
  email: string;
  telefone: string;
  endereco: string;
  vocacao: Vocacao;
  ativo: boolean;
  batizado: boolean;
  primeiraComunhao: boolean;
  crismado: boolean;
  casadoIgrejaCatolica: boolean;
}

export interface CreateFielPayload {
  nome: string;
  cpf: string;
  dataNascimento: string;
  email: string;
  telefone: string;
  endereco: string;
  vocacao: Vocacao;
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
  casadoIgrejaCatolica?: boolean;
}

export interface UpdateFielPayload {
  nome?: string;
  cpf?: string;
  dataNascimento?: string;
  email?: string;
  telefone?: string;
  endereco?: string;
  vocacao?: Vocacao;
  ativo?: boolean;
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
  casadoIgrejaCatolica?: boolean;
}

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

/**
 * Nó da hierarquia organizacional da paróquia (Matriz, Comunidades, pastorais,
 * movimentos, clero). A mesma estrutura em árvore (via `parentId`) é usada tanto
 * para navegação/organograma quanto para escopo de autorização de eventos/projetos.
 */
export interface Organizacao {
  id: string;
  nome: string;
  tipo: TipoOrganismoParoquial | string;
  contato: string | null;
  coordenador?: string | null;
  viceCoordenador?: string | null;
  ativo: boolean;
  /** ID da organização pai na hierarquia, ou `null`/ausente para nós de topo (ex: Matriz). */
  parentId?: string | null;
  /** ID do registro oficial de Pastoral/Movimento vinculado, quando `tipo` for `PASTORAL` ou `MOVIMENTO`. */
  pastoralOficialId?: string | null;
  setorPastoral?: SetorPastoral | null;
}

/**
 * Cadastro oficial de uma Pastoral ou Movimento, referenciado por `Organizacao.pastoralOficialId`
 * para distinguir o registro "oficial" (nome canônico) das organizações que o representam localmente.
 */
export interface PastoralMovimentoOficial {
  id: string;
  nome: string;
  tipo: 'PASTORAL' | 'MOVIMENTO';
}

/**
 * Espaço físico (sala, salão, igreja, etc.) onde eventos podem ser realizados.
 * Usado para checagem de conflito de agenda (mesmo local/período) e para alocação
 * de ativos patrimoniais vinculados a um evento nesse local.
 */
export interface Local {
  id: string;
  nome: string;
  tipo: string;
  endereco: string | null;
  /** Lotação máxima de pessoas; `null` quando não informada/não aplicável. */
  capacidade: number | null;
  /** Observações livres sobre o espaço (ex: recursos disponíveis, restrições de uso). */
  caracteristicas: string | null;
  ativo: boolean;
  /** Organização "dona" do espaço, quando o local pertence a uma comunidade/organização específica. */
  organizacaoId?: string | null;
  organizacaoNome?: string | null;
}

/** Formulário de criação/edição de um {@link Local} — mesmos campos, mas todos opcionais exceto os obrigatórios de cadastro. */
export interface LocalFormValues {
  nome: string;
  tipo: string;
  endereco?: string;
  capacidade?: number | null;
  caracteristicas?: string;
  ativo?: boolean;
  organizacaoId?: string | null;
}

/**
 * Projeto pastoral/administrativo que agrupa uma série de eventos ao longo de um período.
 * O `status` segue seu próprio ciclo de vida, independente do status de cada evento vinculado.
 */
export interface Projeto {
  id: string;
  nome: string;
  descricao: string | null;
  inicio: string;
  fim: string;
  organizacaoResponsavelId: string;
  status: 'ABERTO' | 'EM_ANDAMENTO' | 'CANCELADO' | 'CONCLUIDO';
  /** Flag de UI: marca que o registro acabou de ser atualizado, para destacar/realçar na lista. */
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

/**
 * Evento de calendário — a entidade mais conectada do domínio. Além dos dados básicos
 * de agendamento, carrega sinais opcionais de conflito de agenda (`estadoConflito`/
 * `motivoConflito`) e de aprovação pendente (`aprovacaoPendenteId` e campos relacionados),
 * populados pela UI (ver `src/api/eventos.ts` e `Calendar.tsx`) a partir de uma
 * `AprovacaoResponse` associada, para evitar uma segunda chamada à API.
 */
export interface Evento {
  id: string;
  titulo: string;
  descricao: string;
  categoria: CategoriaEvento | string;
  organizacaoResponsavelId: string;
  /** Projeto ao qual este evento pertence, ou `null` se for um evento avulso. */
  projetoId: string | null;
  nomeProjeto: string | null;
  localId: string | null;
  inicio: string;
  fim: string;
  status: string;
  /** IDs dos membros/participantes convidados/vinculados ao evento. */
  participantes?: string[];
  /** ID da série de recorrência à qual este evento pertence, ou `null`/ausente se não foi gerado por uma regra de recorrência. */
  recorrenciaId?: string | null;
  recorrencia?: any;
  /**
   * Nome de `EstadoConflitoAgenda` — hoje o único valor possível é `"PENDENTE"` — quando a
   * política de integridade do calendário detecta sobreposição de agenda no mesmo
   * espaço/período para este evento; `null` quando não há conflito detectado. A resolução
   * do conflito (aprovação/rejeição) é tratada pelo fluxo de aprovação, não por este campo.
   */
  estadoConflito: string | null;
  /** Descrição textual do conflito de agenda detectado; preenchido apenas quando `estadoConflito` não é `null`. */
  motivoConflito: string | null;
  adicionadoExtraJustificativa?: string | null;
  igrejaVinculoId?: string | null;
  /**
   * ID da solicitação de aprovação (`AprovacaoEntity`) criada quando uma alteração a este
   * evento foi desviada para o fluxo de aprovação em vez de ser aplicada de imediato
   * (o ator não tinha autoridade suficiente). Roda em PARALELO ao `status` efetivo do
   * evento — não é, em si, um valor de status. Ausente quando não há solicitação pendente.
   */
  aprovacaoPendenteId?: string;
  /** ID do usuário que fez a solicitação pendente referenciada por `aprovacaoPendenteId`. */
  solicitantePendenteId?: string;
  /** Tipo da solicitação pendente (ex: `CRIACAO_EVENTO`, `EDICAO_EVENTO`, `CANCELAMENTO`) — ver {@link AprovacaoResponse.tipoSolicitacao}. */
  tipoSolicitacaoPendente?: string;
  /** Mesmo valor de `tipoSolicitacaoPendente`, duplicado para uso onde a UI trata a solicitação como uma "ação" pendente sobre o evento. */
  tipoAcaoPendente?: string;
  /** Nível de prioridade da agenda (1=Solenidade, 2=Preceito, 3=Pastoral, 4=Movimento) */
  nivelPrioridade?: 1 | 2 | 3 | 4;
  /** Liberado pelo Conselho Pastoral Paroquial (CPP) para conflitos de agenda */
  autorizadoPeloCpp?: boolean;
  setorPastoral?: SetorPastoral | null;
  /** Permite ao Pároco ignorar conflitos de agenda eclesial */
  bypassAgendaConflict?: boolean;
}

/**
 * Payload para criação de um {@link Evento}. Pode resultar tanto na criação direta do
 * evento quanto no roteamento para o fluxo de aprovação, dependendo da autoridade do ator.
 */
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
  /** Permite ao ator ignorar/forçar a criação apesar de um conflito de agenda detectado (restrito a papéis com essa autoridade). */
  bypassAgendaConflict?: boolean;
}

/**
 * Payload para edição de um {@link Evento} existente. Assim como na criação, a alteração
 * pode ser aplicada de imediato ou desviada para o fluxo de aprovação conforme a
 * autoridade do ator sobre o evento/organização.
 */
export interface UpdateEventoPayload {
  titulo?: string;
  descricao?: string;
  categoria?: string;
  inicio?: string;
  fim?: string;
  status?: string;
  adicionadoExtraJustificativa?: string | null;
  /** Motivo textual exigido ao transicionar `status` para cancelado. */
  canceladoMotivo?: string | null;
  organizacaoResponsavelId?: string;
  participantes?: string[];
  /** Escopo da edição quando o evento pertence a uma série recorrente: só esta ocorrência, esta e as futuras, ou toda a série. */
  editScope?: 'THIS_EVENT' | 'THIS_AND_FUTURE' | 'ALL_EVENTS';
  projetoId?: string | null;
  localId?: string | null;
  igrejaVinculoId?: string | null;
  bypassAgendaConflict?: boolean;
}

/**
 * Solicitação de aprovação de uma ação sobre um {@link Evento} (criação, edição, alteração
 * de horário, cancelamento, etc.) quando o ator que a originou não tinha autoridade
 * suficiente para aplicá-la diretamente. Consultada pela página de Aprovações e mesclada
 * em campos `*Pendente*` no objeto `Evento` exibido no calendário.
 */
export interface AprovacaoResponse {
  id: string;
  eventoId: string | null;
  tipoSolicitacao: 'CRIACAO_EVENTO' | 'EDICAO_EVENTO' | 'ALTERACAO_HORARIO' | 'CANCELAMENTO' | 'RECLASSIFICACAO' | 'ALOCACAO_ATIVO_EMPRESTIMO' | 'OUTRO' | 'UNKNOWN_LEGACY';
  status: 'PENDENTE' | 'APROVADA' | 'REPROVADA' | 'FALHA_EXECUCAO';
  /** Papel exigido para decidir esta solicitação (ex: quem pode aprovar/reprovar), ou `null` se não restrito a um papel específico. */
  aprovadorPapel: string | null;
  criadoEmUtc: string;
  decididoEmUtc: string | null;
  solicitanteId: string;
  aprovadorId: string | null;
  /** Mensagem de erro quando a execução automática da ação aprovada falhou (`status` = `FALHA_EXECUCAO`). */
  mensagemErroExecucao: string | null;
  /** Payload JSON serializado com os dados da ação original (ex: campos do evento a criar/editar), reidratado na aprovação. */
  actionPayloadJson: string | null;
}

/** Requisição para decidir (aprovar/reprovar) uma solicitação de aprovação pendente. */
export interface AprovacaoDecisionRequest {
  status: 'APROVADA' | 'REPROVADA';
  observacao?: string;
}

/** Resultado de uma decisão de aprovação, incluindo o desfecho da execução automática da ação quando aprovada. */
export interface AprovacaoDecisionResponse {
  id: string;
  status: string;
  /** Resultado da execução da ação aprovada sobre o evento; `null` quando a solicitação não envolveu execução automática (ex: foi reprovada). */
  actionExecution: {
    outcome: string;
    eventoId: string;
    eventStatus: string;
    errorCode: string | null;
  } | null;
}

export type AnoParoquialStatus = 'EM_PLANEJAMENTO' | 'FECHADO' | 'CONCLUIDO';

/**
 * Ano paroquial — controla o ciclo de vida anual (planejamento, fechamento) que
 * habilita/bloqueia o provisionamento de solenidades e eventos para Matriz e Comunidades.
 */
export interface AnoParoquial {
  ano: number;
  status: AnoParoquialStatus;
  /** Data/hora em que o ano foi fechado; `null` enquanto ainda está em planejamento. */
  dataFechamentoUtc: string | null;
  updatedAt: string;
}

/**
 * Solicitação de aprovação de uma ação sobre um {@link Projeto} (criação ou cancelamento),
 * análoga à {@link AprovacaoResponse} de eventos, mas roteada pelo fluxo de aprovação de projetos.
 */
export interface ProjetoAprovacaoResponse {
  id: string;
  projetoId: string | null;
  tipoSolicitacao: 'CRIACAO' | 'CANCELAMENTO';
  status: 'PENDENTE' | 'APROVADA' | 'REPROVADA';
  solicitanteId: string;
  criadoEmUtc: string;
  decididoEmUtc: string | null;
  actionPayloadJson: string | null;
  /** Quando `true`, o cancelamento do projeto arrasta consigo o cancelamento dos eventos vinculados a ele. */
  cancelarEventosVinculados: boolean;
}

/** Cartões-resumo do topo do dashboard. */
export interface GeneralSummary {
  totalEventosMes: number;
  projetosAtivos: number;
  /** Total de solicitações de aprovação (eventos/projetos/ativos) ainda sem decisão. */
  solicitacoesPendentes: number;
}

/** Ponto de dados para o gráfico de distribuição de eventos por organização responsável. */
export interface EventDistribution {
  organizacaoNome: string;
  quantidadeEventos: number;
}

/** Resumo de acompanhamento de um projeto ativo, exibido na lista de projetos do dashboard. */
export interface ProjectDetail {
  nome: string;
  status: string;
  /** Percentual do período do projeto já decorrido (0-100), usado para a barra de progresso temporal. */
  percentualTempoDecorrido: number;
  totalEventos: number;
  eventosRealizados: number;
  /** `true` quando o progresso de eventos está atrasado em relação ao tempo decorrido, sinalizando risco de não conclusão. */
  emRisco: boolean;
  /** Nomes das organizações/pessoas envolvidas no projeto. */
  envolvidos: string[];
}

/** Evento resumido para a lista "eventos da semana" do dashboard. */
export interface WeeklyEvent {
  titulo: string;
  inicio: string;
  localNome: string;
  organizacaoNome: string;
}

/** Agregado de métricas exibidas na tela inicial (Dashboard). */
export interface DashboardMetrics {
  resumoGeral: GeneralSummary;
  eventosPorOrganizacao: EventDistribution[];
  projetosAtivosDetalhes: ProjectDetail[];
  eventosSemana: WeeklyEvent[];
}

export type TipoObservacao = 'NOTA' | 'JUSTIFICATIVA' | 'APROVACAO' | 'REPROVACAO' | 'CANCELAMENTO' | 'AJUSTE_HORARIO';

/**
 * Anotação de auditoria/histórico registrada em um evento (nota livre, justificativa,
 * decisão de aprovação/reprovação, cancelamento ou ajuste de horário) — forma a trilha
 * de observações exibida no histórico do evento.
 */
export interface Observacao {
  id: string;
  eventoId: string;
  usuarioId: string;
  tipo: TipoObservacao;
  conteudo: string;
  criadoEmUtc: string;
}
/**
 * Nó da árvore renderizada pelo Organograma. Mistura nós de organização (paróquia,
 * organizações) com nós de membro/grupo — `type`/`subtype` distinguem o papel do nó
 * na árvore visual, que não é exatamente a mesma hierarquia de {@link Organizacao}.
 */
export interface OrganogramaNode {
  id: string;
  label: string;
  type: 'PAROQUIA' | 'ORGANIZACAO' | 'MEMBRO' | 'GRUPO';
  subtype: string;
  responsavelNome: string | null;
  responsavelPapel: string | null;
  viceResponsavelNome: string | null;
  children: OrganogramaNode[];
  /** Sub-organizações filhas (quando o nó é uma organização), mantidas à parte de `children` para permitir renderização/expansão independente do restante da árvore. */
  orgChildren?: OrganogramaNode[];
}

export type StatusHomologacao = 'PENDENTE_HOMOLOGACAO' | 'HOMOLOGADO' | 'VETADO';

/**
 * Decisão de um Conselho Pastoral Comunitário (CPC) registrada em ata, sujeita a
 * homologação (ou veto) por um papel superior antes de produzir efeito.
 */
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
  /** ID de quem homologou ou vetou a decisão; ausente enquanto `statusHomologacao` é `PENDENTE_HOMOLOGACAO`. */
  homologadorId?: string | null;
  /** Justificativa do veto; preenchido apenas quando `statusHomologacao` é `VETADO`. */
  motivoVeto?: string | null;
  criadoEmUtc: string;
  decididoEmUtc?: string | null;
}

export type AtivoCategoria = 'ELETRONICO' | 'MOBILIARIO' | 'PARAMENTO' | 'INFRAESTRUTURA';

export type AtivoStatus = 'DISPONIVEL' | 'EM_MANUTENCAO' | 'BAIXADO' | 'ALOCADO';

/**
 * Ativo patrimonial (equipamento, mobiliário, paramento, etc.) pertencente a uma
 * organização, que pode ser reservado/emprestado para eventos ou colocado em manutenção.
 */
export interface AtivoPatrimonial {
  id: string;
  nome: string;
  /** Número de tombamento patrimonial — identificador físico único do ativo, distinto do `id` interno. */
  numeroTombamento: string;
  categoria: AtivoCategoria;
  status: AtivoStatus;
  /** Indica se este ativo pode ser reservado/alocado para eventos (alguns ativos existem só para controle patrimonial). */
  reservavel: boolean;
  /** Organização que atualmente possui/detém o ativo (posse pode mudar via transferência). */
  organizacaoId: string;
  dataAquisicao?: string | null;
  valorEstimado?: number | null;
  criadoEmUtc: string;
}

/** Payload de cadastro de um novo {@link AtivoPatrimonial}. */
export interface CreateAtivoPayload {
  nome: string;
  categoria: AtivoCategoria;
  organizacaoId: string;
  reservavel: boolean;
  dataAquisicao?: string | null;
  valorEstimado?: number | null;
}

/** Payload para transferir a posse de um ativo para outra organização, exigindo justificativa. */
export interface TransferirAtivoPayload {
  novaOrganizacaoId: string;
  justificativa: string;
}

/**
 * Payload para reservar/alocar um ativo por um período — a um evento específico
 * (`eventoId`) ou apenas a um intervalo de datas (empréstimo sem evento vinculado).
 */
export interface AlocarAtivoPayload {
  eventoId?: string | null;
  justificativaAlocacao: string;
  dataInicio: string;
  dataFim: string;
}

/** Payload para liberar (devolver) um ativo alocado antes ou ao fim do período reservado. */
export interface DesalocarAtivoPayload {
  observacao?: string | null;
}

/** Payload para colocar um ativo em manutenção por um período, tornando-o indisponível para alocação. */
export interface IniciarManutencaoPayload {
  dataInicio: string;
  dataFim: string;
  motivo: string;
}

/** Payload para cancelar uma alocação de ativo já confirmada, exigindo motivo. */
export interface CancelarAlocacaoPayload {
  motivoCancelamento: string;
}

export type AtivoAlocacaoTipo = 'ALOCACAO_EVENTO' | 'MANUTENCAO';

export type AtivoAlocacaoStatus = 'PENDENTE_APROVACAO' | 'CONFIRMADA' | 'ATRASADA' | 'DEVOLVIDA' | 'CANCELADA';

/**
 * Registro de uma alocação (reserva/empréstimo para evento) ou período de manutenção
 * de um {@link AtivoPatrimonial} — o histórico de "quem usou o ativo e quando".
 */
export interface AtivoAlocacaoResponse {
  id: string;
  ativoId: string;
  ativoNome: string | null;
  numeroTombamento: string | null;
  tipo: AtivoAlocacaoTipo;
  eventoId: string | null;
  /** Organização dona do ativo no momento da alocação. */
  organizacaoOrigemId: string;
  /** Organização para a qual o ativo foi emprestado, quando `emprestimo` é `true`; `null` para alocação sem empréstimo (uso pela própria organização de origem). */
  organizacaoDestinoId: string | null;
  /** `true` quando o ativo está sendo emprestado a outra organização (`organizacaoDestinoId`), não apenas reservado para uso interno. */
  emprestimo: boolean;
  dataInicio: string;
  dataFim: string;
  status: AtivoAlocacaoStatus;
  /** `true` quando esta alocação foi empurrada/afetada pelo atraso de devolução de uma alocação anterior do mesmo ativo. */
  impactadaPorAtraso: boolean;
  criadoEmUtc: string;
  /** Data/hora em que o ativo foi efetivamente devolvido; `null` enquanto ainda está alocado. */
  devolvidoEmUtc: string | null;
}

/**
 * Resposta retornada quando uma solicitação de alocação de ativo foi desviada para o
 * fluxo de aprovação em vez de ser aplicada de imediato (análogo a
 * `EventoAprovacaoPendenteResponse` no domínio de eventos).
 */
export interface AlocacaoAtivoAprovacaoPendenteResponse {
  solicitacaoAprovacaoId: string;
  status: string;
  ativoId: string;
  eventoId: string;
  mensagem: string;
}

/** Resultado discriminado de uma tentativa de alocação: aplicada de imediato ou desviada para aprovação. */
export type AlocarAtivoResultado =
  | { kind: 'ALOCADO'; ativo: AtivoPatrimonial }
  | { kind: 'PENDENTE'; pendente: AlocacaoAtivoAprovacaoPendenteResponse };

/** Métricas agregadas de utilização do patrimônio, exibidas no painel de gestão de ativos. */
export interface MetricasOciosidade {
  totalAtivos: number;
  totalDisponiveis: number;
  totalAlocados: number;
  totalEmManutencao: number;
  totalBaixados: number;
  /** Percentual de ativos reserváveis atualmente ociosos (disponíveis e não alocados). */
  taxaOciosidade: number;
  distribuicaoPorCategoria: Record<string, number>;
}

