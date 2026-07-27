import apiClient from './client';
import {PastoralMovimentoOficial} from './types';

export const getPastoraisOficiais = async (): Promise<PastoralMovimentoOficial[]> => {
  const response = await apiClient.get<PastoralMovimentoOficial[]>('/pastorais-oficiais');
  return response.data;
};

export const createPastoralOficial = async (
  pastoral: Omit<PastoralMovimentoOficial, 'id'>
): Promise<PastoralMovimentoOficial> => {
  const response = await apiClient.post<PastoralMovimentoOficial>('/pastorais-oficiais', pastoral);
  return response.data;
};

export const updatePastoralOficial = async (
  id: string,
  pastoral: Omit<PastoralMovimentoOficial, 'id'>
): Promise<PastoralMovimentoOficial> => {
  const response = await apiClient.put<PastoralMovimentoOficial>(`/pastorais-oficiais/${id}`, pastoral);
  return response.data;
};

export const deletePastoralOficial = async (id: string): Promise<void> => {
  await apiClient.delete(`/pastorais-oficiais/${id}`);
};
