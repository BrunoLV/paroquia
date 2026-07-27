import apiClient from './client';
import {Local} from './types';

export const getLocais = async (): Promise<Local[]> => {
  const response = await apiClient.get<Local[]>('/locais');
  return response.data;
};

export const createLocal = async (local: Omit<Local, 'id'>): Promise<Local> => {
  const response = await apiClient.post<Local>('/locais', local);
  return response.data;
};

export const updateLocal = async (
  id: string,
  local: Omit<Local, 'id'> & { id?: string }
): Promise<Local> => {
  const response = await apiClient.put<Local>(`/locais/${id}`, local);
  return response.data;
};

export const deleteLocal = async (id: string): Promise<void> => {
  await apiClient.delete(`/locais/${id}`);
};
