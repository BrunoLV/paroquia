import apiClient from '@/shared/infrastructure/http/client';
import {
  Aniversariante,
  DashboardComunidade,
  DashboardLogistica,
  DashboardMetrics,
  PeriodoDashboard,
} from '@/shared/domain/types/types';

/** Filtro global aplicado às métricas do dashboard. */
export interface DashboardMetricsParams {
  periodo: PeriodoDashboard;
  /** Comunidade/pastoral a filtrar; ausente usa o escopo do próprio ator. */
  organizacaoId?: string;
}

/**
 * Busca o snapshot agregado das seções crítica e de projetos do Painel: resumo geral,
 * distribuição de eventos por organização, distribuição de projetos por status, detalhes dos
 * projetos ativos, eventos órfãos e a agenda da semana corrente. As métricas de volume respeitam
 * o recorte temporal `periodo`; a organização é validada no backend contra o que o ator pode ver
 * (um `organizacaoId` sem acesso resulta em 403).
 */
export const getDashboardMetrics = async (
  params: DashboardMetricsParams,
): Promise<DashboardMetrics> => {
  const response = await apiClient.get<DashboardMetrics>('/painel/metricas', {
    params: {periodo: params.periodo, organizacaoId: params.organizacaoId},
  });
  return response.data;
};

/**
 * Busca a seção de logística do Painel (`GET /dashboard/logistica`): choque de horário por
 * local, taxa de ocupação estimada dos ambientes, eventos por responsável e pendências de
 * homologação por tipo. Mesmo filtro (`periodo`, `organizacaoId`) e mesma validação de escopo
 * do endpoint de métricas.
 */
export const getDashboardLogistica = async (
  params: DashboardMetricsParams,
): Promise<DashboardLogistica> => {
  const response = await apiClient.get<DashboardLogistica>('/painel/logistica', {
    params: {periodo: params.periodo, organizacaoId: params.organizacaoId},
  });
  return response.data;
};

/**
 * Busca os aniversariantes do Painel (`GET /dashboard/aniversariantes`). Retorna apenas
 * primeiro nome + dia/mês, de fiéis ativos que autorizaram a divulgação (LGPD).
 */
export const getAniversariantes = async (
  janela: PeriodoDashboard,
): Promise<Aniversariante[]> => {
  const response = await apiClient.get<Aniversariante[]>('/painel/aniversariantes', {
    params: {janela},
  });
  return response.data;
};

/**
 * Busca a seção de vida comunitária do Painel (`GET /dashboard/comunidade`): pastorais com
 * número crítico de voluntários ou sem liderança. Escopo validado no backend.
 */
export const getDashboardComunidade = async (
  organizacaoId?: string,
): Promise<DashboardComunidade> => {
  const response = await apiClient.get<DashboardComunidade>('/painel/comunidade', {
    params: {organizacaoId},
  });
  return response.data;
};
