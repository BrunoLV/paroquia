import {vi} from "vitest";
import apiClient from '@/shared/infrastructure/http/client';
// @ts-ignore
import {createObservacao, deleteObservacao, getObservacoes, updateObservacao} from './observacoes';

vi.mock('@/shared/infrastructure/http/client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('Observacoes API Client', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('getObservacoes should call apiClient.get with correct path', async () => {
    const mockData = [
      { id: 'obs-1', eventoId: 'evt-1', usuarioId: 'usr-1', tipo: 'NOTA', conteudo: 'Teste', criadoEmUtc: '2026-06-13T14:00:00Z' }
    ];
    (apiClient.get as jest.Mock).mockResolvedValue({ data: mockData });

    const result = await getObservacoes('evt-1');

    expect(apiClient.get).toHaveBeenCalledWith('/eventos/evt-1/observacoes');
    expect(result).toEqual(mockData);
  });

  test('createObservacao should call apiClient.post with correct body', async () => {
    const mockResponse = { id: 'obs-1', eventoId: 'evt-1', usuarioId: 'usr-1', tipo: 'NOTA', conteudo: 'Nova nota', criadoEmUtc: '2026-06-13T14:00:00Z' };
    (apiClient.post as jest.Mock).mockResolvedValue({ data: mockResponse });

    const result = await createObservacao('evt-1', 'Nova nota');

    expect(apiClient.post).toHaveBeenCalledWith('/eventos/evt-1/observacoes', {
      tipo: 'NOTA',
      conteudo: 'Nova nota',
    });
    expect(result).toEqual(mockResponse);
  });

  test('updateObservacao should call apiClient.patch with correct body', async () => {
    const mockResponse = { id: 'obs-1', eventoId: 'evt-1', usuarioId: 'usr-1', tipo: 'NOTA', conteudo: 'Atualizada', criadoEmUtc: '2026-06-13T14:00:00Z' };
    (apiClient.patch as jest.Mock).mockResolvedValue({ data: mockResponse });

    const result = await updateObservacao('evt-1', 'obs-1', 'Atualizada');

    expect(apiClient.patch).toHaveBeenCalledWith('/eventos/evt-1/observacoes/obs-1', {
      conteudo: 'Atualizada',
    });
    expect(result).toEqual(mockResponse);
  });

  test('deleteObservacao should call apiClient.delete with correct path', async () => {
    (apiClient.delete as jest.Mock).mockResolvedValue({ status: 204 });

    await deleteObservacao('evt-1', 'obs-1');

    expect(apiClient.delete).toHaveBeenCalledWith('/eventos/evt-1/observacoes/obs-1');
  });
});
