import apiClient from './client';
import { Projeto, ProjetoAprovacaoResponse } from './types';

export const getProjectApprovals = async (status?: string): Promise<ProjetoAprovacaoResponse[]> => {
  const response = await apiClient.get<ProjetoAprovacaoResponse[]>('/projetos/solicitacoes', {
    params: status ? { status } : {},
  });
  return response.data;
};

export const decideProjectApproval = async (
  id: string,
  aprovado: boolean,
  observacao: string
): Promise<any> => {
  const response = await apiClient.post(`/projetos/solicitacoes/${id}/decisao`, {
    aprovado,
    observacao,
  });
  return response.data;
};

export const iniciarProjeto = async (id: string): Promise<Projeto> => {
  const response = await apiClient.post<Projeto>(`/projetos/${id}/iniciar`);
  return response.data;
};

export const concluirProjeto = async (id: string): Promise<Projeto> => {
  const response = await apiClient.post<Projeto>(`/projetos/${id}/concluir`);
  return response.data;
};

export const cancelarProjeto = async (
  id: string,
  cancelarEventosVinculados: boolean
): Promise<any> => {
  const response = await apiClient.post(`/projetos/${id}/cancelar`, {
    cancelarEventosVinculados,
  });
  return response.data;
};
