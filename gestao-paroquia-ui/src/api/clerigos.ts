import apiClient from '@/api/client';
import type { Clerigo, ClerigoInstitucional, ClerigoPayload } from '@/api/types';

/** Integração do Clero; as chaves React Query são mantidas no hook da feature. */
export const listarClerigos = async (): Promise<Clerigo[]> => (await apiClient.get<Clerigo[]>('/clerigos')).data;
export const listarDiretorioClero = async (): Promise<ClerigoInstitucional[]> => (await apiClient.get<ClerigoInstitucional[]>('/clerigos/institucional')).data;
export const obterClerigo = async (id: string): Promise<Clerigo> => (await apiClient.get<Clerigo>(`/clerigos/${id}`)).data;
export const criarClerigo = async (payload: ClerigoPayload): Promise<Clerigo> => (await apiClient.post<Clerigo>('/clerigos', payload)).data;
export const atualizarClerigo = async (id: string, payload: ClerigoPayload): Promise<Clerigo> => (await apiClient.put<Clerigo>(`/clerigos/${id}`, payload)).data;
export const inativarClerigo = async (id: string): Promise<void> => { await apiClient.delete(`/clerigos/${id}`); };
