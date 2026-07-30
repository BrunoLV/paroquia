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

export const fetchPainelMetricasConsolidado = async (): Promise<MetricasEstrategicasPainelResponse> => {
  const response = await apiClient.get<MetricasEstrategicasPainelResponse>('/metricas/estrategicas/painel');
  return response.data;
};

export const fetchSazonalidadeLiturgica = async (): Promise<SazonalidadeLiturgicaResponse[]> => {
  const response = await apiClient.get<SazonalidadeLiturgicaResponse[]>('/metricas/estrategicas/sazonalidade-liturgica');
  return response.data;
};

export const fetchTaxaProjetos = async (): Promise<TaxaConclusaoProjetosResponse> => {
  const response = await apiClient.get<TaxaConclusaoProjetosResponse>('/metricas/estrategicas/taxa-projetos');
  return response.data;
};

export const fetchSlaDespacho = async (): Promise<SlaDespachoPastoralResponse> => {
  const response = await apiClient.get<SlaDespachoPastoralResponse>('/metricas/estrategicas/sla-despacho');
  return response.data;
};

export const fetchImpactoSocial = async (): Promise<CoeficienteImpactoSocialResponse> => {
  const response = await apiClient.get<CoeficienteImpactoSocialResponse>('/metricas/estrategicas/impacto-social');
  return response.data;
};

export const fetchConcorrenciaInterpastoral = async (): Promise<ConcorrenciaInterpastoralResponse> => {
  const response = await apiClient.get<ConcorrenciaInterpastoralResponse>('/metricas/estrategicas/concorrencia-interpastoral');
  return response.data;
};

export const fetchOciosidadePatrimonial = async (): Promise<OciosidadePatrimonialResponse> => {
  const response = await apiClient.get<OciosidadePatrimonialResponse>('/metricas/estrategicas/ociosidade-patrimonial');
  return response.data;
};
