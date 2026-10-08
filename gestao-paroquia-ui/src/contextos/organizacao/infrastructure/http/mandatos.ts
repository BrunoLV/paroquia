import apiClient from '@/shared/infrastructure/http/client';
import { FuncaoCoordenacao, MandatoCoordenacao } from '@/shared/domain/types/types';

export interface PropostaMandato {
  fielId: string;
  funcao: FuncaoCoordenacao;
  inicio: string;
  fim: string;
}

/** Lista os mandatos de coordenação de uma organização, do mais recente ao mais antigo. */
export const getMandatos = async (organizacaoId: string): Promise<MandatoCoordenacao[]> => {
  const response = await apiClient.get<MandatoCoordenacao[]>(`/organizacoes/${organizacaoId}/mandatos`);
  return response.data;
};

/** Registra a proposta de um mandato, pendente de homologação do Pároco. */
export const proporMandato = async (
  organizacaoId: string,
  proposta: PropostaMandato,
): Promise<MandatoCoordenacao> => {
  const response = await apiClient.post<MandatoCoordenacao>(`/organizacoes/${organizacaoId}/mandatos`, proposta);
  return response.data;
};

/** Homologa a proposta: o fiel passa a exercer legitimamente a função. */
export const homologarMandato = async (organizacaoId: string, mandatoId: string): Promise<MandatoCoordenacao> => {
  const response = await apiClient.post<MandatoCoordenacao>(
    `/organizacoes/${organizacaoId}/mandatos/${mandatoId}/homologacao`,
  );
  return response.data;
};

/** Veta a proposta; o motivo é obrigatório. */
export const vetarMandato = async (
  organizacaoId: string,
  mandatoId: string,
  motivo: string,
): Promise<MandatoCoordenacao> => {
  const response = await apiClient.post<MandatoCoordenacao>(
    `/organizacoes/${organizacaoId}/mandatos/${mandatoId}/veto`,
    { motivo },
  );
  return response.data;
};

/** Encerra um mandato homologado, devolvendo o fiel ao papel de membro ativo. */
export const encerrarMandato = async (organizacaoId: string, mandatoId: string): Promise<MandatoCoordenacao> => {
  const response = await apiClient.post<MandatoCoordenacao>(
    `/organizacoes/${organizacaoId}/mandatos/${mandatoId}/encerramento`,
  );
  return response.data;
};
