import apiClient from './client';
import {
  AtivoManutencao,
  AtivoPatrimonial,
  CadastrarAtivoPayload,
  EditarDescricaoAtivoPayload,
  EnviarManutencaoPayload,
  MetricasOciosidade,
  ReservaImpeditiva,
} from './types';

/** Lista todos os ativos patrimoniais não excluídos da paróquia. */
export const getAtivos = async (): Promise<AtivoPatrimonial[]> => {
  const response = await apiClient.get<AtivoPatrimonial[]>('/ativos');
  return response.data;
};

/** Lista o inventário de ativos de uma unidade territorial (posse fixa do bem). */
export const getAtivosPorOrganizacao = async (grupoParoquialId: string): Promise<AtivoPatrimonial[]> => {
  const response = await apiClient.get<AtivoPatrimonial[]>(`/ativos/organizacao/${grupoParoquialId}`);
  return response.data;
};

/** Detalhe de um ativo. */
export const getAtivo = async (id: string): Promise<AtivoPatrimonial> => {
  const response = await apiClient.get<AtivoPatrimonial>(`/ativos/${id}`);
  return response.data;
};

/** Cadastra um novo ativo (status inicial DISPONIVEL, código de tombamento gerado pelo sistema). */
export const cadastrarAtivo = async (payload: CadastrarAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.post<AtivoPatrimonial>('/ativos', payload);
  return response.data;
};

/** Edita o ativo — apenas a descrição é aceita (imutabilidade estrutural). */
export const editarDescricaoAtivo = async (
  id: string,
  payload: EditarDescricaoAtivoPayload,
): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}`, payload);
  return response.data;
};

/** Exclusão lógica (soft delete) do ativo — desincorporação patrimonial. */
export const excluirAtivo = async (id: string): Promise<void> => {
  await apiClient.delete(`/ativos/${id}`);
};

/** Envia o ativo (DISPONIVEL) para manutenção por um período determinado. */
export const enviarParaManutencao = async (
  id: string,
  payload: EnviarManutencaoPayload,
): Promise<AtivoManutencao> => {
  const response = await apiClient.post<AtivoManutencao>(`/ativos/${id}/manutencoes`, payload);
  return response.data;
};

/** Pré-checa reservas que impediriam a manutenção no intervalo informado. */
export const verificarConflitoManutencao = async (
  id: string,
  params: { dataInicio: string; dataFimPrevista: string },
): Promise<ReservaImpeditiva[]> => {
  const response = await apiClient.get<ReservaImpeditiva[]>(`/ativos/${id}/manutencoes/conflitos`, { params });
  return response.data;
};

/** Conclui a manutenção em aberto, devolvendo o ativo para DISPONIVEL. */
export const concluirManutencao = async (id: string): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}/manutencoes/atual/concluir`);
  return response.data;
};

/** Linha do tempo de manutenções do ativo (mais recente primeiro). */
export const getHistoricoManutencao = async (id: string): Promise<AtivoManutencao[]> => {
  const response = await apiClient.get<AtivoManutencao[]>(`/ativos/${id}/manutencoes`);
  return response.data;
};

/** Métricas de ociosidade do acervo. */
export const getMetricasOciosidade = async (): Promise<MetricasOciosidade> => {
  const response = await apiClient.get<MetricasOciosidade>('/ativos/metricas/ociosidade');
  return response.data;
};
