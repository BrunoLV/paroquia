import { ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import apiClient from '@/shared/infrastructure/http/client';
import { useProjetoResumo, useProjetos } from './useProjetosQueries';

vi.mock('@/shared/infrastructure/http/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), patch: vi.fn() },
}));

const client = vi.mocked(apiClient, true);

const wrapper = ({ children }: { children: ReactNode }) => (
  <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
    {children}
  </QueryClientProvider>
);

describe('hooks de consulta de projetos', () => {
  beforeEach(() => vi.clearAllMocks());

  it('useProjetos devolve lista vazia quando o corpo vem sem dados', async () => {
    client.get.mockResolvedValueOnce({ data: undefined });

    const { result } = renderHook(() => useProjetos(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual([]);
    expect(client.get).toHaveBeenCalledWith('/projetos');
  });

  it('useProjetoResumo não consulta enquanto nenhum projeto está selecionado', () => {
    renderHook(() => useProjetoResumo(undefined), { wrapper });

    expect(client.get).not.toHaveBeenCalled();
  });
});
