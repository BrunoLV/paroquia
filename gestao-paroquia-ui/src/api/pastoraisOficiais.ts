import apiClient from './client';
import {PastoralMovimentoOficial} from './types';

/**
 * Busca a lista oficial de pastorais e movimentos reconhecidos pela paróquia
 * (`tipo` distingue PASTORAL de MOVIMENTO), usada como referência ao vincular
 * organizações a um tipo oficial.
 */
export const getPastoraisOficiais = async (): Promise<PastoralMovimentoOficial[]> => {
  const response = await apiClient.get<PastoralMovimentoOficial[]>('/pastorais-oficiais');
  return response.data;
};

/**
 * Cadastra uma nova pastoral ou movimento oficial de referência.
 * @param pastoral Dados da pastoral/movimento, sem o `id` (gerado pelo backend).
 */
export const createPastoralOficial = async (
  pastoral: Omit<PastoralMovimentoOficial, 'id'>
): Promise<PastoralMovimentoOficial> => {
  const response = await apiClient.post<PastoralMovimentoOficial>('/pastorais-oficiais', pastoral);
  return response.data;
};

/**
 * Atualiza os dados de uma pastoral ou movimento oficial existente.
 * @param id ID da pastoral/movimento a atualizar.
 * @param pastoral Dados atualizados.
 */
export const updatePastoralOficial = async (
  id: string,
  pastoral: Omit<PastoralMovimentoOficial, 'id'>
): Promise<PastoralMovimentoOficial> => {
  const response = await apiClient.put<PastoralMovimentoOficial>(`/pastorais-oficiais/${id}`, pastoral);
  return response.data;
};

/**
 * Remove uma pastoral ou movimento oficial do cadastro de referência.
 * @param id ID da pastoral/movimento a remover.
 */
export const deletePastoralOficial = async (id: string): Promise<void> => {
  await apiClient.delete(`/pastorais-oficiais/${id}`);
};
