import apiClient from '@/shared/infrastructure/http/client';

export interface Usuario {
  id: string;
  username: string;
  fielId?: string;
  nome: string;
  telefone?: string;
  dataNascimento?: string;
  enabled: boolean;
  igrejaId?: string;
  igrejaNome?: string;
  batizado?: boolean;
  primeiraComunhao?: boolean;
  crismado?: boolean;
  casadoIgrejaCatolica?: boolean;
}

export interface CreateUsuarioRequest {
  fielId: string;
  username: string;
  password?: string;
}

export interface VinculoResponse {
  id: string;
  organizacaoId: string;
  organizacaoNome?: string;
  tipoOrganizacao: string;
  papel: string;
  ativo: boolean;
  igrejaVinculoId?: string | null;
  igrejaVinculoNome?: string | null;
}

export interface AdicionarVinculoRequest {
  grupoParoquialId?: string;
  organizacaoId?: string;
  tipo: string;
  papel: string;
  igrejaVinculoId?: string;
}

/**
 * Busca a lista de usuários cadastrados no sistema.
 */
export const getUsuarios = async (): Promise<Usuario[]> => {
  const response = await apiClient.get<Usuario[]>('/usuarios');
  return response.data;
};

/**
 * Cria um novo usuário vinculado a um Fiel existente.
 */
export const createUsuario = async (request: CreateUsuarioRequest): Promise<string> => {
  const response = await apiClient.post<string>('/usuarios', request);
  return response.data;
};

/**
 * Aprova o cadastro de um usuário pendente.
 */
export const approveUsuario = async (id: string): Promise<void> => {
  await apiClient.put(`/usuarios/${id}/aprovar`);
};

/**
 * Busca a lista de vínculos de um usuário com organizações da paróquia.
 */
export const getVinculos = async (id: string): Promise<VinculoResponse[]> => {
  const response = await apiClient.get<VinculoResponse[]>(`/usuarios/${id}/membros`);
  return response.data;
};

/**
 * Adiciona um novo vínculo de organização a um usuário.
 */
export const adicionarVinculo = async (userId: string, request: AdicionarVinculoRequest): Promise<string> => {
  const response = await apiClient.post<string>(`/usuarios/${userId}/membros`, request);
  return response.data;
};

/**
 * Remove um vínculo de organização de um usuário.
 */
export const removerVinculo = async (userId: string, vinculoId: string): Promise<void> => {
  await apiClient.delete(`/usuarios/${userId}/membros/${vinculoId}`);
};

export default apiClient;
