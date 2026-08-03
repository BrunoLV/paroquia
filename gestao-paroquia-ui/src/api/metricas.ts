import { apiClient } from './client';
import {
  MetricasEstrategicasPainelResponse,
  ConcorrenciaInterpastoralResponse,
  SazonalidadeLiturgicaResponse,
  TaxaConclusaoProjetosResponse,
  SlaDespachoPastoralResponse,
  CoeficienteImpactoSocialResponse,
  OciosidadePatrimonialResponse,
} from './metricasTypes';

/**
 * Busca o painel consolidado de métricas estratégicas de gestão pastoral em
 * uma única chamada — reúne sazonalidade litúrgica, taxa de conclusão de
 * projetos, SLA de despacho pastoral, impacto social, concorrência
 * interpastoral e ociosidade patrimonial. Preferir esta função a disparar
 * as seis chamadas individuais abaixo quando todas as métricas forem
 * necessárias ao mesmo tempo (ex.: tela de relatórios executivos).
 */
export const fetchPainelMetricasConsolidado = async (): Promise<MetricasEstrategicasPainelResponse> => {
  const response = await apiClient.get<MetricasEstrategicasPainelResponse>('/metricas/estrategicas/painel');
  return response.data;
};

/**
 * Busca o índice de sazonalidade litúrgica: compara o volume de eventos
 * histórico e previsto em cada período do calendário litúrgico (Advento,
 * Quaresma, Páscoa, festa da padroeira etc.) para apontar o nível de demanda
 * esperado (`BAIXA` a `PICO_OPERACIONAL`) — usado para dimensionar
 * antecipadamente a capacidade operacional da paróquia em períodos de pico.
 */
export const fetchSazonalidadeLiturgica = async (): Promise<SazonalidadeLiturgicaResponse[]> => {
  const response = await apiClient.get<SazonalidadeLiturgicaResponse[]>('/metricas/estrategicas/sazonalidade-liturgica');
  return response.data;
};

/**
 * Busca a taxa de conclusão de projetos pastorais: quantos projetos foram
 * concluídos, quantos estão em andamento e quantos foram cancelados/
 * abandonados, resultando em uma avaliação executiva (`SAUDAVEL` a
 * `CRITICO`) da saúde geral da carteira de projetos da paróquia.
 */
export const fetchTaxaProjetos = async (): Promise<TaxaConclusaoProjetosResponse> => {
  const response = await apiClient.get<TaxaConclusaoProjetosResponse>('/metricas/estrategicas/taxa-projetos');
  return response.data;
};

/**
 * Busca o indicador de SLA de despacho pastoral: tempo médio de aprovação
 * das solicitações submetidas ao fluxo de aprovação, comparado à meta
 * (`slaMetaHoras`), resultando em um diagnóstico de agilidade administrativa
 * (`EXCELENTE` a `GARGALO_BUROCRATICO`) — sinaliza gargalos no despacho de
 * eventos/projetos pendentes de decisão.
 */
export const fetchSlaDespacho = async (): Promise<SlaDespachoPastoralResponse> => {
  const response = await apiClient.get<SlaDespachoPastoralResponse>('/metricas/estrategicas/sla-despacho');
  return response.data;
};

/**
 * Busca o coeficiente de impacto social: proporção entre eventos de
 * caráter social/caridade e reuniões puramente internas/administrativas,
 * resultando em um diagnóstico pastoral (`MISSIONARIA_EM_SAIDA` a
 * `EXCESSIVAMENTE_BUROCRATICA`) sobre o quanto a atividade da paróquia está
 * voltada à comunidade externa versus à própria burocracia interna.
 */
export const fetchImpactoSocial = async (): Promise<CoeficienteImpactoSocialResponse> => {
  const response = await apiClient.get<CoeficienteImpactoSocialResponse>('/metricas/estrategicas/impacto-social');
  return response.data;
};

/**
 * Busca a análise de concorrência interpastoral: detecta pares de eventos de
 * pastorais diferentes que competem pelo mesmo público-alvo em datas
 * próximas/coincidentes, listando cada conflito (`conflitos`) e um nível de
 * risco agregado (`nivelRiscoConcorrencia`) — ajuda a evitar sobreposição de
 * público entre atividades de pastorais distintas.
 */
export const fetchConcorrenciaInterpastoral = async (): Promise<ConcorrenciaInterpastoralResponse> => {
  const response = await apiClient.get<ConcorrenciaInterpastoralResponse>('/metricas/estrategicas/concorrencia-interpastoral');
  return response.data;
};

/**
 * Busca o índice de ociosidade patrimonial: compara as horas úteis semanais
 * disponíveis nos locais/espaços da paróquia com as horas efetivamente
 * ocupadas por eventos, resultando em uma recomendação para o Conselho de
 * Assuntos Econômicos (CAE) (`EXCELENTE_APROVEITAMENTO` a
 * `SUBAPROVEITAMENTO_CRITICO`) sobre o aproveitamento do patrimônio físico.
 */
export const fetchOciosidadePatrimonial = async (): Promise<OciosidadePatrimonialResponse> => {
  const response = await apiClient.get<OciosidadePatrimonialResponse>('/metricas/estrategicas/ociosidade-patrimonial');
  return response.data;
};
