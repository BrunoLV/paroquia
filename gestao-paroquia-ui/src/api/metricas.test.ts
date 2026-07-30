import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchPainelMetricasConsolidado, fetchConcorrenciaInterpastoral } from './metricas';
import { apiClient } from './client';

vi.mock('./client', () => {
  const getMock = vi.fn();
  return {
    apiClient: {
      get: getMock,
    },
    default: {
      get: getMock,
    },
  };
});

describe('metricas API service', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve chamar GET /metricas/estrategicas/painel com sucesso', async () => {
    const mockPainel = {
      taxaProjetos: { avaliacaoExecutiva: 'SAUDAVEL' },
      slaDespacho: { statusSla: 'EXCELENTE' },
    };
    (apiClient.get as any).mockResolvedValueOnce({ data: mockPainel });

    const result = await fetchPainelMetricasConsolidado();
    expect(apiClient.get).toHaveBeenCalledWith('/metricas/estrategicas/painel');
    expect(result.taxaProjetos.avaliacaoExecutiva).toBe('SAUDAVEL');
  });

  it('deve chamar GET /metricas/estrategicas/concorrencia-interpastoral com sucesso', async () => {
    const mockConcorrencia = {
      totalConflitosDetectados: 0,
      nivelRiscoConcorrencia: 'BAIXO',
    };
    (apiClient.get as any).mockResolvedValueOnce({ data: mockConcorrencia });

    const result = await fetchConcorrenciaInterpastoral();
    expect(apiClient.get).toHaveBeenCalledWith('/metricas/estrategicas/concorrencia-interpastoral');
    expect(result.nivelRiscoConcorrencia).toBe('BAIXO');
  });
});
