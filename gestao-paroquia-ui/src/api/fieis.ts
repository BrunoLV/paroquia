import apiClient from './client';
import { CreateFielPayload, Fiel, UpdateFielPayload } from './types';

/**
 * Busca a lista de Fiéis cadastrados na paróquia.
 */
export const getFieis = async (): Promise<Fiel[]> => {
  const response = await apiClient.get<Fiel[]>('/fieis');
  return response.data;
};

/**
 * Busca os detalhes de um Fiel específico pelo ID.
 */
export const getFiel = async (id: string): Promise<Fiel> => {
  const response = await apiClient.get<Fiel>(`/fieis/${id}`);
  return response.data;
};

/**
 * Cadastra um novo Fiel (pessoa física) no sistema.
 */
export const createFiel = async (payload: CreateFielPayload): Promise<Fiel> => {
  const response = await apiClient.post<Fiel>('/fieis', payload);
  return response.data;
};

/**
 * Atualiza os dados de um Fiel existente.
 */
export const updateFiel = async (id: string, payload: UpdateFielPayload): Promise<Fiel> => {
  const response = await apiClient.put<Fiel>(`/fieis/${id}`, payload);
  return response.data;
};
