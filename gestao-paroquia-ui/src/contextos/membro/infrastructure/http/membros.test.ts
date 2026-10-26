import { beforeEach, describe, expect, it, vi } from 'vitest';
import apiClient from '@/shared/infrastructure/http/client';
import { getMembros, promoverACoordenador, toggleMembroStatus } from './membros';

vi.mock('@/shared/infrastructure/http/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), patch: vi.fn(), put: vi.fn() },
}));

const client = vi.mocked(apiClient, true);

describe('adapter HTTP de membros', () => {
  beforeEach(() => vi.clearAllMocks());

  it('lista membros repassando os filtros como query params', async () => {
    client.get.mockResolvedValueOnce({ data: { content: [{ id: 'm1' }] } });

    const resultado = await getMembros({ organizacaoId: 'org-1', ativo: true });

    expect(client.get).toHaveBeenCalledWith('/membros', { params: { organizacaoId: 'org-1', ativo: true } });
    expect(resultado.content).toEqual([{ id: 'm1' }]);
  });

  it('altera o status enviando o flag em query param e sem corpo', async () => {
    client.patch.mockResolvedValueOnce({ data: undefined });

    await toggleMembroStatus('m1', false);

    expect(client.patch).toHaveBeenCalledWith('/membros/m1/status', null, { params: { ativo: false } });
  });

  it('promove a coordenador e devolve o resultado do backend', async () => {
    const payload = { fielId: 'f1', organizacaoId: 'org-1', papel: 'COORDENADOR' };
    client.post.mockResolvedValueOnce({ data: { membroId: 'm1', usuarioId: 'u1', novoUsuarioCriado: true } });

    const resultado = await promoverACoordenador(payload);

    expect(client.post).toHaveBeenCalledWith('/membros/promover-coordenador', payload);
    expect(resultado.novoUsuarioCriado).toBe(true);
  });
});
