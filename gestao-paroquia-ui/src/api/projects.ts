import apiClient from './client';
import {Projeto, ProjetoAprovacaoResponse} from './types';

/**
 * Busca as solicitações de aprovação de projetos (criação ou cancelamento).
 * Criação e cancelamento de projetos passam pelo fluxo de aprovação — este endpoint
 * lista as solicitações pendentes/decididas para essa fila, não os projetos em si.
 * @param status Filtra por status da solicitação (`PENDENTE`, `APROVADA`, `REPROVADA`); omitido retorna todas.
 * @returns Lista de solicitações, incluindo `cancelarEventosVinculados` quando o tipo for `CANCELAMENTO`.
 */
export const getAprovacoesProjeto = async (status?: string): Promise<ProjetoAprovacaoResponse[]> => {
  const response = await apiClient.get<ProjetoAprovacaoResponse[]>('/projetos/solicitacoes', {
    params: status ? { status } : {},
  });
  return response.data;
};

/**
 * Decide (aprova ou reprova) uma solicitação pendente de criação/cancelamento de projeto.
 * Ao aprovar, o backend executa a ação original (cria ou cancela o projeto); ao reprovar,
 * a solicitação é apenas encerrada sem efeito sobre o projeto.
 * @param id Identificador da solicitação de aprovação (não do projeto).
 * @param aprovado `true` para aprovar, `false` para reprovar.
 * @param observacao Justificativa/observação do decisor, registrada no histórico.
 */
export const decideAprovacaoProjeto = async (
  id: string,
  aprovado: boolean,
  observacao: string
): Promise<ProjetoAprovacaoResponse> => {
  const response = await apiClient.post<ProjetoAprovacaoResponse>(`/projetos/solicitacoes/${id}/decisao`, {
    aprovado,
    observacao,
  });
  return response.data;
};

/**
 * Inicia um projeto aprovado, transicionando seu status de `ABERTO` para `EM_ANDAMENTO`.
 */
export const iniciarProjeto = async (id: string): Promise<Projeto> => {
  const response = await apiClient.post<Projeto>(`/projetos/${id}/iniciar`);
  return response.data;
};

/**
 * Conclui um projeto em andamento, transicionando seu status para `CONCLUIDO`.
 */
export const concluirProjeto = async (id: string): Promise<Projeto> => {
  const response = await apiClient.post<Projeto>(`/projetos/${id}/concluir`);
  return response.data;
};

/**
 * Solicita o cancelamento de um projeto. Assim como a criação, o cancelamento passa
 * pelo fluxo de aprovação — a chamada gera uma solicitação pendente, não cancela o
 * projeto imediatamente.
 * @param id Identificador do projeto a ser cancelado.
 * @param cancelarEventosVinculados Se `true`, os eventos vinculados ao projeto também
 * serão cancelados quando a solicitação for aprovada; se `false`, permanecem intactos.
 */
export const cancelarProjeto = async (
  id: string,
  cancelarEventosVinculados: boolean
): Promise<ProjetoAprovacaoResponse | Projeto> => {
  const response = await apiClient.post<ProjetoAprovacaoResponse | Projeto>(`/projetos/${id}/cancelar`, {
    cancelarEventosVinculados,
  });
  return response.data;
};
