import { useQuery } from '@tanstack/react-query';
import { listarProjetos, obterResumoProjeto } from '@/contextos/projeto/infrastructure/http/projetos';

/** Chaves TanStack Query do contexto projeto, centralizadas para invalidação consistente. */
export const projetoQueryKeys = {
  lista: ['projetos'] as const,
  resumo: (id?: string) => ['projeto-resumo', id] as const,
};

/** Lista de projetos da tela de Projetos. */
export const useProjetos = () => useQuery({ queryKey: projetoQueryKeys.lista, queryFn: listarProjetos });

/** Resumo do projeto selecionado; só consulta quando há projeto aberto no painel de detalhes. */
export const useProjetoResumo = (id?: string) =>
  useQuery({
    queryKey: projetoQueryKeys.resumo(id),
    queryFn: () => obterResumoProjeto(id!),
    enabled: Boolean(id),
  });
