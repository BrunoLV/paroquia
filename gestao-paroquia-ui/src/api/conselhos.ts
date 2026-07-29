import apiClient from './client';
import { CpcDecisao, StatusHomologacao } from './types';

export const getCpcDecisoes = async (params?: { comunidadeId?: string; status?: StatusHomologacao }): Promise<CpcDecisao[]> => {
  const response = await apiClient.get<CpcDecisao[]>('/conselhos/decisoes', { params });
  return response.data;
};

export const createCpcDecisao = async (data: {
  titulo: string;
  pautaDescricao: string;
  dataReuniao: string;
  comunidadeId: string;
}): Promise<CpcDecisao> => {
  const response = await apiClient.post<CpcDecisao>('/conselhos/decisoes', data);
  return response.data;
};

export const homologarCpcDecisao = async (
  id: string,
  data: { status: StatusHomologacao; motivoVeto?: string }
): Promise<CpcDecisao> => {
  const response = await apiClient.patch<CpcDecisao>(`/conselhos/decisoes/${id}/homologacao`, data);
  return response.data;
};
