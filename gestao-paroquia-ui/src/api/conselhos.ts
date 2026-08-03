import apiClient from './client';
import { CpcDecisao, StatusHomologacao } from './types';

/**
 * Busca as decisões registradas pelo Conselho Pastoral Comunitário (CPC), com o
 * fluxo de homologação em qualquer estágio (pendente, homologada ou vetada).
 * Uma decisão do CPC é uma deliberação de reunião de conselho que só produz
 * efeito administrativo depois de homologada pela instância competente.
 * @param params Filtros opcionais: `comunidadeId` restringe a uma comunidade
 * específica e `status` restringe ao estágio de homologação desejado.
 */
export const getCpcDecisoes = async (params?: { comunidadeId?: string; status?: StatusHomologacao }): Promise<CpcDecisao[]> => {
  const response = await apiClient.get<CpcDecisao[]>('/conselhos/decisoes', { params });
  return response.data;
};

/**
 * Registra uma nova decisão de reunião do Conselho Pastoral Comunitário.
 * A decisão nasce sempre no estágio `PENDENTE_HOMOLOGACAO` — ela ainda precisa
 * ser homologada (ou vetada) antes de valer como deliberação oficial.
 */
export const createCpcDecisao = async (data: {
  titulo: string;
  pautaDescricao: string;
  dataReuniao: string;
  comunidadeId: string;
}): Promise<CpcDecisao> => {
  const response = await apiClient.post<CpcDecisao>('/conselhos/decisoes', data);
  return response.data;
};

/**
 * Homologa ou veta uma decisão do CPC, encerrando o fluxo de deliberação.
 * `status` deve ser `HOMOLOGADO` ou `VETADO`; ao vetar, `motivoVeto` documenta
 * a justificativa formal da recusa, exigida para fins de transparência/ata.
 */
export const homologarCpcDecisao = async (
  id: string,
  data: { status: StatusHomologacao; motivoVeto?: string }
): Promise<CpcDecisao> => {
  const response = await apiClient.patch<CpcDecisao>(`/conselhos/decisoes/${id}/homologacao`, data);
  return response.data;
};
