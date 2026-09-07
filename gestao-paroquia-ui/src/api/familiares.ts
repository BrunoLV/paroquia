import apiClient from '@/api/client';

export type TipoRelacionamentoFamiliar = 'CONJUGE' | 'GENITOR' | 'FILHO' | 'IRMAO';

export interface RelacionamentoFamiliar {
  id: string;
  fielId: string;
  nome: string;
  relacao: string;
}

export const listarFamiliares = async (fielId: string): Promise<RelacionamentoFamiliar[]> =>
  (await apiClient.get<RelacionamentoFamiliar[]>(`/fieis/${fielId}/familiares`)).data;

export const criarRelacionamentoFamiliar = async (
  fielId: string,
  fielDestinoId: string,
  tipo: TipoRelacionamentoFamiliar,
): Promise<string> => (await apiClient.post<string>(`/fieis/${fielId}/familiares`, { fielDestinoId, tipo })).data;

export const removerRelacionamentoFamiliar = async (fielId: string, relacaoId: string): Promise<void> => {
  await apiClient.delete(`/fieis/${fielId}/familiares/${relacaoId}`);
};
