import {useQuery} from '@tanstack/react-query';
import {getDashboardMetrics} from '@/api/dashboard';
import {DashboardMetrics, PeriodoDashboard} from '@/api/types';

/**
 * Busca as métricas consolidadas do Dashboard (`GET /dashboard/metrics` via `getDashboardMetrics`
 * em `api/dashboard.ts`) para um recorte temporal e uma comunidade.
 *
 * Contrato de rede: query params `periodo` (HOJE|SEMANA|MES) e `grupoParoquialId` opcional; o
 * backend valida o acesso à comunidade e responde 403 quando o ator não pode vê-la.
 *
 * Query key `['dashboard', 'metrics', periodo, grupoParoquialId ?? null]` — sob o prefixo
 * `['dashboard']`, que as mutations de evento/projeto/fiel invalidam por prefixo. A key inclui o
 * filtro para que cada combinação recorte×comunidade tenha seu próprio cache e a troca de filtro
 * não descarte o resultado anterior. Também é a fonte única usada pela tela de Relatórios (com
 * `periodo='MES'`), eliminando a antiga chave compartilhada `['dashboardMetrics']`.
 *
 * `staleTime` de 60s evita refazer as (várias) consultas de contagem a cada foco de janela sem
 * deixar os números defasados por muito tempo.
 */
export const useDashboardMetrics = (periodo: PeriodoDashboard, grupoParoquialId?: string) => {
  return useQuery<DashboardMetrics>({
    queryKey: ['dashboard', 'metrics', periodo, grupoParoquialId ?? null],
    queryFn: () => getDashboardMetrics({periodo, grupoParoquialId}),
    staleTime: 60 * 1000,
  });
};
