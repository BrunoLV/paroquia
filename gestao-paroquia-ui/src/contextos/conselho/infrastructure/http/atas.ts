import apiClient from '@/shared/infrastructure/http/client';
import { baixarBlob, nomeDoArquivo } from '@/shared/utils/downloadBlob';
import {
  Ata,
  ClerigoOpcao,
  ConteudoAtaPayload,
  FiltroAtas,
  OpcoesExportacao,
  PaginaAtas,
  PresencaRedator,
  RascunharAtaPayload,
  RegistroAuditoria,
  TipoReuniao,
  TipoReuniaoInfo,
} from '@/contextos/conselho/domain/ata';

const BASE = '/conselhos';

/** Tipos de reunião com as regras de cada um (homologação, quórum, presença, votação). */
export const listarTiposReuniao = async (): Promise<TipoReuniaoInfo[]> =>
  (await apiClient.get<TipoReuniaoInfo[]>(`${BASE}/tipos-reuniao`)).data;

/** Clérigos ativos que o redator pode incluir na presença; vazio para quem não redige nenhum órgão. */
export const listarClero = async (): Promise<ClerigoOpcao[]> => (await apiClient.get<ClerigoOpcao[]>(`${BASE}/clero`)).data;

/** Presença do próprio redator na ata nova; `null` se ele não redige o tipo no órgão ou não tem cadastro de fiel. */
export const obterPresencaRedator = async (tipo: TipoReuniao, organizacaoId: string): Promise<PresencaRedator | null> => {
  const resposta = await apiClient.get<PresencaRedator | ''>(`${BASE}/redator-presenca`, { params: { tipo, organizacaoId } });
  return resposta.status === 204 || !resposta.data ? null : resposta.data;
};

/** Atas visíveis ao usuário; o escopo por órgão é aplicado no servidor. */
export const listarAtas = async (filtro: FiltroAtas = {}): Promise<PaginaAtas> =>
  (await apiClient.get<PaginaAtas>(`${BASE}/atas`, { params: filtro })).data;

export const obterAta = async (id: string): Promise<Ata> => (await apiClient.get<Ata>(`${BASE}/atas/${id}`)).data;

/** Abre uma ata em rascunho; status, redator e número são definidos pelo servidor. */
export const rascunharAta = async (payload: RascunharAtaPayload): Promise<Ata> =>
  (await apiClient.post<Ata>(`${BASE}/atas`, payload)).data;

/** Substitui o conteúdo integral do rascunho. */
export const atualizarAta = async (id: string, conteudo: ConteudoAtaPayload): Promise<Ata> =>
  (await apiClient.put<Ata>(`${BASE}/atas/${id}`, conteudo)).data;

export const submeterAta = async (id: string): Promise<Ata> =>
  (await apiClient.post<Ata>(`${BASE}/atas/${id}/submissao`)).data;

/** Ato privativo do Pároco: sela a ata e lhe atribui o número. */
export const homologarAta = async (id: string): Promise<Ata> =>
  (await apiClient.post<Ata>(`${BASE}/atas/${id}/homologacao`)).data;

export const vetarAta = async (id: string, motivo: string): Promise<Ata> =>
  (await apiClient.post<Ata>(`${BASE}/atas/${id}/veto`, { motivo })).data;

export const reabrirAta = async (id: string): Promise<Ata> =>
  (await apiClient.post<Ata>(`${BASE}/atas/${id}/reabertura`)).data;

/** Abre a ata retificadora de uma ata selada; a original permanece intacta. */
export const retificarAta = async (id: string, justificativa: string): Promise<Ata> =>
  (await apiClient.post<Ata>(`${BASE}/atas/${id}/retificacao`, { justificativa })).data;

/** Trilha de auditoria da ata; restrita a Pároco, Bispo diocesano e secretaria paroquial. */
export const obterTrilha = async (id: string): Promise<RegistroAuditoria[]> =>
  (await apiClient.get<RegistroAuditoria[]>(`${BASE}/atas/${id}/historico`)).data;

const baixar = async (url: string, params: object, reserva: string): Promise<void> => {
  const resposta = await apiClient.get<Blob>(url, { params, responseType: 'blob' });
  const disposicao = resposta.headers['content-disposition'] as string | undefined;
  baixarBlob(resposta.data, nomeDoArquivo(disposicao, reserva));
};

/** Baixa uma ata em PDF ou Word; ata não selada sai como minuta, sem efeito. */
export const baixarAta = (id: string, opcoes: OpcoesExportacao): Promise<void> =>
  baixar(`${BASE}/atas/${id}/exportacao`, opcoes, `ata.${opcoes.formato.toLowerCase()}`);

/** Baixa a compilação (livro de atas e decisões) das atas seladas do filtro. */
export const baixarCompilacao = (filtro: FiltroAtas, opcoes: OpcoesExportacao): Promise<void> =>
  baixar(`${BASE}/atas/exportacao`, { ...filtro, ...opcoes }, `livro-de-atas.${opcoes.formato.toLowerCase()}`);
