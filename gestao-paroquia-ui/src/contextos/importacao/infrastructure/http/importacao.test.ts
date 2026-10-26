import { beforeEach, describe, expect, it, vi } from 'vitest';
import api from '@/shared/infrastructure/http/client';
import { confirmarImportacao, validarImportacao } from './importacao';

vi.mock('@/shared/infrastructure/http/client', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

const client = vi.mocked(api, true);

describe('adapter HTTP de importação em massa', () => {
  beforeEach(() => vi.clearAllMocks());

  it('valida o arquivo enviando multipart com o campo "file" e o tipo na query', async () => {
    const resultado = { totalLinhas: 1, totalValidos: 1, totalInvalidos: 0, itens: [] };
    client.post.mockResolvedValueOnce({ data: resultado });
    const arquivo = new File(['nome\nMaria'], 'fieis.csv', { type: 'text/csv' });

    await expect(validarImportacao('FIEIS', arquivo)).resolves.toEqual(resultado);

    const [url, corpo, config] = client.post.mock.calls[0]!;
    expect(url).toBe('/importacao/validar?tipo=FIEIS');
    expect((corpo as FormData).get('file')).toBe(arquivo);
    expect(config).toEqual({ headers: { 'Content-Type': 'multipart/form-data' } });
  });

  it('confirma a importação enviando somente os itens recebidos', async () => {
    const itens = [{ nome: 'Maria' }];
    client.post.mockResolvedValueOnce({ data: { registrosImportados: 1 } });

    await expect(confirmarImportacao('FIEIS', itens)).resolves.toEqual({ registrosImportados: 1 });

    expect(client.post).toHaveBeenCalledWith('/importacao/confirmar?tipo=FIEIS', itens);
  });
});
