import apiClient from '@/shared/infrastructure/http/client';
import {
  Assento,
  AssentoResumo,
  AverbacaoPayload,
  Certidao,
  HistoricoEntrada,
  LavrarAssentoPayload,
  RetificacaoPayload,
  TeorPayload,
  TipoSacramento,
} from '@/contextos/sacramento/domain/sacramento';

const BASE = '/sacramentos';

/** Lista assentos visíveis ao ator; o livro reservado só vem para Pároco e Bispo Diocesano. */
export const listarAssentos = async (tipo?: TipoSacramento): Promise<AssentoResumo[]> =>
  (await apiClient.get<AssentoResumo[]>(BASE, { params: tipo ? { tipo } : undefined })).data;

/** Visão 360º: assentos em que o fiel ou clérigo está vinculado (respeita a visibilidade do ator). */
export const listarAssentosVinculados = async (pessoaId: string): Promise<AssentoResumo[]> =>
  (await apiClient.get<AssentoResumo[]>(`${BASE}/vinculados/${pessoaId}`)).data;

export const obterAssento = async (id: string): Promise<Assento> =>
  (await apiClient.get<Assento>(`${BASE}/${id}`)).data;

/** Lavra um rascunho; fiel/clérigo são opcionais (vínculo tardio). */
export const lavrarAssento = async (payload: LavrarAssentoPayload): Promise<Assento> =>
  (await apiClient.post<Assento>(BASE, payload)).data;

export const atualizarRascunho = async (id: string, teor: TeorPayload): Promise<Assento> =>
  (await apiClient.put<Assento>(`${BASE}/${id}`, { teor })).data;

/** Ato privativo do Pároco: sela o termo e torna o teor somente leitura. */
export const homologarAssento = async (id: string): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/homologacao`)).data;

export const vincularParticipante = async (
  id: string,
  participanteId: string,
  vinculo: { fielId?: string; clerigoId?: string },
): Promise<Assento> =>
  (await apiClient.patch<Assento>(`${BASE}/${id}/participantes/${participanteId}/vinculo`, vinculo)).data;

/** Ato do Pároco: aprova o vínculo proposto pela Secretaria. */
export const aprovarVinculo = async (id: string, participanteId: string): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/participantes/${participanteId}/vinculo/aprovacao`)).data;

/** Ato do Pároco: rejeita o vínculo proposto, removendo o ponteiro. */
export const rejeitarVinculo = async (id: string, participanteId: string): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/participantes/${participanteId}/vinculo/rejeicao`)).data;

export const desvincularParticipante = async (id: string, participanteId: string): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/participantes/${participanteId}/desvinculo`)).data;

export const averbarBatismo = async (id: string, payload: AverbacaoPayload): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/averbacoes`, payload)).data;

export const homologarAverbacao = async (id: string, averbacaoId: string): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/averbacoes/${averbacaoId}/homologacao`)).data;

export const retificarAssento = async (id: string, payload: RetificacaoPayload): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/retificacoes`, payload)).data;

export const cancelarAssento = async (id: string, motivo: string): Promise<Assento> =>
  (await apiClient.post<Assento>(`${BASE}/${id}/cancelamento`, { motivo })).data;

export const emitirCertidao = async (id: string): Promise<Certidao> =>
  (await apiClient.get<Certidao>(`${BASE}/${id}/certidao`)).data;

export const obterHistorico = async (id: string): Promise<HistoricoEntrada[]> =>
  (await apiClient.get<HistoricoEntrada[]>(`${BASE}/${id}/historico`)).data;
