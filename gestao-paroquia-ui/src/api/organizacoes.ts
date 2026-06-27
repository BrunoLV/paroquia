import apiClient from './client';
import { Organizacao } from './types';

export const getOrganizacoes = async (): Promise<Organizacao[]> => {
  const response = await apiClient.get<Organizacao[]>('/organizacoes');
  return response.data;
};

export const createOrganizacao = async (organizacao: Omit<Organizacao, 'id'>): Promise<Organizacao> => {
  const response = await apiClient.post<Organizacao>('/organizacoes', organizacao);
  return response.data;
};

export const updateOrganizacao = async (
  id: string,
  organizacao: Omit<Organizacao, 'id'> & { id?: string }
): Promise<Organizacao> => {
  const response = await apiClient.put<Organizacao>(`/organizacoes/${id}`, organizacao);
  return response.data;
};

export const deleteOrganizacao = async (id: string): Promise<void> => {
  await apiClient.delete(`/organizacoes/${id}`);
};

export interface OrganizacaoMembro {
  membershipId: string;
  usuarioId: string;
  usuarioNome: string;
  papel: string;
  ativo: boolean;
}

export const getOrganizacaoMembros = async (id: string): Promise<OrganizacaoMembro[]> => {
  const response = await apiClient.get<OrganizacaoMembro[]>(`/organizacoes/${id}/membros`);
  return response.data;
};
