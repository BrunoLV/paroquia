import apiClient from './client';
import {Organizacao, OrganogramaNode} from './types';
import {getUnidadesTerritoriais} from './unidadesTerritoriais';

export {getUnidadesTerritoriais};

/**
 * Busca todas as organizações da paróquia (Unidades Territoriais como Matriz, Comunidades,
 * Clero e Grupos Paroquiais como pastorais, movimentos, conselhos).
 */
export const getOrganizacoes = async (): Promise<Organizacao[]> => {
  const [unidades, grupos] = await Promise.all([
    getUnidadesTerritoriais().catch(() => []),
    apiClient.get<Organizacao[]>('/organizacoes').then((r) => r.data).catch(() => []),
  ]);
  return [...unidades, ...grupos];
};

/**
 * Cria uma nova organização dentro da hierarquia paroquial. `parentId`
 * posiciona o novo nó como filho de outra organização (ex.: uma pastoral
 * vinculada a uma Comunidade); omitir associa a organização diretamente à
 * raiz (Matriz). O tipo (`TipoOrganismoParoquial`) determina como o nó se
 * comporta no organograma e nas regras de autorização por escopo.
 */
export const createOrganizacao = async (organizacao: Omit<Organizacao, 'id'>): Promise<Organizacao> => {
  const response = await apiClient.post<Organizacao>('/organizacoes', organizacao);
  return response.data;
};

/**
 * Atualiza os dados cadastrais de uma organização existente (nome, contato,
 * coordenador/vice-coordenador, vínculo com pastoral oficial, setor pastoral
 * etc.). Não move a organização na hierarquia por si só além do que for
 * enviado em `parentId` — alterar esse campo reposiciona o nó no organograma.
 */
export const updateOrganizacao = async (
  id: string,
  organizacao: Omit<Organizacao, 'id'> & { id?: string }
): Promise<Organizacao> => {
  const response = await apiClient.put<Organizacao>(`/organizacoes/${id}`, organizacao);
  return response.data;
};

/**
 * Remove uma organização da hierarquia paroquial. Deve ser usada com cautela:
 * uma organização com vínculos de membros ativos ou organizações-filhas pode
 * ter a exclusão rejeitada pelo backend para preservar a integridade do
 * organograma e da autorização por escopo organizacional.
 */
export const deleteOrganizacao = async (id: string): Promise<void> => {
  await apiClient.delete(`/organizacoes/${id}`);
};

export interface OrganizacaoMembro {
  vinculoId: string;
  fielId?: string;
  fielNome?: string;
  usuarioId?: string;
  usuarioNome?: string;
  nomeCompleto?: string;
  papel: string;
  ativo: boolean;
  igrejaVinculoId?: string | null;
}

/**
 * Busca os membros vinculados a uma organização específica, com o papel
 * (`papel`) que cada um exerce nela e se o vínculo está ativo. É a visão
 * "pelo lado da organização"; o vínculo inverso (usuário -> organizações)
 * fica em `usuarios.ts` (`getVinculos`).
 */
export const getOrganizacaoMembros = async (id: string): Promise<OrganizacaoMembro[]> => {
  const response = await apiClient.get<OrganizacaoMembro[]>(`/organizacoes/${id}/membros`);
  return response.data;
};

/**
 * Busca a árvore completa do organograma da paróquia, já montada pelo
 * backend a partir da hierarquia de organizações (Matriz, Comunidades,
 * grupos, membros com papel de destaque), pronta para renderização no
 * componente de organograma sem processamento adicional de hierarquia no
 * cliente.
 */
export const getOrganograma = async (): Promise<OrganogramaNode> => {
  const response = await apiClient.get<OrganogramaNode>('/organizacoes/organograma');
  return response.data;
};
