import apiClient from './client';
import {
  AtivoPatrimonial,
  AlocarAtivoPayload,
  AlocarAtivoResultado,
  AtivoAlocacaoResponse,
  CancelarAlocacaoPayload,
  CreateAtivoPayload,
  DesalocarAtivoPayload,
  IniciarManutencaoPayload,
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

export const alocarAtivo = async (id: string, payload: AlocarAtivoPayload): Promise<AlocarAtivoResultado> => {
  const response = await apiClient.post(`/ativos/${id}/alocar`, payload);
  if (response.status === 202) {
    return { kind: 'PENDENTE', pendente: response.data };
  }
  return { kind: 'ALOCADO', ativo: response.data };
};

export const desalocarAtivo = async (id: string, payload?: DesalocarAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}/desalocar`, payload || {});
  return response.data;
};

export const iniciarManutencao = async (id: string, payload: IniciarManutencaoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.post<AtivoPatrimonial>(`/ativos/${id}/manutencao`, payload);
  return response.data;
};

export const finalizarManutencao = async (id: string): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}/manutencao/finalizar`);
  return response.data;
};

export const cancelarAlocacao = async (alocacaoId: string, payload: CancelarAlocacaoPayload): Promise<void> => {
  await apiClient.patch(`/ativos/alocacoes/${alocacaoId}/cancelar`, payload);
};

export const getAlocacoesPorAtivo = async (ativoId: string): Promise<AtivoAlocacaoResponse[]> => {
  const response = await apiClient.get<AtivoAlocacaoResponse[]>(`/ativos/${ativoId}/alocacoes`);
  return response.data;
};

export const getAlocacoesPorEvento = async (eventoId: string): Promise<AtivoAlocacaoResponse[]> => {
  const response = await apiClient.get<AtivoAlocacaoResponse[]>(`/ativos/evento/${eventoId}/alocacoes`);
  return response.data;
};

export const getMetricasOciosidade = async (): Promise<MetricasOciosidade> => {
  const response = await apiClient.get<MetricasOciosidade>('/ativos/metricas/ociosidade');
  return response.data;
};
