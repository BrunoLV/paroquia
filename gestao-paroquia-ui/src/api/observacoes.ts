import apiClient from './client';
import { Observacao } from './types';

export const getObservacoes = async (eventoId: string): Promise<Observacao[]> => {
  const response = await apiClient.get<Observacao[]>(`/eventos/${eventoId}/observacoes`);
  return response.data;
};

export const createObservacao = async (eventoId: string, conteudo: string): Promise<Observacao> => {
  const response = await apiClient.post<Observacao>(`/eventos/${eventoId}/observacoes`, {
    tipo: 'NOTA',
    conteudo,
  });
  return response.data;
};

export const updateObservacao = async (eventoId: string, observacaoId: string, conteudo: string): Promise<Observacao> => {
  const response = await apiClient.patch<Observacao>(`/eventos/${eventoId}/observacoes/${observacaoId}`, {
    conteudo,
  });
  return response.data;
};

export const deleteObservacao = async (eventoId: string, observacaoId: string): Promise<void> => {
  await apiClient.delete(`/eventos/${eventoId}/observacoes/${observacaoId}`);
};
