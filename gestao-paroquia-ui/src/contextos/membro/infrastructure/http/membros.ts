import apiClient from '@/shared/infrastructure/http/client';

export interface Membro {
  id: string;
  fielId: string;
  fielNome?: string;
  fielEmail?: string;
  fielTelefone?: string;
  grupoParoquialId: string;
  grupoParoquialNome?: string;
  /** Aliases para compatibilidade */
  organizacaoId?: string;
  organizacaoNome?: string;
  papel: string;
  dataEntrada?: string;
  ativo: boolean;
  igrejaVinculoId?: string | null;
  igrejaId?: string;
}

export interface MembroRequest {
  fielId: string;
  grupoParoquialId: string;
  papel: string;
  dataEntrada?: string;
  igrejaVinculoId?: string;
  ativo?: boolean;
}

export interface PromoverACoordenadorPayload {
  fielId: string;
  grupoParoquialId: string;
  papel: string;
  username?: string;
  password?: string;
}

export interface PromoverACoordenadorResponse {
  membroId: string;
  usuarioId: string;
  novoUsuarioCriado: boolean;
}

export const getMembros = async (params?: {
  grupoParoquialId?: string;
  fielId?: string;
  ativo?: boolean;
}): Promise<{ content: Membro[] }> => {
  const response = await apiClient.get<{ content: Membro[] }>('/membros', { params });
  return response.data;
};

export const createMembro = async (request: MembroRequest): Promise<Membro> => {
  const response = await apiClient.post<Membro>('/membros', request);
  return response.data;
};

export const updateMembro = async (id: string, request: MembroRequest): Promise<Membro> => {
  const response = await apiClient.patch<Membro>(`/membros/${id}`, request);
  return response.data;
};

export const toggleMembroStatus = async (id: string, ativo: boolean): Promise<void> => {
  await apiClient.patch(`/membros/${id}/status`, null, { params: { ativo } });
};

export const promoverACoordenador = async (
  payload: PromoverACoordenadorPayload
): Promise<PromoverACoordenadorResponse> => {
  const response = await apiClient.post<PromoverACoordenadorResponse>(
    '/membros/promover-coordenador',
    payload
  );
  return response.data;
};
