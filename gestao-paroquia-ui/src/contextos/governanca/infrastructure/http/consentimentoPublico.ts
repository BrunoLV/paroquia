import apiClient from '@/shared/infrastructure/http/client';

export interface SolicitarCodigoConsentimentoRequest {
  cpf: string;
}

export interface SolicitarCodigoConsentimentoResponse {
  emailMascarado: string;
  expiraEmSegundos: number;
  mensagem: string;
}

export interface ValidarCodigoConsentimentoRequest {
  cpf: string;
  codigo: string;
}

export interface ValidarCodigoConsentimentoResponse {
  token: string;
  titularTipo: string;
  titularId: string;
  nome: string;
  isMenor: boolean;
  termoAceito: boolean;
  termoVersao: string;
  termoHash: string;
  textoTermo: string;
  usoImagemAutorizado: boolean;
  permitirDivulgacaoAniversario: boolean;
  responsavelMenorNome: string | null;
  responsavelMenorCpf: string | null;
  dataHoraUltimaManifestacaoUtc: string | null;
}

export interface ManifestarConsentimentoRequest {
  termoAceito: boolean;
  usoImagemAutorizado: boolean;
  permitirDivulgacaoAniversario: boolean;
  responsavelMenorNome?: string | null;
  responsavelMenorCpf?: string | null;
}

export interface ComprovanteConsentimentoResponse {
  protocolo: string;
  titularNome: string;
  tipoEvento: string;
  dataHoraUtc: string;
  termoVersao: string;
  termoHash: string;
  mensagem: string;
}

export interface HistoricoConsentimentoItem {
  id: string;
  tipoEvento: string;
  canal: string;
  metodoAutenticacao: string;
  termoVersao: string;
  termoHash: string;
  termoAceito: boolean;
  usoImagemAutorizado: boolean;
  permitirDivulgacaoAniversario: boolean;
  responsavelMenorNome?: string | null;
  responsavelMenorCpf?: string | null;
  ipOrigem?: string | null;
  userAgent?: string | null;
  criadoEmUtc: string;
}

export interface CriarSolicitacaoDpoRequest {
  titularNome: string;
  titularEmail: string;
  titularCpf?: string;
  tipoSolicitacao: string;
  mensagem: string;
}

export interface SolicitacaoDpoResponse {
  id: string;
  protocolo: string;
  titularNome: string;
  titularEmail: string;
  titularCpf?: string;
  tipoSolicitacao: string;
  mensagem: string;
  status: 'RECEBIDA' | 'EM_ANALISE' | 'ATENDIDA' | 'REJEITADA';
  resposta?: string;
  prazoRespostaUtc: string;
  respondidoEmUtc?: string;
  criadoEmUtc: string;
}

export interface ResponderSolicitacaoDpoRequest {
  resposta: string;
  status: 'ATENDIDA' | 'REJEITADA' | 'EM_ANALISE';
}

export const solicitarCodigoConsentimento = async (
  payload: SolicitarCodigoConsentimentoRequest
): Promise<SolicitarCodigoConsentimentoResponse> => {
  const response = await apiClient.post<SolicitarCodigoConsentimentoResponse>(
    '/publico/consentimento/solicitar-codigo',
    payload
  );
  return response.data;
};

export const validarCodigoConsentimento = async (
  payload: ValidarCodigoConsentimentoRequest
): Promise<ValidarCodigoConsentimentoResponse> => {
  const response = await apiClient.post<ValidarCodigoConsentimentoResponse>(
    '/publico/consentimento/validar-codigo',
    payload
  );
  return response.data;
};

export const manifestarConsentimento = async (
  token: string,
  payload: ManifestarConsentimentoRequest
): Promise<ComprovanteConsentimentoResponse> => {
  const response = await apiClient.post<ComprovanteConsentimentoResponse>(
    '/publico/consentimento/manifestar',
    payload,
    {
      headers: {
        'X-Consent-Token': token,
      },
    }
  );
  return response.data;
};

export const revogarConsentimento = async (
  token: string
): Promise<ComprovanteConsentimentoResponse> => {
  const response = await apiClient.post<ComprovanteConsentimentoResponse>(
    '/publico/consentimento/revogar',
    {},
    {
      headers: {
        'X-Consent-Token': token,
      },
    }
  );
  return response.data;
};

export const listarHistoricoConsentimento = async (
  titularTipo: 'FIEL' | 'CLERIGO',
  titularId: string
): Promise<HistoricoConsentimentoItem[]> => {
  const response = await apiClient.get<HistoricoConsentimentoItem[]>(
    `/consentimentos/${titularTipo}/${titularId}/historico`
  );
  return response.data;
};

export const solicitarAoDpo = async (
  payload: CriarSolicitacaoDpoRequest
): Promise<SolicitacaoDpoResponse> => {
  const response = await apiClient.post<SolicitacaoDpoResponse>(
    '/publico/dpo/solicitar',
    payload
  );
  return response.data;
};

export const consultarProtocoloDpo = async (
  protocolo: string
): Promise<SolicitacaoDpoResponse> => {
  const response = await apiClient.get<SolicitacaoDpoResponse>(
    `/publico/dpo/solicitacao/${protocolo}`
  );
  return response.data;
};

export const listarSolicitacoesDpo = async (): Promise<SolicitacaoDpoResponse[]> => {
  const response = await apiClient.get<SolicitacaoDpoResponse[]>(
    '/governanca/dpo/solicitacoes'
  );
  return response.data;
};

export const responderSolicitacaoDpo = async (
  id: string,
  payload: ResponderSolicitacaoDpoRequest
): Promise<SolicitacaoDpoResponse> => {
  const response = await apiClient.post<SolicitacaoDpoResponse>(
    `/governanca/dpo/solicitacoes/${id}/responder`,
    payload
  );
  return response.data;
};
