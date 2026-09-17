import apiClient from '@/shared/infrastructure/http/client';
import {Local} from '@/shared/domain/types/types';

/**
 * Busca a lista de locais físicos cadastrados (salões, igrejas, espaços), usados para
 * alocação de eventos e verificação de conflito de agenda por local.
 */
export const getLocais = async (): Promise<Local[]> => {
  const response = await apiClient.get<Local[]>('/locais');
  return response.data;
};

/**
 * Cadastra um novo local físico de referência.
 * @param local Dados do local, sem o `id` (gerado pelo backend).
 */
export const createLocal = async (local: Omit<Local, 'id'>): Promise<Local> => {
  const response = await apiClient.post<Local>('/locais', local);
  return response.data;
};

/**
 * Atualiza os dados cadastrais de um local existente.
 * @param id ID do local a atualizar.
 * @param local Dados atualizados do local.
 */
export const updateLocal = async (
  id: string,
  local: Omit<Local, 'id'> & { id?: string }
): Promise<Local> => {
  const response = await apiClient.put<Local>(`/locais/${id}`, local);
  return response.data;
};

/**
 * Remove um local do cadastro de referência.
 * @param id ID do local a remover.
 */
export const deleteLocal = async (id: string): Promise<void> => {
  await apiClient.delete(`/locais/${id}`);
};
