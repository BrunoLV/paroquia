import {vi} from "vitest";
import apiClient from './client';
// @ts-ignore
import {decideApproval, getApprovals} from './approvals';

vi.mock('./client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    patch: jest.fn(),
  },
}));

describe('Approvals API Client', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('getApprovals should call apiClient.get with correct path and params', async () => {
    const mockData = { content: [], totalElements: 0 };
    (apiClient.get as jest.Mock).mockResolvedValue({ data: mockData });

    const result = await getApprovals('PENDENTE', 0, 10);

    expect(apiClient.get).toHaveBeenCalledWith('/aprovacoes', {
      params: {
        status: 'PENDENTE',
        page: 0,
        size: 10,
      },
    });
    expect(result).toEqual(mockData);
  });

  test('decideApproval should call apiClient.patch with correct path and request body', async () => {
    const mockResponse = { id: '1', status: 'APROVADA' };
    (apiClient.patch as jest.Mock).mockResolvedValue({ data: mockResponse });

    const result = await decideApproval('1', { status: 'APROVADA', observacao: 'Aprovado' });

    expect(apiClient.patch).toHaveBeenCalledWith('/aprovacoes/1', {
      status: 'APROVADA',
      observacao: 'Aprovado',
    });
    expect(result).toEqual(mockResponse);
  });
});
