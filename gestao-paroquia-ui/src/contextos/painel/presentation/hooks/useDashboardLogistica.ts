import {useQuery} from '@tanstack/react-query';
import {getDashboardLogistica} from '@/contextos/painel/infrastructure/http/dashboard';
import {DashboardLogistica, PeriodoDashboard} from '@/shared/domain/types/types';

/**
 * Busca a seção de logística do Painel (`GET /dashboard/logistica` via `getDashboardLogistica`
 * em `api/dashboard.ts`) para um recorte temporal e uma comunidade.
 *
 * Contrato de rede: query params `periodo` (HOJE|SEMANA|MES) e `organizacaoId` opcional; o
 * backend responde 403 quando o ator não pode ver a comunidade pedida.
 *
 * Query key `['dashboard', 'logistica', periodo, organizacaoId ?? null]` — sob o mesmo prefixo
 * `['dashboard']` do restante do painel, então as mutations de evento/projeto o invalidam junto.
 * É uma consulta separada da de métricas porque a detecção de choque é O(n²) no backend e só vale
 * a pena pagar quando esta seção está visível.
 *
 * `staleTime` de 60s alinhado ao hook de métricas.
 */
export const useDashboardLogistica = (periodo: PeriodoDashboard, organizacaoId?: string) => {
  return useQuery<DashboardLogistica>({
    queryKey: ['dashboard', 'logistica', periodo, organizacaoId ?? null],
    queryFn: () => getDashboardLogistica({periodo, organizacaoId}),
    staleTime: 60 * 1000,
  });
};
