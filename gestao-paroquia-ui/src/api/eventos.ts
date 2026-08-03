import apiClient from './client';
import { Evento } from './types';

export interface ListEventosParams {
  dataInicio: string;
  dataFim: string;
  organizacaoIds?: string[];
}

/**
 * Busca os eventos do calendário dentro de um intervalo de datas, opcionalmente
 * filtrados por organização(ões). Fixa `size=200` (o backend pagina, mas a UI de
 * calendário carrega o intervalo inteiro de uma vez) e serializa `organizacaoIds`
 * sem índices (`?organizacaoIds=a&organizacaoIds=b`, via `paramsSerializer: { indexes: null }`)
 * para compatibilidade com o binding de array do Spring.
 *
 * Cada `Evento` retornado pode carregar sinais de conflito de agenda
 * (`estadoConflito`/`motivoConflito`) e de aprovação pendente (`aprovacaoPendenteId`,
 * `tipoSolicitacaoPendente`, `tipoAcaoPendente`) quando o evento está bloqueado por
 * uma solicitação em andamento — a UI usa esses campos para sinalizar o estado do
 * evento sem precisar de uma segunda chamada.
 *
 * A resposta do backend pode vir paginada (`{ content: [...] }`) ou como array puro;
 * esta função normaliza ambos os formatos, retornando `[]` se nenhum for reconhecido.
 * @param params Intervalo (`dataInicio`/`dataFim`) e organizações a filtrar.
 */
export const listEventos = async (params: ListEventosParams): Promise<Evento[]> => {
  const response = await apiClient.get<any>('/eventos', {
    params: { ...params, size: 200 },
    paramsSerializer: { indexes: null },
  });
  if (response.data && Array.isArray(response.data.content)) {
    return response.data.content;
  }
  if (Array.isArray(response.data)) {
    return response.data;
  }
  return [];
};
