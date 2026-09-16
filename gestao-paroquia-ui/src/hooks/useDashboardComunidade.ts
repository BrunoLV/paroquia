import {useQuery} from '@tanstack/react-query';
import {getAniversariantes, getDashboardComunidade} from '@/api/dashboard';
import {Aniversariante, DashboardComunidade, PeriodoDashboard} from '@/api/types';

/**
 * Busca os aniversariantes do período (`GET /dashboard/aniversariantes` via `getAniversariantes`).
 *
 * Contrato de rede: query param `janela` (HOJE|SEMANA|MES). Exige autenticação (dado sensível).
 *
 * Query key `['dashboard', 'aniversariantes', janela]` — sob o prefixo `['dashboard']`.
 * `staleTime` de 30 min: a lista muda no máximo uma vez por dia e o cálculo no backend
 * descriptografa todos os fiéis, então não vale refazer a cada foco de janela.
 */
export const useAniversariantes = (janela: PeriodoDashboard) => {
  return useQuery<Aniversariante[]>({
    queryKey: ['dashboard', 'aniversariantes', janela],
    queryFn: () => getAniversariantes(janela),
    staleTime: 30 * 60 * 1000,
  });
};

/**
 * Busca a seção de vida comunitária (`GET /dashboard/comunidade` via `getDashboardComunidade`):
 * pastorais em atenção quanto à retenção.
 *
 * Contrato de rede: query param `grupoParoquialId` opcional; 403 quando o ator não pode ver a
 * comunidade pedida.
 *
 * Query key `['dashboard', 'comunidade', grupoParoquialId ?? null]`. `staleTime` de 5 min.
 */
export const useDashboardComunidade = (grupoParoquialId?: string) => {
  return useQuery<DashboardComunidade>({
    queryKey: ['dashboard', 'comunidade', grupoParoquialId ?? null],
    queryFn: () => getDashboardComunidade(grupoParoquialId),
    staleTime: 5 * 60 * 1000,
  });
};
