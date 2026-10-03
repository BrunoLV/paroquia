import apiClient from '@/shared/infrastructure/http/client';

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
}

export interface AtualizarIdentidadeVisualPayload {
  corPrimaria: string;
  corSecundaria: string;
}

export const obterConfiguracaoParoquia = async (): Promise<ParoquiaConfiguracao> => {
  const response = await apiClient.get<ParoquiaConfiguracao>('/configuracao-paroquia');
  return response.data;
};

export const atualizarIdentidadeVisual = async (
  payload: AtualizarIdentidadeVisualPayload
): Promise<ParoquiaConfiguracao> => {
  const response = await apiClient.put<ParoquiaConfiguracao>('/configuracao-paroquia', payload);
  return response.data;
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
