import { useQuery } from '@tanstack/react-query';
import { fetchPainelMetricasConsolidado, fetchConcorrenciaInterpastoral } from '../api/metricas';
import { MetricasEstrategicasPainelResponse, ConcorrenciaInterpastoralResponse } from '../api/metricasTypes';

export const usePainelMetricasEstrategicas = () => {
  return useQuery<MetricasEstrategicasPainelResponse>({
    queryKey: ['metricas-estrategicas-painel'],
    queryFn: fetchPainelMetricasConsolidado,
    staleTime: 5 * 60 * 1000,
  });
};

export const useConcorrenciaInterpastoral = () => {
  return useQuery<ConcorrenciaInterpastoralResponse>({
    queryKey: ['concorrencia-interpastoral'],
    queryFn: fetchConcorrenciaInterpastoral,
    staleTime: 1 * 60 * 1000,
  });
};
