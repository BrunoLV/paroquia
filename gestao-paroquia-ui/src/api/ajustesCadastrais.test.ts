import { afterEach, describe, expect, test, vi } from 'vitest';
import apiClient from './client';
import { listarAjustesPendentesDoFiel } from './ajustesCadastrais';

vi.mock('./client', () => ({
  __esModule: true,
  default: { get: vi.fn() },
}));

describe('ajustesCadastrais API', () => {
  afterEach(() => vi.clearAllMocks());

  test('consulta somente os ajustes pendentes do fiel informado', async () => {
    const ajustes = [{ id: 'ajuste-1', fielId: 'fiel-1', status: 'PENDENTE' }];
    vi.mocked(apiClient.get).mockResolvedValue({ data: ajustes });

    await expect(listarAjustesPendentesDoFiel('fiel-1')).resolves.toEqual(ajustes);
    expect(apiClient.get).toHaveBeenCalledWith('/fieis/fiel-1/ajustes-cadastrais');
  });
});
