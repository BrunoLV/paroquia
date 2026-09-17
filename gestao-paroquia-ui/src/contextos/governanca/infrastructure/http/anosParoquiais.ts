import apiClient from '@/shared/infrastructure/http/client';
import {AnoParoquial, AnoParoquialStatus} from '@/shared/domain/types/types';

/**
 * Busca todos os anos paroquiais e seu status no ciclo de vida
 * (EM_PLANEJAMENTO -> FECHADO -> CONCLUIDO). Usado para exibir o histórico de anos
 * e decidir quais permitem novos lançamentos (apenas EM_PLANEJAMENTO).
 */
export const getAnosParoquiais = async (): Promise<AnoParoquial[]> => {
  const response = await apiClient.get<AnoParoquial[]>('/anos-paroquiais');
  return response.data;
};

/**
 * Transiciona o status de um ano paroquial (ex.: fechar o planejamento ou concluir um
 * ano já fechado). A transição é uma ação administrativa irreversível na prática —
 * uma vez FECHADO/CONCLUIDO, o ano deixa de aceitar novos lançamentos.
 * @param ano Ano paroquial identificado pelo próprio número do ano.
 * @param status Novo status desejado no ciclo de vida.
 */
export const updateAnoParoquialStatus = async (
  ano: number,
  status: AnoParoquialStatus
): Promise<AnoParoquial> => {
  const response = await apiClient.patch<AnoParoquial>(`/anos-paroquiais/${ano}`, { status });
  return response.data;
};
