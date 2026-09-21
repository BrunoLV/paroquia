export type TipoOrganismoParoquial =
  | "MATRIZ"
  | "COMUNIDADE"
  | "CLERO"
  | "CONSELHO_PASTORAL"
  | "CONSELHO_ECONOMICO"
  | "PASTORAL"
  | "MOVIMENTO"
  | "OUTRO";

export type Vocacao = "LEIGO" | "SACERDOTE" | "DIACONO" | "RELIGIOSO";

export const VOCACAO_LABELS: Record<Vocacao, string> = {
  LEIGO: "Leigo(a)",
  SACERDOTE: "Sacerdote",
  DIACONO: "Diácono",
  RELIGIOSO: "Religioso(a)",
};

export type Sexo = "MASCULINO" | "FEMININO" | "NAO_INFORMADO";
export type EstadoCivil =
  | "SOLTEIRO"
  | "CASADO_IGREJA"
  | "CASADO_CIVIL"
  | "VIUVO"
  | "DIVORCIADO"
  | "UNIAO_ESTAVEL";
export type PreferenciaCanalComunicacao =
  "WHATSAPP" | "EMAIL" | "SMS" | "TELEFONE" | "NENHUM";
export type Parentesco = "FILHO" | "ENTEADO" | "TUTELADO" | "OUTRO";

export interface Endereco {
  cep?: string | null;
  logradouro?: string | null;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade?: string | null;
  uf?: string | null;
  estado?: string | null;
}

export interface Dependente {
  id?: string;
  nome: string;
  dataNascimento?: string | null;
  parentesco?: Parentesco;
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
}

export interface VidaSacramental {
  batizado?: boolean;
  dataBatismo?: string | null;
  paroquiaBatismo?: string | null;
  celebranteBatismo?: string | null;
  livroBatismo?: string | null;
  folhaBatismo?: string | null;
  termoBatismo?: string | null;
  padrinhosBatismo?: string | null;

  primeiraComunhao?: boolean;
  dataPrimeiraComunhao?: string | null;
  paroquiaPrimeiraComunhao?: string | null;
  turmaCatequese?: string | null;

  crismado?: boolean;
  dataCrisma?: string | null;
  paroquiaCrisma?: string | null;
  celebranteCrisma?: string | null;
  padrinhoCrisma?: string | null;

  casadoIgrejaCatolica?: boolean;
  dataMatrimonio?: string | null;
  paroquiaMatrimonio?: string | null;
  numeroProcessoMatrimonial?: string | null;
  certidaoMatrimonio?: string | null;
  nomePaiCertidao?: string | null;
  nomeMaeCertidao?: string | null;
  testemunhasMatrimonio?: string | null;
}

export interface ConsentimentoLgpd {
  termoConsentimentoAceito?: boolean;
  dataHoraConsentimentoUtc?: string | null;
  ipConsentimento?: string | null;
  versaoTermoConsentimento?: string | null;
  usoImagemAutorizado?: boolean;
  permitirDivulgacaoAniversario?: boolean;
}

export interface Fiel {
  fielId?: string;
  id: string;
  nome: string | null;
  sexo?: Sexo;
  cpf: string | null;
  rg?: string | null;
  dataNascimento: string | null;
  profissao?: string | null;
  email: string | null;
  telefone: string | null;
  telefoneFixo?: string | null;
  celularWhatsapp?: string | null;
  preferenciaCanalComunicacao?: PreferenciaCanalComunicacao;
  endereco?: Endereco | string | null;
  estadoCivil?: EstadoCivil;
  conjugeId?: string | null;
  vocacao: Vocacao;
  ativo: boolean;
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
  casadoIgrejaCatolica?: boolean;
  vidaSacramental?: VidaSacramental | null;
  dependentes?: Dependente[];
  habilidadesVoluntariado?: string | null;
  consentimentoLgpd?: ConsentimentoLgpd | null;
  dadosIdentificacao?: {
    cpfFormatado?: string | null;
    rg?: string | null;
    paisOrigem?: string | null;
    tipoDocumentoEstrangeiro?: TipoDocumentoEstrangeiro | null;
    documentoEstrangeiroFormatado?: string | null;
  };
  contato?: {
    celularFormatado?: string | null;
    isWhatsapp?: boolean;
    telefoneFixoFormatado?: string | null;
    email?: string | null;
  };
  igrejaFrequenta?: { id: string; nome: string; tipo: string } | null;
  isDizimista?: boolean;
  permitirDivulgacaoAniversario?: boolean;
  isAtivo?: boolean;
}

export interface CreateFielPayload {
  nome: string;
  sexo: Sexo;
  paisOrigem: string;
  cpf?: string;
  rg?: string;
  tipoDocumentoEstrangeiro?: TipoDocumentoEstrangeiro;
  numeroDocumentoEstrangeiro?: string;
  dataNascimento: string;
  profissao?: string;
  email: string;
  celular: string;
  isWhatsapp: boolean;
  unidadeTerritorialId: string;
  isDizimista: boolean;
  permitirDivulgacaoAniversario: boolean;
  telefone?: string;
  telefoneFixo?: string;
  celularWhatsapp?: string;
  preferenciaCanalComunicacao?: PreferenciaCanalComunicacao;
  endereco?: Endereco | string;
  estadoCivil?: EstadoCivil;
  conjugeId?: string;
  vocacao?: Vocacao;
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
  casadoIgrejaCatolica?: boolean;
  vidaSacramental?: VidaSacramental;
  dependentes?: Dependente[];
  habilidadesVoluntariado?: string;
  consentimentoLgpd?: ConsentimentoLgpd;
}

export interface UpdateFielPayload {
  nome?: string;
  cpf?: string;
  rg?: string;
  paisOrigem?: string;
  tipoDocumentoEstrangeiro?: TipoDocumentoEstrangeiro;
  numeroDocumentoEstrangeiro?: string;
  dataNascimento?: string;
  sexo?: Sexo;
  profissao?: string;
  email?: string;
  celular?: string;
  isWhatsapp?: boolean;
  unidadeTerritorialId?: string;
  isDizimista?: boolean;
  permitirDivulgacaoAniversario?: boolean;
  telefone?: string;
  telefoneFixo?: string;
  celularWhatsapp?: string;
  preferenciaCanalComunicacao?: PreferenciaCanalComunicacao;
  endereco?: Endereco | string;
  estadoCivil?: EstadoCivil;
  conjugeId?: string;
  vocacao?: Vocacao;
  ativo?: boolean;
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
  casadoIgrejaCatolica?: boolean;
  vidaSacramental?: VidaSacramental;
  dependentes?: Dependente[];
  habilidadesVoluntariado?: string;
  consentimentoLgpd?: ConsentimentoLgpd;
}

export type TipoDocumentoEstrangeiro = "RNE_RNM" | "PASSAPORTE";

export type SetorPastoral =
  "SANTIFICACAO_LITURGIA" | "FORMACAO_ENSINAR" | "SOCIAL_CARIDADE";

export type PapelEclesial =
  | "ADMIN"
  | "BISPO_DIOCESANO"
  | "PAROCO"
  | "VIGARIO"
  | "DIACONO"
  | "PADRE"
  | "MESA_CPP"
  | "MEMBRO_CPP"
  | "MEMBRO_CAE"
  | "PRESIDENTE_CPC"
  | "MEMBRO_CPC"
  | "COORD_PASTORAL_GLOBAL"
  | "COORD_PASTORAL_LOCAL"
  | "COORD_PASTORAL"
  | "COORD_MOVIMENTO"
  | "VOLUNTARIO_PASTORAL_LOCAL"
  | "MEMBRO_PASTORAL"
  | "MEMBRO_MOVIMENTO"
  | "SECRETARIO";

export const PAPEL_ECLESIAL_LABELS: Record<string, string> = {
  ADMIN: "Administrador do Sistema",
  BISPO_DIOCESANO: "Bispo Diocesano",
  PAROCO: "Pároco",
  VIGARIO: "Vigário Paroquial",
  DIACONO: "Diácono",
  PADRE: "Padre",
  MESA_CPP: "Mesa Diretora do CPP",
  MEMBRO_CPP: "Membro do CPP",
  MEMBRO_CAE: "Membro do CAE",
  PRESIDENTE_CPC: "Presidente do CPC",
  MEMBRO_CPC: "Membro do CPC",
  COORD_PASTORAL_GLOBAL: "Coordenador Pastoral Global",
  COORD_PASTORAL_LOCAL: "Coordenador Pastoral Local",
  COORD_PASTORAL: "Coordenador Pastoral",
  COORD_MOVIMENTO: "Coordenador de Movimento",
  VOLUNTARIO_PASTORAL_LOCAL: "Voluntário Pastoral Local",
  MEMBRO_PASTORAL: "Membro de Pastoral",
  MEMBRO_MOVIMENTO: "Membro de Movimento",
  SECRETARIO: "Secretário",

  // Aliases for stored values / legacy compatibility
  MESA_DIRETORA_CPP: "Mesa Diretora do CPP",
  COORDENADOR_PASTORAL_GLOBAL: "Coordenador Pastoral Global",
  COORDENADOR_PASTORAL_LOCAL: "Coordenador Pastoral Local",
  COORDENADOR_PASTORAL: "Coordenador Pastoral",
  COORDENADOR_MOVIMENTO: "Coordenador de Movimento",
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
  tipo: "PASTORAL" | "MOVIMENTO";
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
  capacidadeMaxima: number;
  descricao?: string | null;
  ativo: boolean;
  igrejaId?: string | null;
  igrejaNome?: string | null;
  cep?: string | null;
  logradouro?: string | null;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade?: string | null;
  uf?: string | null;
  enderecoFormatado?: string | null;
  deletedAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

/** Formulário de criação/edição de um {@link Local}. */
export interface LocalFormValues {
  nome: string;
  tipo: string;
  capacidadeMaxima: number;
  descricao?: string;
  ativo?: boolean;
  igrejaId?: string | null;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
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
  grupoParoquialResponsavelId: string;
  status: "ABERTO" | "EM_ANDAMENTO" | "CANCELADO" | "CONCLUIDO";
  /** Flag de UI: marca que o registro acabou de ser atualizado, para destacar/realçar na lista. */
  updated?: boolean;
}

export type CategoriaEvento =
  | "SOLENIDADE"
  | "PASTORAL"
  | "SOCIAL"
  | "LITURGICO"
  | "ADMINISTRATIVO"
  | "SACRAMENTAL"
  | "FORMATIVO"
  | "ASSISTENCIAL"
  | "MOVIMENTO";

export type TipoHorarioEvento =
  | 'PERSONALIZADO'
  | 'DIA_INTEIRO'
  | 'MADRUGADA'
  | 'MANHA'
  | 'TARDE'
  | 'NOITE';

export type FormatoEvento = 'PRESENCIAL' | 'ONLINE' | 'HIBRIDO';

export const FORMATO_EVENTO_LABELS: Record<FormatoEvento, string> = {
  PRESENCIAL: 'Presencial',
  ONLINE: 'Online / Remoto',
  HIBRIDO: 'Híbrido',
};

/**
 * Evento de calendário — a entidade mais conectada do domínio. Além dos dados básicos
 * de agendamento, carrega sinais opcionais de conflito de agenda (`estadoConflito`/
 * `motivoConflito`) e de aprovação pendente (`aprovacaoPendenteId` e campos relacionados),
 * populados pela UI (ver `src/api/eventos.ts` e `Calendario.tsx`) a partir de uma
 * `AprovacaoResponse` associada, para evitar uma segunda chamada à API.
 */
export interface Evento {
  id: string;
  titulo: string;
  descricao: string;
  categoria: CategoriaEvento | string;
  grupoParoquialResponsavelId: string;
  /** Projeto ao qual este evento pertence, ou `null` se for um evento avulso. */
  projetoId: string | null;
  nomeProjeto: string | null;
  localId: string | null;
  /** IDs de todos os espaços físicos vinculados ao evento (permite múltiplos locais). */
  localIds?: string[];
  inicio: string;
  fim: string;
  status: string;
  /** Formato de realização (Presencial, Online, Híbrido). */
  formato?: FormatoEvento;
  /** Formato de horário (Personalizado, Dia Inteiro, Madrugada, Manhã, Tarde, Noite). */
  tipoHorario?: TipoHorarioEvento;
  /** Turnos múltiplos selecionados para o evento (ex: Manhã e Noite). */
  periodos?: TipoHorarioEvento[];
  /** IDs dos membros/participantes convidados/vinculados ao evento. */
  participantes?: string[];
  /**
   * ID da série de recorrência à qual este evento pertence, ou `null`/ausente se não foi gerado
   * por uma regra de recorrência. A regra completa (frequência, dias, limite) não vem aqui —
   * busque separadamente via `getRecorrencia(eventoId)` (`GET /eventos/{id}/recorrencia`).
   */
  recorrenciaId?: string | null;
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
  /** IDs dos ativos patrimoniais alocados para o evento */
  ativoIds?: string[];
}

/**
 * Payload para criação de um {@link Evento}. Pode resultar tanto na criação direta do
 * evento quanto no roteamento para o fluxo de aprovação, dependendo da autoridade do ator.
 */
export interface CreateEventoPayload {
  titulo: string;
  descricao?: string;
  categoria: string;
  grupoParoquialResponsavelId: string;
  projetoId?: string | null;
  localId?: string | null;
  /** Lista de múltiplos locais/espaços físicos alocados simultaneamente */
  localIds?: string[];
  inicio: string;
  fim: string;
  formato?: FormatoEvento;
  tipoHorario?: TipoHorarioEvento;
  periodos?: TipoHorarioEvento[];
  status?: string;
  participantes?: string[];
  adicionadoExtraJustificativa?: string | null;
  igrejaVinculoId?: string | null;
  /** Permite ao ator liberar/forçar a criação apesar de um conflito de agenda detectado (restrito a papéis com essa autoridade). */
  liberarConflitoAgenda?: boolean;
  /** IDs dos ativos patrimoniais alocados para o evento */
  ativoIds?: string[];
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
  formato?: FormatoEvento;
  tipoHorario?: TipoHorarioEvento;
  periodos?: TipoHorarioEvento[];
  status?: string;
  adicionadoExtraJustificativa?: string | null;
  /** Motivo textual exigido ao transicionar `status` para cancelado. */
  canceladoMotivo?: string | null;
  grupoParoquialResponsavelId?: string;
  participantes?: string[];
  /** Escopo da edição quando o evento pertence a uma série recorrente: só esta ocorrência, esta e as futuras, ou toda a série. */
  escopoEdicao?: "SOMENTE_ESTE" | "ESTE_E_PROXIMOS" | "TODOS";
  projetoId?: string | null;
  localId?: string | null;
  /** Lista de múltiplos locais/espaços físicos alocados simultaneamente */
  localIds?: string[];
  igrejaVinculoId?: string | null;
  liberarConflitoAgenda?: boolean;
  /** IDs dos ativos patrimoniais alocados para o evento */
  ativoIds?: string[];
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
  tipoSolicitacao:
    | "CRIACAO_EVENTO"
    | "EDICAO_EVENTO"
    | "ALTERACAO_HORARIO"
    | "CANCELAMENTO"
    | "RECLASSIFICACAO"
    | "ALOCACAO_ATIVO_EMPRESTIMO"
    | "OUTRO"
    | "UNKNOWN_LEGACY";
  status: "PENDENTE" | "APROVADA" | "REPROVADA" | "FALHA_EXECUCAO";
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
  /** Identificador da aprovação mestre agregadora em caso de série recorrente desmembrada */
  aprovacaoPaiId?: string | null;
  /** Compilação analítica de conflitos de local, ativos e agenda eclesial em JSON */
  conflitosJson?: string | null;
  /** Data da ocorrência no caso de item granular de recorrência */
  dataOcorrenciaUtc?: string | null;
  /** Justificativa formal do solicitante (mandatória para Secretário e ano fechado) */
  justificativa?: string | null;
  /** Total de ocorrências filhas vinculadas à solicitação mestre */
  totalOcorrenciasFilhas?: number | null;
}

export interface ConflitoEventoItem {
  tipo: 'LOCAL' | 'ATIVO' | 'AGENDA_ECLESIAL' | string;
  itemId?: string | null;
  itemNome?: string | null;
  eventoConflitanteId?: string | null;
  eventoConflitanteTitulo?: string | null;
  inicioOcupadoUtc?: string | null;
  fimOcupadoUtc?: string | null;
  solicitanteNome?: string | null;
  prioridadeExistente?: number | null;
  prioridadeSolicitada?: number | null;
  detalhe?: string | null;
}

/** Requisição para decidir (aprovar/reprovar) uma solicitação de aprovação pendente. */
export interface AprovacaoDecisionRequest {
  status: "APROVADA" | "REPROVADA";
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

export type AnoParoquialStatus = "EM_PLANEJAMENTO" | "FECHADO" | "CONCLUIDO";

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
  tipoSolicitacao: "CRIACAO" | "CANCELAMENTO";
  status: "PENDENTE" | "APROVADA" | "REPROVADA";
  solicitanteId: string;
  criadoEmUtc: string;
  decididoEmUtc: string | null;
  actionPayloadJson: string | null;
  /** Quando `true`, o cancelamento do projeto arrasta consigo o cancelamento dos eventos vinculados a ele. */
  cancelarEventosVinculados: boolean;
}

/** Recorte temporal do filtro global do dashboard. */
export type PeriodoDashboard = "HOJE" | "SEMANA" | "MES";

/** Cartões-resumo do topo do dashboard. */
export interface GeneralSummary {
  /** Eventos com início dentro do recorte temporal selecionado. */
  totalEventosPeriodo: number;
  projetosAtivos: number;
  /** Total de solicitações de aprovação (eventos/projetos) ainda sem decisão. */
  solicitacoesPendentes: number;
  /** Fiéis com cadastro ativo. */
  totalFieisAtivos: number;
  /** Fiéis cadastrados desde o primeiro dia do mês corrente (sempre mensal). */
  novosFieisMes: number;
  /** Fiéis distintos com vínculo ativo a uma pastoral ou movimento. */
  fieisEngajadosPastoral: number;
  /** Fiéis ativos sem nenhum vínculo pastoral ativo. */
  fieisSemVinculoPastoral: number;
}

/** Ponto de dados para o gráfico de distribuição de eventos por organização responsável. */
export interface EventDistribution {
  grupoParoquialNome: string;
  quantidadeEventos: number;
}

/** Próximo evento crítico ainda por ocorrer de um projeto ativo. */
export interface ProximoMarco {
  titulo: string;
  inicio: string;
  /** 1 = prioridade máxima, 4 = mínima. */
  nivelPrioridade: number;
}

/** Resumo de acompanhamento de um projeto ativo, exibido na lista de projetos do dashboard. */
export interface ProjectDetail {
  nome: string;
  status: string;
  /** Percentual do período do projeto já decorrido (0-100). */
  percentualTempoDecorrido: number;
  totalEventos: number;
  eventosRealizados: number;
  /** Percentual (0-100) de eventos concluídos sobre os previstos. */
  percentualExecucao: number;
  /** Eventos do projeto ainda aguardando aprovação. */
  eventosPendentesAprovacao: number;
  /** `true` quando o prazo está apertado com pendências. */
  emRisco: boolean;
  /** Próximo marco crítico, ou `null`. */
  proximoMarco: ProximoMarco | null;
  /** Nomes das organizações/pessoas envolvidas no projeto. */
  envolvidos: string[];
}

/** Distribuição dos projetos pelas faixas de status exibidas no dashboard. */
export interface ProjetosPorStatus {
  planejamento: number;
  emExecucao: number;
  concluidos: number;
  /** Projetos ativos com prazo vencido ou classificados em risco (derivado). */
  atrasados: number;
}

/** Evento sem projeto vinculado, com eventual sugestão de vínculo. */
export interface EventoOrfao {
  eventoId: string;
  titulo: string;
  inicio: string;
  projetoSugeridoId: string | null;
  projetoSugeridoNome: string | null;
}

/** Evento resumido para a lista "eventos da semana" do dashboard. */
export interface WeeklyEvent {
  titulo: string;
  inicio: string;
  localNome: string;
  grupoParoquialNome: string;
}

/** Aniversariante do período (dados mínimos por LGPD: primeiro nome + dia/mês). */
export interface Aniversariante {
  primeiroNome: string;
  dia: number;
  mes: number;
}

/** Pastoral em situação de atenção quanto à retenção de membros. */
export interface RetencaoPastoral {
  grupoId: string;
  nome: string;
  totalVoluntarios: number;
  temLideranca: boolean;
  motivos: string[];
}

/** Seção de vida comunitária do Painel. */
export interface DashboardComunidade {
  retencaoPastorais: RetencaoPastoral[];
}

/** Par de eventos que disputam o mesmo local em horários sobrepostos. */
export interface ChoqueHorario {
  localNome: string;
  eventoATitulo: string;
  eventoBTitulo: string;
  inicio: string;
}

/** Ocupação estimada de um ambiente na semana. */
export interface OcupacaoLocal {
  localNome: string;
  taxaSemanaPercentual: number;
  horasOcupadasSemana: number;
}

/** Taxa de ocupação estimada dos ambientes (dia e semana). */
export interface OcupacaoAmbientes {
  taxaDiaPercentual: number;
  taxaSemanaPercentual: number;
  porLocal: OcupacaoLocal[];
}

/** Contagem de eventos por organização responsável no período. */
export interface EventosPorResponsavel {
  grupoParoquialNome: string;
  quantidade: number;
}

/** Pendências de homologação agrupadas por tipo de solicitação. */
export interface PendenciaHomologacao {
  tipo: string;
  quantidade: number;
}

/** Seção de logística/espaços/agenda do Painel. */
export interface DashboardLogistica {
  choquesHorario: ChoqueHorario[];
  ocupacaoAmbientes: OcupacaoAmbientes;
  eventosPorResponsavel: EventosPorResponsavel[];
  pendenciasHomologacao: PendenciaHomologacao[];
}

/** Agregado de métricas exibidas na tela inicial (Painel). */
export interface DashboardMetrics {
  resumoGeral: GeneralSummary;
  eventosPorGrupoParoquial: EventDistribution[];
  projetosPorStatus: ProjetosPorStatus;
  projetosAtivosDetalhes: ProjectDetail[];
  eventosOrfaos: EventoOrfao[];
  eventosSemana: WeeklyEvent[];
}

export type TipoObservacao =
  | "NOTA"
  | "JUSTIFICATIVA"
  | "APROVACAO"
  | "REPROVACAO"
  | "CANCELAMENTO"
  | "AJUSTE_HORARIO";

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
  type:
    | "PAROQUIA"
    | "ORGANIZACAO"
    | "UNIDADE_TERRITORIAL"
    | "GRUPO_PAROQUIAL"
    | "MEMBRO"
    | "GRUPO"
    | string;
  subtype: string;
  responsavelNome: string | null;
  responsavelPapel: string | null;
  viceResponsavelNome: string | null;
  children: OrganogramaNode[];
  /** Sub-organizações filhas (quando o nó é uma organização), mantidas à parte de `children` para permitir renderização/expansão independente do restante da árvore. */
  orgChildren?: OrganogramaNode[];
}

export type StatusHomologacao =
  "PENDENTE_HOMOLOGACAO" | "HOMOLOGADO" | "VETADO";

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

export type AtivoCategoria =
  "ELETRONICO" | "MOBILIARIO" | "PARAMENTO" | "INFRAESTRUTURA";

export type AtivoStatus = "DISPONIVEL" | "RESERVADO" | "EM_MANUTENCAO";

/**
 * Bem patrimonial móvel da paróquia, pertencente de forma fixa a uma unidade territorial.
 * Após o cadastro, apenas `descricao` é editável (imutabilidade estrutural). O uso temporário
 * do bem é feito pelo módulo de Reservas; o descarte é exclusão lógica (`deletedAt`).
 */
export interface AtivoPatrimonial {
  id: string;
  nome: string;
  /** Código de tombamento patrimonial gerado pelo sistema (padrão PAT-AAAA-NNNN); imutável. */
  codigoTombamento: string;
  descricao?: string | null;
  /** Tipo do bem: eletrônico, mobiliário, paramento litúrgico ou infraestrutura. */
  categoria: AtivoCategoria;
  status: AtivoStatus;
  /** Elegível para reserva no módulo de Reservas; imutável após o cadastro. */
  isReservavel: boolean;
  grupoParoquialId: string;
  dataAquisicao: string;
  valorAquisicao: number;
  cadastradoPorId?: string | null;
  criadoEmUtc: string;
  atualizadoEmUtc: string;
}

/** Payload de cadastro de um novo {@link AtivoPatrimonial}. */
export interface CadastrarAtivoPayload {
  nome: string;
  descricao?: string | null;
  categoria: AtivoCategoria;
  grupoParoquialId: string;
  isReservavel: boolean;
  dataAquisicao: string;
  valorAquisicao: number;
}

/** Edição do ativo — apenas a descrição é mutável. */
export interface EditarDescricaoAtivoPayload {
  descricao?: string | null;
}

/** Envio de um ativo para manutenção por um período determinado. */
export interface EnviarManutencaoPayload {
  descricaoReparo: string;
  /** ISO-8601 (datetime). */
  dataInicio: string;
  /** ISO-8601 (datetime). */
  dataFimPrevista: string;
}

/** Registro de manutenção de um ativo. `dataFimReal` nula = manutenção em aberto. */
export interface AtivoManutencao {
  id: string;
  ativoId: string;
  descricaoReparo: string;
  dataInicio: string;
  dataFimPrevista: string;
  dataFimReal?: string | null;
  registradoPorId: string;
  criadoEmUtc: string;
}

/** Reserva que impede o envio de um ativo para manutenção no período consultado. */
export interface ReservaImpeditiva {
  reservaId: string;
  descricao: string;
  inicio: string;
  fim: string;
  status: string;
  solicitanteNome: string;
}

/** Métricas agregadas de ociosidade do acervo patrimonial. */
export interface MetricasOciosidade {
  totalAtivos: number;
  totalDisponiveis: number;
  totalReservados: number;
  totalEmManutencao: number;
  /** Proporção de bens DISPONIVEL (sem uso) sobre o total incorporado. */
  taxaOciosidade: number;
}

export type GrauOrdem = 'DIACONO_TRANSITORIO' | 'DIACONO_PERMANENTE' | 'PADRE_DIOCESANO' | 'PADRE_RELIGIOSO' | 'BISPO';
export type FuncaoEclesiastica = 'BISPO_DIOCESANO' | 'PAROCO' | 'ADMINISTRADOR_PAROQUIAL' | 'VIGARIO_PAROQUIAL' | 'DIACONO_AUXILIAR' | 'EMERITO';
export interface ClerigoInstitucional { clerigoId: string; nomeEclesiastico: string; grauOrdem: GrauOrdem; funcaoAtual: FuncaoEclesiastica | null; paroquia: string; }
export interface Clerigo extends ClerigoInstitucional { nomeCivil: string; dataNascimento: string; paisOrigem: string; cpfFormatado: string | null; rg: string | null; documentoEstrangeiro: string | null; email: string; celularFormatado: string; isWhatsapp: boolean; telefoneFixoFormatado: string | null; endereco: Endereco | string | null; dioceseIncardinacao: string; dataOrdenacaoDiaconal: string | null; dataOrdenacaoPresbiteral: string | null; dataOrdenacaoEpiscopal: string | null; isAtivo: boolean; }
export type ClerigoPayload = Omit<Clerigo, 'clerigoId' | 'cpfFormatado' | 'celularFormatado' | 'telefoneFixoFormatado' | 'paroquia' | 'isAtivo' | 'endereco'> & { cpf?: string; celular: string; telefoneFixo?: string; endereco?: Endereco | null; };

export type TipoItemReserva = 'LOCAL' | 'ATIVO';

export type StatusReserva =
  | 'PENDENTE'
  | 'CONFIRMADA'
  | 'RETIRADO'
  | 'FINALIZADA'
  | 'CANCELADA'
  | 'EXPIRADA';

export interface ReservaDetalhe {
  id: string;
  tipoItem: TipoItemReserva;
  itemId: string;
  itemNome: string;
  dataHoraInicio: string;
  dataHoraFim: string;
  bufferMinutos: number;
  fimComBuffer: string;
  descricao: string;
  solicitanteId: string;
  solicitanteNome: string;
  pastoralMovimentoId: string;
  pastoralMovimentoNome: string;
  eventoId?: string | null;
  eventoTitulo?: string | null;
  grupoRecorrenciaId?: string | null;
  status: StatusReserva;
  aprovadorId?: string | null;
  aprovadorNome?: string | null;
  aprovadoEm?: string | null;
  motivoCancelamento?: string | null;
  canceladoPorId?: string | null;
  canceladoPorNome?: string | null;
  checkAvarias?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CriarReservaPayload {
  tipoItem: TipoItemReserva;
  itemId: string;
  dataHoraInicio: string;
  dataHoraFim: string;
  bufferMinutos?: number;
  descricao: string;
  pastoralMovimentoId: string;
  eventoId?: string | null;
}

export interface CriarReservaRecorrentePayload {
  tipoItem: TipoItemReserva;
  itemId: string;
  dataHoraInicio: string;
  dataHoraFim: string;
  bufferMinutos?: number;
  descricao: string;
  pastoralMovimentoId: string;
  eventoId?: string | null;
  tipoRecorrencia: 'SEMANAL' | 'QUINZENAL' | 'MENSAL';
  quantidadeOcorrencias: number;
}

export interface CancelarReservaPayload {
  motivo: string;
}

export interface DevolverAtivoPayload {
  checkAvarias: string;
}

export interface ReservaConflitoDetalhe {
  itemId?: string;
  itemNome?: string;
  tipoItem?: TipoItemReserva | string;
  dataHoraInicio?: string;
  fimComBuffer?: string;
  responsavelId?: string;
  responsavelNome?: string;
  motivo?: string;
  // Propriedades adicionais retornadas pelo backend
  tipoConflito?: string;
  recursoId?: string;
  recursoNome?: string;
  eventoConcorrenteId?: string;
  eventoConcorrenteTitulo?: string;
  inicioUtc?: string;
  fimUtc?: string;
  solicitanteOuResponsavel?: string;
  prioridadeConcorrente?: number;
  prioridadeSolicitada?: number;
  detalhe?: string;
  inicioOcupado?: string;
  fimOcupadoComBuffer?: string;
  solicitanteId?: string;
  solicitanteNome?: string;
  descricaoReserva?: string;
}

export interface ReservaMetricas {
  totalHoje: number;
  totalPendentes: number;
  totalEmUso: number;
  totalConflitosPrevenidos: number;
}

export interface DisponibilidadeSlot {
  inicio: string;
  fim: string;
  disponivel: boolean;
  reservaId?: string | null;
  descricao?: string | null;
}

