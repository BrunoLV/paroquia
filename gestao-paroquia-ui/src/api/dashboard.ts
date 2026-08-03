import apiClient from './client';
import {DashboardMetrics} from './types';

/**
 * Busca o snapshot agregado exibido na tela inicial (Dashboard): resumo geral
 * da paróquia, distribuição de eventos por organização, detalhes dos projetos
 * ativos e a agenda de eventos da semana corrente. É uma visão consolidada e
 * somente leitura — não substitui as consultas detalhadas de cada contexto
 * (Calendário, Projetos), servindo apenas como painel de visão rápida.
 */
export const getDashboardMetrics = async (): Promise<DashboardMetrics> => {
  const response = await apiClient.get<DashboardMetrics>('/dashboard/metrics');
  return response.data;
};
