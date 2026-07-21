import apiClient from './client';

export interface Usuario {
  id: string;
  username: string;
  nome: string;
  telefone?: string;
  dataNascimento?: string;
  enabled: boolean;
  igrejaId?: string;
  igrejaNome?: string;
}

export interface MembershipResponse {
  id: string;
  organizacaoId: string;
  organizacaoNome: string;
  tipoOrganizacao: string;
  papel: string;
  ativo: boolean;
  igrejaVinculoId?: string | null;
}

export interface AddMembershipRequest {
  organizacaoId: string;
  tipo: string;
  papel: string;
  igrejaVinculoId?: string;
}

export const getUsuarios = async (): Promise<Usuario[]> => {
  const response = await apiClient.get<Usuario[]>('/usuarios');
  return response.data;
};

export const approveUsuario = async (id: string): Promise<void> => {
  await apiClient.put(`/usuarios/${id}/approve`);
};

export const getMemberships = async (id: string): Promise<MembershipResponse[]> => {
  const response = await apiClient.get<MembershipResponse[]>(`/usuarios/${id}/membros`);
  return response.data;
};

export const addMembership = async (userId: string, request: AddMembershipRequest): Promise<string> => {
  const response = await apiClient.post<string>(`/usuarios/${userId}/membros`, request);
  return response.data;
};

export const removeMembership = async (userId: string, membershipId: string): Promise<void> => {
  await apiClient.delete(`/usuarios/${userId}/membros/${membershipId}`);
};

export interface SolicitacaoOrganizacaoRequest {
  organizacaoId: string;
  igrejaVinculoId?: string | null;
}

export interface RegistrarUsuarioRequest {
  nome: string;
  username: string;
  password?: string;
  telefone: string;
  dataNascimento: string;
  igrejaId: string;
  organizacoesSolicitadas?: SolicitacaoOrganizacaoRequest[];
}

export const registrarUsuario = async (request: RegistrarUsuarioRequest): Promise<string> => {
  const response = await apiClient.post<string>('/usuarios/registrar', request);
  return response.data;
};

export default apiClient;
