import apiClient from '@/shared/infrastructure/http/client';

export interface ParoquiaConfiguracaoRaw {
  id: string;
  nome: string;
  diocese: string | null;
  endereco: string | null;
  telefone: string | null;
  email: string | null;
  cnpj: string | null;
  corPrimariaHex?: string;
  corSecundariaHex?: string;
  corPrimaria?: string;
  corSecundaria?: string;
  possuiLogoFundoClaro?: boolean;
  possuiLogoFundoEscuro?: boolean;
  temLogoFundoClaro?: boolean;
  temLogoFundoEscuro?: boolean;
  urlLogoFundoClaro?: string;
  urlLogoFundoEscuro?: string;
  logoFundoClaroUrl?: string;
  logoFundoEscuroUrl?: string;
  dpoNome?: string | null;
  dpoEmail?: string | null;
  dpoTelefone?: string | null;
}

export interface ParoquiaConfiguracao {
  id: string;
  nome: string;
  diocese: string | null;
  endereco: string | null;
  telefone: string | null;
  email: string | null;
  cnpj: string | null;
  corPrimaria: string;
  corSecundaria: string;
  temLogoFundoClaro: boolean;
  temLogoFundoEscuro: boolean;
  logoFundoClaroUrl: string;
  logoFundoEscuroUrl: string;
  dpoNome: string | null;
  dpoEmail: string | null;
  dpoTelefone: string | null;
}

export interface AtualizarDpoPayload {
  dpoNome?: string | null;
  dpoEmail?: string | null;
  dpoTelefone?: string | null;
}

export interface AtualizarIdentidadeVisualPayload {
  corPrimaria: string;
  corSecundaria: string;
}

export const normalizarConfiguracao = (data: ParoquiaConfiguracaoRaw): ParoquiaConfiguracao => ({
  id: data.id,
  nome: data.nome,
  diocese: data.diocese ?? null,
  endereco: data.endereco ?? null,
  telefone: data.telefone ?? null,
  email: data.email ?? null,
  cnpj: data.cnpj ?? null,
  corPrimaria: data.corPrimariaHex || data.corPrimaria || '#1A237E',
  corSecundaria: data.corSecundariaHex || data.corSecundaria || '#808080',
  temLogoFundoClaro: data.possuiLogoFundoClaro ?? data.temLogoFundoClaro ?? false,
  temLogoFundoEscuro: data.possuiLogoFundoEscuro ?? data.temLogoFundoEscuro ?? false,
  logoFundoClaroUrl: data.urlLogoFundoClaro || data.logoFundoClaroUrl || '/api/v1/configuracao-paroquia/logo/claro.png',
  logoFundoEscuroUrl: data.urlLogoFundoEscuro || data.logoFundoEscuroUrl || '/api/v1/configuracao-paroquia/logo/escuro.png',
  dpoNome: data.dpoNome ?? null,
  dpoEmail: data.dpoEmail ?? null,
  dpoTelefone: data.dpoTelefone ?? null,
});

export const obterConfiguracaoParoquia = async (): Promise<ParoquiaConfiguracao> => {
  const response = await apiClient.get<ParoquiaConfiguracaoRaw>('/configuracao-paroquia');
  return normalizarConfiguracao(response.data);
};

export const atualizarDpo = async (payload: AtualizarDpoPayload): Promise<ParoquiaConfiguracao> => {
  const response = await apiClient.put<ParoquiaConfiguracaoRaw>('/configuracao-paroquia/dpo', payload);
  return normalizarConfiguracao(response.data);
};

export const atualizarIdentidadeVisual = async (
  payload: AtualizarIdentidadeVisualPayload
): Promise<ParoquiaConfiguracao> => {
  const requestBody = {
    corPrimariaHex: payload.corPrimaria,
    corSecundariaHex: payload.corSecundaria,
  };
  const response = await apiClient.put<ParoquiaConfiguracaoRaw>('/configuracao-paroquia', requestBody);
  return normalizarConfiguracao(response.data);
};

export const atualizarLogoParoquia = async (
  variante: 'claro' | 'escuro',
  arquivo: File
): Promise<void> => {
  const formData = new FormData();
  formData.append('arquivo', arquivo);
  await apiClient.put(`/configuracao-paroquia/logo/${variante}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};
