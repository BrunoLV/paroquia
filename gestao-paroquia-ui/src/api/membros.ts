import apiClient from './client';

export interface Membro {
  id: string;
  usuarioId: string;
  usuarioNome?: string;
  usuarioEmail?: string;
  usuarioTelefone?: string;
  organizacaoId: string;
  organizacaoNome?: string;
  papel: string;
  dataEntrada?: string;
  ativo: boolean;
  igrejaId?: string;
}

export interface MembroRequest {
  usuarioId: string;
  organizacaoId: string;
  papel: string;
  dataEntrada?: string;
  igrejaId?: string;
  ativo?: boolean;
}

export const getMembros = async (params?: { organizacaoId?: string; usuarioId?: string; ativo?: boolean }): Promise<{ content: Membro[] }> => {
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
