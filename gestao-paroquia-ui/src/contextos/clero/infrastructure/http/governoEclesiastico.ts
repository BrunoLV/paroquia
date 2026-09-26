import apiClient from '@/shared/infrastructure/http/client';
import { Diocese, GovernoEclesiastico } from '@/shared/domain/types/types';

export interface DioceseFormData {
  nome: string;
  bispoDiocesano: string;
  cnpjMitra?: string | null;
  cidade?: string | null;
  uf?: string | null;
  contato?: string | null;
  ativo: boolean;
}

export interface GovernoEclesiasticoFormData {
  dioceseId: string;
  parocoId?: string | null;
  dataPosseParoco?: string | null;
  decretoErecaoParoquia?: string | null;
  dataInstalacaoParoquia?: string | null;
  livroTomboNumero?: string | null;
}

/**
 * Consulta as configurações do Governo Eclesiástico da paróquia.
 */
export const getGovernoEclesiastico = async (): Promise<GovernoEclesiastico | null> => {
  try {
    const response = await apiClient.get<GovernoEclesiastico>('/governo-eclesiastico');
    return response.data;
  } catch (err: unknown) {
    if (typeof err === 'object' && err !== null && 'response' in err) {
      const axiosErr = err as { response?: { status?: number } };
      if (axiosErr.response?.status === 404) {
        return null;
      }
    }
    throw err;
  }
};

/**
 * Salva as configurações de Governo Eclesiástico.
 */
export const saveGovernoEclesiastico = async (
  data: GovernoEclesiasticoFormData
): Promise<GovernoEclesiastico> => {
  const response = await apiClient.post<GovernoEclesiastico>('/governo-eclesiastico', data);
  return response.data;
};

/**
 * Lista todas as dioceses.
 */
export const getDioceses = async (): Promise<Diocese[]> => {
  const response = await apiClient.get<Diocese[]>('/dioceses');
  return response.data;
};

/**
 * Obtém os dados de uma diocese pelo identificador.
 */
export const getDiocese = async (id: string): Promise<Diocese> => {
  const response = await apiClient.get<Diocese>(`/dioceses/${id}`);
  return response.data;
};

/**
 * Cadastra uma nova diocese.
 */
export const createDiocese = async (data: DioceseFormData): Promise<Diocese> => {
  const response = await apiClient.post<Diocese>('/dioceses', data);
  return response.data;
};

/**
 * Atualiza os dados de uma diocese.
 */
export const updateDiocese = async (id: string, data: DioceseFormData): Promise<Diocese> => {
  const response = await apiClient.put<Diocese>(`/dioceses/${id}`, data);
  return response.data;
};

/**
 * Exclui uma diocese.
 */
export const deleteDiocese = async (id: string): Promise<void> => {
  await apiClient.delete(`/dioceses/${id}`);
};
