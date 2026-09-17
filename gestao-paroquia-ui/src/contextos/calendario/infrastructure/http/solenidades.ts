import apiClient from '@/shared/infrastructure/http/client';
import { ApiSolenidadeDTO } from '@/shared/utils/liturgicalCalendar';

/**
 * Busca apenas as solenidades litúrgicas atualmente ativas, usadas para plotar o
 * calendário litúrgico vigente sobre a agenda de eventos.
 */
export async function fetchSolenidadesLiturgicasAtivas(): Promise<ApiSolenidadeDTO[]> {
  const response = await apiClient.get<ApiSolenidadeDTO[]>('/solenidades-liturgicas/ativas');
  return response.data;
}

/**
 * Busca todas as solenidades litúrgicas cadastradas, incluindo inativas — usado nas
 * telas administrativas de manutenção do calendário litúrgico.
 */
export async function fetchAllSolenidadesLiturgicas(): Promise<ApiSolenidadeDTO[]> {
  const response = await apiClient.get<ApiSolenidadeDTO[]>('/solenidades-liturgicas');
  return response.data;
}
