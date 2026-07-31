import apiClient from './client';
import {
  AtivoPatrimonial,
  AlocarAtivoPayload,
  CreateAtivoPayload,
  MetricasOciosidade,
  TransferirAtivoPayload,
} from './types';

export const getAtivosPorOrganizacao = async (organizacaoId: string): Promise<AtivoPatrimonial[]> => {
  const response = await apiClient.get<AtivoPatrimonial[]>(`/ativos/organizacao/${organizacaoId}`);
  return response.data;
};

export const createAtivo = async (payload: CreateAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.post<AtivoPatrimonial>('/ativos', payload);
  return response.data;
};

export const transferirAtivo = async (id: string, payload: TransferirAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}/transferir`, payload);
  return response.data;
};

export const alocarAtivo = async (id: string, payload: AlocarAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.post<AtivoPatrimonial>(`/ativos/${id}/alocar`, payload);
  return response.data;
};

export const getMetricasOciosidade = async (): Promise<MetricasOciosidade> => {
  const response = await apiClient.get<MetricasOciosidade>('/ativos/metricas/ociosidade');
  return response.data;
};
