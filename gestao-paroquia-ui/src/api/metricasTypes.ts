export type TipoCicloLiturgico = 'ADVENTO' | 'NATAL' | 'QUARESMA' | 'PASCOA' | 'FESTA_PADROEIRA' | 'TEMPO_COMUM';

export interface SazonalidadeLiturgicaResponse {
  cicloNome: string;
  tipoCiclo: TipoCicloLiturgico;
  fatorDemanda: number;
  quantidadeEventosHistoricos: number;
  quantidadeEventosPrevistos: number;
  indiceSazonalidade: number;
  nivelDemanda: 'BAIXA' | 'NORMAL' | 'ALTA' | 'PICO_OPERACIONAL';
}

export interface TaxaConclusaoProjetosResponse {
  totalProjetos: number;
  projetosConcluidos: number;
  projetosEmAndamento: number;
  projetosCancelados: number;
  taxaConclusaoPercentual: number;
  taxaAbandonoPercentual: number;
  avaliacaoExecutiva: 'SAUDAVEL' | 'ALERTA' | 'CRITICO';
}

export interface SlaDespachoPastoralResponse {
  totalSolicitacoes: number;
  solicitacoesAprovadas: number;
  solicitacoesRejeitadas: number;
  tempoMedioAprovacaoHoras: number;
  slaMetaHoras: number;
  percentualDentroSla: number;
  statusSla: 'EXCELENTE' | 'DENTRO_DO_ESPERADO' | 'GARGALO_BUROCRATICO';
}

export interface CoeficienteImpactoSocialResponse {
  totalEventos: number;
  eventosSociaisCaridade: number;
  reunioesInternas: number;
  coeficienteImpactoSocialPercentual: number;
  diagnosticoPastoral: 'MISSIONARIA_EM_SAIDA' | 'EQUILIBRADA' | 'EXCESSIVAMENTE_BUROCRATICA';
}

export interface ConflitoItemDto {
  eventoIdA: string;
  tituloEventoA: string;
  pastoralA: string;
  eventoIdB: string;
  tituloEventoB: string;
  pastoralB: string;
  dataEvento: string;
  publicoAlvoCompartilhado: string;
  mensagemAlerta: string;
}

export interface ConcorrenciaInterpastoralResponse {
  totalConflitosDetectados: number;
  conflitos: ConflitoItemDto[];
  nivelRiscoConcorrencia: 'BAIXO' | 'MEDIO' | 'ALTO';
}

export interface OciosidadePatrimonialResponse {
  totalLocais: number;
  totalHorasUteisSemana: number;
  horasOcupadasSemana: number;
  horasOciosasSemana: number;
  indiceOciosidadePercentual: number;
  recomendacaoCae: 'EXCELENTE_APROVEITAMENTO' | 'OPORTUNIDADE_DE_NOVAS_FRENTES' | 'SUBAPROVEITAMENTO_CRITICO';
}

export interface MetricasEstrategicasPainelResponse {
  sazonalidadeLiturgica: SazonalidadeLiturgicaResponse[];
  taxaProjetos: TaxaConclusaoProjetosResponse;
  slaDespacho: SlaDespachoPastoralResponse;
  impactoSocial: CoeficienteImpactoSocialResponse;
  concorrenciaInterpastoral: ConcorrenciaInterpastoralResponse;
  ociosidadePatrimonial: OciosidadePatrimonialResponse;
}
