import apiClient from './client';
import {AprovacaoDecisionRequest, AprovacaoDecisionResponse, AprovacaoResponse} from './types';

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * Busca a fila paginada de solicitações de aprovação de eventos (criação, edição,
 * alteração de horário, cancelamento, reclassificação, alocação de ativo por
 * empréstimo, etc. — ver `tipoSolicitacao` em `AprovacaoResponse`).
 * @param status Filtra pelo status da solicitação (`PENDENTE`, `APROVADA`, `REPROVADA`,
 * `FALHA_EXECUCAO`); omitido retorna todos os status.
 * @param page Índice da página (0-based).
 * @param size Tamanho da página.
 * @returns Página de solicitações; `content` traz os itens e os demais campos os metadados de paginação.
 */
export const getAprovacoes = async (
  status?: string,
  page: number = 0,
  size: number = 10
): Promise<PaginatedResponse<AprovacaoResponse>> => {
  const params: Record<string, any> = { page, size };
  if (status) {
    params.status = status;
  }

  const response = await apiClient.get<PaginatedResponse<AprovacaoResponse>>('/aprovacoes', {
    params,
  });
  return response.data;
};

/**
 * Decide (aprova ou reprova) uma solicitação de aprovação pendente. Ao aprovar, o
 * backend executa a ação original que gerou a solicitação (ex.: efetivar a criação/edição
 * do evento); se a execução falhar, a solicitação registra `mensagemErroExecucao` e o
 * status `FALHA_EXECUCAO` em vez de `APROVADA`.
 * @param id Identificador da solicitação de aprovação (não do evento/recurso associado).
 * @param request Decisão (`APROVADA`/`REPROVADA`) e observação opcional do decisor.
 * @returns O resultado da decisão, incluindo `actionExecution` com o desfecho da ação
 * executada (evento afetado, novo status, código de erro, se houver).
 */
export const decideAprovacao = async (
  id: string,
  request: AprovacaoDecisionRequest
): Promise<AprovacaoDecisionResponse> => {
  const response = await apiClient.patch<AprovacaoDecisionResponse>(`/aprovacoes/${id}`, request);
  return response.data;
};
