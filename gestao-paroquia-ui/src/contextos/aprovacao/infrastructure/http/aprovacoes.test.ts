import {vi} from "vitest";
import apiClient from '@/shared/infrastructure/http/client';
// @ts-ignore
import {decideAprovacao, getAprovacoes} from './aprovacoes';

vi.mock('@/shared/infrastructure/http/client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    patch: jest.fn(),
  },
}));

describe('Aprovacoes API Client', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('getAprovacoes should call apiClient.get with correct path and params', async () => {
    const mockData = { content: [], totalElements: 0 };
    (apiClient.get as jest.Mock).mockResolvedValue({ data: mockData });

    const result = await getAprovacoes('PENDENTE', 0, 10);

    expect(apiClient.get).toHaveBeenCalledWith('/aprovacoes', {
      params: {
        status: 'PENDENTE',
        page: 0,
        size: 10,
      },
    });
    expect(result).toEqual(mockData);
  });

  test('decideAprovacao should call apiClient.patch with correct path and request body', async () => {
    const mockResponse = { id: '1', status: 'APROVADA' };
    (apiClient.patch as jest.Mock).mockResolvedValue({ data: mockResponse });

    const result = await decideAprovacao('1', { status: 'APROVADA', observacao: 'Aprovado' });

    expect(apiClient.patch).toHaveBeenCalledWith('/aprovacoes/1', {
      status: 'APROVADA',
      observacao: 'Aprovado',
    });
    expect(result).toEqual(mockResponse);
  });
});
