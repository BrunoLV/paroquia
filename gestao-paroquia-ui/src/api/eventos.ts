import apiClient from './client';
import { Evento } from './types';

export interface ListEventosParams {
  dataInicio: string;
  dataFim: string;
  organizacaoIds?: string[];
}

export const listEventos = async (params: ListEventosParams): Promise<Evento[]> => {
  const response = await apiClient.get<any>('/eventos', {
    params: { ...params, size: 200 },
    paramsSerializer: { indexes: null },
  });
  if (response.data && Array.isArray(response.data.content)) {
    return response.data.content;
  }
  if (Array.isArray(response.data)) {
    return response.data;
  }
  return [];
};
