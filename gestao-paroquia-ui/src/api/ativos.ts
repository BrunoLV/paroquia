import apiClient from './client';
import {
  AtivoPatrimonial,
  AlocarAtivoPayload,
  AlocarAtivoResultado,
  AtivoAlocacaoResponse,
  CancelarAlocacaoPayload,
  CreateAtivoPayload,
  DesalocarAtivoPayload,
  IniciarManutencaoPayload,
  MetricasOciosidade,
  TransferirAtivoPayload,
} from './types';

/**
 * Busca os ativos patrimoniais pertencentes a uma organização (posse atual do bem).
 */
export const getAtivosPorOrganizacao = async (organizacaoId: string): Promise<AtivoPatrimonial[]> => {
  const response = await apiClient.get<AtivoPatrimonial[]>(`/ativos/organizacao/${organizacaoId}`);
  return response.data;
};

/**
 * Cadastra um novo ativo patrimonial, vinculado desde a criação a uma organização
 * (dona/possuidora do bem) e com status inicial `DISPONIVEL`.
 */
export const createAtivo = async (payload: CreateAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.post<AtivoPatrimonial>('/ativos', payload);
  return response.data;
};

/**
 * Transfere a posse de um ativo para outra organização, exigindo justificativa
 * para fins de rastreabilidade patrimonial.
 */
export const transferirAtivo = async (id: string, payload: TransferirAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}/transferir`, payload);
  return response.data;
};

/**
 * Aloca (reserva) um ativo para uso em um período/evento, informando justificativa
 * e datas de início/fim. A alocação pode ser confirmada de imediato ou depender de
 * aprovação (ex.: empréstimo entre organizações): o backend responde com HTTP 202
 * quando fica pendente, e o retorno reflete essa bifurcação via `kind`.
 * @returns `{ kind: 'ALOCADO', ativo }` quando a alocação é confirmada de imediato,
 * ou `{ kind: 'PENDENTE', pendente }` quando depende de uma solicitação de aprovação.
 */
export const alocarAtivo = async (id: string, payload: AlocarAtivoPayload): Promise<AlocarAtivoResultado> => {
  const response = await apiClient.post(`/ativos/${id}/alocar`, payload);
  if (response.status === 202) {
    return { kind: 'PENDENTE', pendente: response.data };
  }
  return { kind: 'ALOCADO', ativo: response.data };
};

/**
 * Desaloca (libera) um ativo, retornando-o para o status `DISPONIVEL`. A observação
 * é opcional e serve apenas de registro (ex.: motivo da devolução antecipada).
 */
export const desalocarAtivo = async (id: string, payload?: DesalocarAtivoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}/desalocar`, payload || {});
  return response.data;
};

/**
 * Coloca um ativo em manutenção por um período determinado, bloqueando novas
 * alocações enquanto o status permanecer `EM_MANUTENCAO`.
 */
export const iniciarManutencao = async (id: string, payload: IniciarManutencaoPayload): Promise<AtivoPatrimonial> => {
  const response = await apiClient.post<AtivoPatrimonial>(`/ativos/${id}/manutencao`, payload);
  return response.data;
};

/**
 * Encerra a manutenção em andamento de um ativo, retornando-o ao status `DISPONIVEL`.
 */
export const finalizarManutencao = async (id: string): Promise<AtivoPatrimonial> => {
  const response = await apiClient.patch<AtivoPatrimonial>(`/ativos/${id}/manutencao/finalizar`);
  return response.data;
};

/**
 * Cancela uma alocação (reserva/empréstimo) já existente de um ativo, exigindo o
 * motivo do cancelamento. Transiciona a alocação para o status `CANCELADA`.
 */
export const cancelarAlocacao = async (alocacaoId: string, payload: CancelarAlocacaoPayload): Promise<void> => {
  await apiClient.patch(`/ativos/alocacoes/${alocacaoId}/cancelar`, payload);
};

/**
 * Busca o histórico/agenda de alocações (reservas, empréstimos, manutenções) de um ativo específico.
 */
export const getAlocacoesPorAtivo = async (ativoId: string): Promise<AtivoAlocacaoResponse[]> => {
  const response = await apiClient.get<AtivoAlocacaoResponse[]>(`/ativos/${ativoId}/alocacoes`);
  return response.data;
};

/**
 * Busca as alocações de ativos patrimoniais vinculadas a um evento específico —
 * usado para listar quais bens estão reservados para aquele evento.
 */
export const getAlocacoesPorEvento = async (eventoId: string): Promise<AtivoAlocacaoResponse[]> => {
  const response = await apiClient.get<AtivoAlocacaoResponse[]>(`/ativos/evento/${eventoId}/alocacoes`);
  return response.data;
};

/**
 * Busca métricas agregadas de ociosidade do patrimônio: totais por status
 * (disponível, alocado, em manutenção, baixado), taxa de ociosidade geral e
 * distribuição de ativos por categoria — usado em dashboards/relatórios.
 */
export const getMetricasOciosidade = async (): Promise<MetricasOciosidade> => {
  const response = await apiClient.get<MetricasOciosidade>('/ativos/metricas/ociosidade');
  return response.data;
};
