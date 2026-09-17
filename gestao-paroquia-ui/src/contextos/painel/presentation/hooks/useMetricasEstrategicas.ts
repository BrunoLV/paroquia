import { useQuery } from '@tanstack/react-query';
import { fetchPainelMetricasConsolidado, fetchConcorrenciaInterpastoral } from '@/contextos/painel/infrastructure/http/metricas';
import { MetricasEstrategicasPainelResponse, ConcorrenciaInterpastoralResponse } from '@/contextos/painel/infrastructure/http/metricasTypes';

/**
 * Busca o painel consolidado de métricas estratégicas (`GET` via `fetchPainelMetricasConsolidado`
 * em `api/metricas.ts`), que agrega em uma única resposta os seis indicadores executivos da
 * paróquia: sazonalidade litúrgica, taxa de conclusão de projetos, SLA de despacho pastoral,
 * coeficiente de impacto social, concorrência interpastoral e ociosidade patrimonial (ver
 * `MetricasEstrategicasPainelResponse` em `api/metricasTypes.ts`).
 *
 * Query key `['metricas-estrategicas-painel']` é isolada e não é invalidada por nenhuma
 * mutation do app — estas métricas são derivadas/analíticas (agregações históricas), não
 * refletem uma entidade editável, então não há fluxo de invalidação a manter em sincronia.
 * `staleTime` de 5 minutos evita recomputar o painel (potencialmente custoso no backend) a
 * cada troca de aba/foco de janela, já que os números não mudam em tempo real.
 */
export const usePainelMetricasEstrategicas = () => {
  return useQuery<MetricasEstrategicasPainelResponse>({
    queryKey: ['metricas-estrategicas-painel'],
    queryFn: fetchPainelMetricasConsolidado,
    staleTime: 5 * 60 * 1000,
  });
};

/**
 * Busca isoladamente o indicador de concorrência interpastoral (`fetchConcorrenciaInterpastoral`
 * em `api/metricas.ts`): lista de conflitos de agenda entre pastorais que disputam o mesmo
 * público-alvo na mesma data (ver `ConcorrenciaInterpastoralResponse`/`ConflitoItemDto` em
 * `api/metricasTypes.ts`). É separado do painel consolidado porque telas que só precisam do
 * alerta de conflitos (ex.: um badge no calendário) não precisam pagar o custo de buscar os
 * outros cinco indicadores do painel completo.
 *
 * Query key `['concorrencia-interpastoral']` própria, sem invalidação cruzada com outras
 * mutations — é um indicador analítico, não uma entidade mutável pelo usuário. `staleTime`
 * de 1 minuto é menor que o do painel consolidado porque conflitos de agenda podem surgir
 * logo após a criação/edição de um evento, exigindo dados mais frescos.
 */
export const useConcorrenciaInterpastoral = () => {
  return useQuery<ConcorrenciaInterpastoralResponse>({
    queryKey: ['concorrencia-interpastoral'],
    queryFn: fetchConcorrenciaInterpastoral,
    staleTime: 1 * 60 * 1000,
  });
};
