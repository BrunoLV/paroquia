import { vi } from "vitest";
import apiClient from './client';
// @ts-ignore
import { getDashboardMetrics } from './dashboard';

vi.mock('./client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
  },
}));

describe('Dashboard API Client', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('getDashboardMetrics should call apiClient.get with correct path', async () => {
    const mockData = {
      resumoGeral: { totalEventosMes: 5, projetosAtivos: 2, solicitacoesPendentes: 1 },
      eventosPorOrganizacao: [],
      projetosAtivosDetalhes: [],
      eventosSemana: [],
    };
    (apiClient.get as jest.Mock).mockResolvedValue({ data: mockData });

    const result = await getDashboardMetrics();

    expect(apiClient.get).toHaveBeenCalledWith('/dashboard/metrics');
    expect(result).toEqual(mockData);
  });
});
