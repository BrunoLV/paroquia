import {useQuery} from '@tanstack/react-query';
import {getDashboardLogistica} from '../api/dashboard';
import {DashboardLogistica, PeriodoDashboard} from '../api/types';

/**
 * Busca a seção de logística do Dashboard (`GET /dashboard/logistica` via `getDashboardLogistica`
 * em `api/dashboard.ts`) para um recorte temporal e uma comunidade.
 *
 * Contrato de rede: query params `periodo` (HOJE|SEMANA|MES) e `grupoParoquialId` opcional; o
 * backend responde 403 quando o ator não pode ver a comunidade pedida.
 *
 * Query key `['dashboard', 'logistica', periodo, grupoParoquialId ?? null]` — sob o mesmo prefixo
 * `['dashboard']` do restante do painel, então as mutations de evento/projeto o invalidam junto.
 * É uma consulta separada da de métricas porque a detecção de choque é O(n²) no backend e só vale
 * a pena pagar quando esta seção está visível.
 *
 * `staleTime` de 60s alinhado ao hook de métricas.
 */
export const useDashboardLogistica = (periodo: PeriodoDashboard, grupoParoquialId?: string) => {
  return useQuery<DashboardLogistica>({
    queryKey: ['dashboard', 'logistica', periodo, grupoParoquialId ?? null],
    queryFn: () => getDashboardLogistica({periodo, grupoParoquialId}),
    staleTime: 60 * 1000,
  });
};
