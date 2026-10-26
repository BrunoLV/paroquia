import {vi} from "vitest";
import apiClient from '@/shared/infrastructure/http/client';
import {getDashboardMetrics} from './dashboard';

vi.mock('@/shared/infrastructure/http/client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
  },
}));

describe('Painel API Client', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('getDashboardMetrics should call apiClient.get with correct path', async () => {
    const mockData = {
      resumoGeral: {
        totalEventosPeriodo: 5, projetosAtivos: 2, solicitacoesPendentes: 1,
        totalFieisAtivos: 10, novosFieisMes: 1, fieisEngajadosPastoral: 6, fieisSemVinculoPastoral: 4,
      },
      eventosPorOrganizacao: [],
      projetosPorStatus: { planejamento: 0, emExecucao: 0, concluidos: 0, atrasados: 0 },
      projetosAtivosDetalhes: [],
      eventosOrfaos: [],
      eventosSemana: [],
    };
    (apiClient.get as jest.Mock).mockResolvedValue({ data: mockData });

    const result = await getDashboardMetrics({ periodo: 'SEMANA' });

    expect(apiClient.get).toHaveBeenCalledWith('/painel/metricas', {
      params: { periodo: 'SEMANA', organizacaoId: undefined },
    });
    expect(result).toEqual(mockData);
  });
});
