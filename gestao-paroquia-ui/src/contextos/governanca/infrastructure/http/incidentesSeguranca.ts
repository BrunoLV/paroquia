import apiClient from '@/shared/infrastructure/http/client';

export type GravidadeIncidente = 'BAIXA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type StatusIncidente = 'EM_ANALISE' | 'CONTIDO' | 'NOTIFICADO' | 'ENCERRADO';

export interface IncidenteSeguranca {
  id: string;
  titulo: string;
  descricao: string;
  tipoDadoAfetado: string;
  categoriaGravidade: GravidadeIncidente;
  dataOcorrencia: string;
  dataDeteccao: string;
  medidasMitigacao?: string | null;
  notificadoAnpd: boolean;
  dataNotificacaoAnpd?: string | null;
  protocoloAnpd?: string | null;
  notificadoTitulares: boolean;
  dataNotificacaoTitulares?: string | null;
  status: StatusIncidente;
  criadoEmUtc: string;
}

export interface RegistrarIncidentePayload {
  titulo: string;
  descricao: string;
  tipoDadoAfetado: string;
  categoriaGravidade: GravidadeIncidente;
  dataOcorrencia: string;
  dataDeteccao: string;
  medidasMitigacao?: string;
}

export interface AtualizarStatusIncidentePayload {
  status: StatusIncidente;
  notificadoAnpd?: boolean;
  dataNotificacaoAnpd?: string;
  protocoloAnpd?: string;
  notificadoTitulares?: boolean;
  dataNotificacaoTitulares?: string;
  medidasMitigacao?: string;
}

export const listarIncidentesSeguranca = async (): Promise<IncidenteSeguranca[]> => {
  const response = await apiClient.get<IncidenteSeguranca[]>('/governanca/incidentes');
  return response.data;
};

export const registrarIncidenteSeguranca = async (
  payload: RegistrarIncidentePayload
): Promise<IncidenteSeguranca> => {
  const response = await apiClient.post<IncidenteSeguranca>('/governanca/incidentes', payload);
  return response.data;
};

export const atualizarIncidenteSeguranca = async (
  id: string,
  payload: AtualizarStatusIncidentePayload
): Promise<IncidenteSeguranca> => {
  const response = await apiClient.patch<IncidenteSeguranca>(`/governanca/incidentes/${id}`, payload);
  return response.data;
};
