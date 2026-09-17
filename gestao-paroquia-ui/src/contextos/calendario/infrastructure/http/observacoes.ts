import apiClient from '@/shared/infrastructure/http/client';
import {Observacao} from '@/shared/domain/types/types';

/**
 * Busca as observações (notas) registradas em um evento, em ordem cronológica.
 * Usado para exibir o histórico de anotações da equipe sobre a preparação/execução do evento.
 * @param eventoId ID do evento ao qual as observações pertencem.
 */
export const getObservacoes = async (eventoId: string): Promise<Observacao[]> => {
  const response = await apiClient.get<Observacao[]>(`/eventos/${eventoId}/observacoes`);
  return response.data;
};

/**
 * Cria uma nova observação em um evento. O tipo é fixado como 'NOTA' — este endpoint
 * cobre apenas anotações livres da equipe, não outros tipos de observação que a API suporte.
 * @param eventoId ID do evento a ser anotado.
 * @param conteudo Texto livre da observação.
 */
export const createObservacao = async (eventoId: string, conteudo: string): Promise<Observacao> => {
  const response = await apiClient.post<Observacao>(`/eventos/${eventoId}/observacoes`, {
    tipo: 'NOTA',
    conteudo,
  });
  return response.data;
};

/**
 * Atualiza o conteúdo de uma observação existente de um evento (edição de nota já registrada).
 * @param eventoId ID do evento dono da observação.
 * @param observacaoId ID da observação a editar.
 * @param conteudo Novo texto da observação.
 */
export const updateObservacao = async (eventoId: string, observacaoId: string, conteudo: string): Promise<Observacao> => {
  const response = await apiClient.patch<Observacao>(`/eventos/${eventoId}/observacoes/${observacaoId}`, {
    conteudo,
  });
  return response.data;
};

/**
 * Remove definitivamente uma observação de um evento.
 * @param eventoId ID do evento dono da observação.
 * @param observacaoId ID da observação a remover.
 */
export const deleteObservacao = async (eventoId: string, observacaoId: string): Promise<void> => {
  await apiClient.delete(`/eventos/${eventoId}/observacoes/${observacaoId}`);
};
