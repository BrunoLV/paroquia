import apiClient from '@/shared/infrastructure/http/client';
import { IdoneidadeAgente } from '@/shared/domain/types/types';

export interface Membro {
  id: string;
  fielId: string;
  fielNome?: string;
  fielEmail?: string;
  fielTelefone?: string;
  organizacaoId: string;
  organizacaoNome?: string;
  papel: string;
  dataEntrada?: string;
  ativo: boolean;
  igrejaVinculoId?: string | null;
  igrejaId?: string;
}

export interface MembroRequest {
  fielId: string;
  organizacaoId: string;
  papel: string;
  dataEntrada?: string;
  igrejaVinculoId?: string;
  ativo?: boolean;
}

export interface PromoverACoordenadorPayload {
  fielId: string;
  organizacaoId: string;
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
  organizacaoId?: string;
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

export interface RegistroIdoneidade {
  formacaoConcluidaEm: string | null;
  vivenciaSacramentalAtestadaEm: string | null;
  termoProtecaoMenoresEm: string | null;
  termoProtecaoMenoresValidoAte: string | null;
  enviadoEm: string | null;
}

/** Consulta a idoneidade pastoral do agente e as pendências para a função que ele ocupa. */
export const getIdoneidade = async (membroId: string): Promise<IdoneidadeAgente> => {
  const response = await apiClient.get<IdoneidadeAgente>(`/membros/${membroId}/idoneidade`);
  return response.data;
};

/** Registra a idoneidade pastoral do agente (apenas datas atestadas). */
export const registrarIdoneidade = async (
  membroId: string,
  registro: RegistroIdoneidade
): Promise<IdoneidadeAgente> => {
  const response = await apiClient.put<IdoneidadeAgente>(`/membros/${membroId}/idoneidade`, registro);
  return response.data;
};
