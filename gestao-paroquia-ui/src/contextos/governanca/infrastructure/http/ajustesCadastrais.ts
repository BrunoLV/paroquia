import apiClient from '@/shared/infrastructure/http/client';
export interface AjusteCadastral { id:string; fielId:string; status:string; email:string|null; celular:string|null; telefoneFixo:string|null; endereco:{logradouro?:string|null; cidade?:string|null; estado?:string|null}; criadaEmUtc:string; }
export const listarAjustesCadastrais = async ():Promise<AjusteCadastral[]> => (await apiClient.get<AjusteCadastral[]>('/ajustes-cadastrais')).data;
export const listarAjustesPendentesDoFiel = async (fielId:string):Promise<AjusteCadastral[]> => (await apiClient.get<AjusteCadastral[]>(`/fieis/${fielId}/ajustes-cadastrais`)).data;
export const decidirAjusteCadastral = async (id:string,aprovar:boolean,justificativa:string):Promise<void> => { await apiClient.post(`/ajustes-cadastrais/${id}/decidir`,{aprovar,justificativa}); };

export interface PreferenciasPrivacidadePayload {
  usoImagemAutorizado?: boolean;
  permitirDivulgacaoAniversario?: boolean;
}

export const exportarMeusDados = async (): Promise<any> => {
  const response = await apiClient.get('/meu-perfil/portabilidade');
  return response.data;
};

export const atualizarPreferenciasPrivacidade = async (payload: PreferenciasPrivacidadePayload): Promise<void> => {
  await apiClient.patch('/meu-perfil/preferencias-privacidade', payload);
};
