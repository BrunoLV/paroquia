import apiClient from '@/shared/infrastructure/http/client';
import { UnidadeTerritorial } from '@/shared/domain/types/types';

export interface UnidadeTerritorialFormData {
  nome: string;
  tipo: 'MATRIZ' | 'COMUNIDADE';
  contato?: string | null;
  ativo: boolean;
  parentId?: string | null;

  // Canônicos
  padroeiro?: string | null;
  dataFestaPadroeira?: string | null;
  dataDedicacaoAltar?: string | null;
  possuiSacrario: boolean;
  possuiPiaBatismal: boolean;
  clerigoResponsavelId?: string | null;

  // Fiscais e Jurídicos
  cnpj?: string | null;
  inscricaoMunicipal?: string | null;
  inscricaoImobiliariaIptu?: string | null;
  processoImunidadeIptu?: string | null;

  // Registro de Imóveis (RGI)
  cartorioRgi?: string | null;
  matriculaImovel?: string | null;
  tipoAquisicao?: string | null;
  areaTerrenoM2?: number | null;
  areaConstruidaM2?: number | null;

  // Segurança e Concessionárias
  avcbNumero?: string | null;
  avcbValidade?: string | null;
  lotacaoMaximaBombeiros?: number | null;
  codigoConsumidorEnergia?: string | null;
  codigoConsumidorAgua?: string | null;

  // Contato e Endereço Estruturado (LGPD Criptografado no Backend)
  telefone?: string | null;
  email?: string | null;
  enderecoCep?: string | null;
  enderecoLogradouro?: string | null;
  enderecoNumero?: string | null;
  enderecoComplemento?: string | null;
  enderecoBairro?: string | null;
  enderecoCidade?: string | null;
  enderecoUf?: string | null;
}

/**
 * Busca todas as unidades territoriais da paróquia (Matriz e Comunidades).
 */
export const getUnidadesTerritoriais = async (): Promise<UnidadeTerritorial[]> => {
  const response = await apiClient.get<UnidadeTerritorial[]>('/unidades-territoriais');
  return response.data;
};

/**
 * Busca uma unidade territorial pelo ID.
 */
export const getUnidadeTerritorial = async (id: string): Promise<UnidadeTerritorial> => {
  const response = await apiClient.get<UnidadeTerritorial>(`/unidades-territoriais/${id}`);
  return response.data;
};

/**
 * Cria uma nova unidade territorial.
 */
export const createUnidadeTerritorial = async (data: UnidadeTerritorialFormData): Promise<UnidadeTerritorial> => {
  const response = await apiClient.post<UnidadeTerritorial>('/unidades-territoriais', data);
  return response.data;
};

/**
 * Atualiza uma unidade territorial existente.
 */
export const updateUnidadeTerritorial = async (id: string, data: UnidadeTerritorialFormData): Promise<UnidadeTerritorial> => {
  const response = await apiClient.put<UnidadeTerritorial>(`/unidades-territoriais/${id}`, data);
  return response.data;
};

/**
 * Remove uma unidade territorial.
 */
export const deleteUnidadeTerritorial = async (id: string): Promise<void> => {
  await apiClient.delete(`/unidades-territoriais/${id}`);
};
