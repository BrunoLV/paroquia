import apiClient from './client';
import { Organizacao } from './types';

/**
 * Busca todas as unidades territoriais da paróquia (Clero, Matriz, Comunidades).
 */
export const getUnidadesTerritoriais = async (): Promise<Organizacao[]> => {
  const response = await apiClient.get<Organizacao[]>('/unidades-territoriais');
  return response.data;
};
