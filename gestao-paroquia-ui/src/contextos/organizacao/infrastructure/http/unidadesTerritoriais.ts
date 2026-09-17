import apiClient from '@/shared/infrastructure/http/client';
import { Organizacao } from '@/shared/domain/types/types';

/**
 * Busca todas as unidades territoriais da paróquia (Clero, Matriz, Comunidades).
 */
export const getUnidadesTerritoriais = async (): Promise<Organizacao[]> => {
  const response = await apiClient.get<Organizacao[]>('/unidades-territoriais');
  return response.data;
};
