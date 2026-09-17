import { vi } from 'vitest';
import apiClient from '@/shared/infrastructure/http/client';
import { getFieis, getFiel, createFiel, updateFiel } from './fieis';
import { Fiel, CreateFielPayload, UpdateFielPayload } from '@/shared/domain/types/types';

vi.mock('@/shared/infrastructure/http/client', () => ({
  __esModule: true,
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('Fieis API Client', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  const mockFiel: Fiel = {
    id: 'fiel-1',
    nome: 'João Maria',
    cpf: '12345678900',
    dataNascimento: '1985-05-15',
    email: 'joao@example.com',
    telefone: '(11) 98765-4321',
    endereco: 'Rua das Flores, 123',
    vocacao: 'LEIGO',
    ativo: true,
    batizado: true,
    primeiraComunhao: true,
    crismado: true,
    casadoIgrejaCatolica: false,
  };

  test('getFieis should fetch list of fiéis', async () => {
    (apiClient.get as any).mockResolvedValue({ data: [mockFiel] });

    const result = await getFieis();

    expect(apiClient.get).toHaveBeenCalledWith('/fieis');
    expect(result).toEqual([mockFiel]);
  });

  test('getFiel should fetch a single fiel by id', async () => {
    (apiClient.get as any).mockResolvedValue({ data: mockFiel });

    const result = await getFiel('fiel-1');

    expect(apiClient.get).toHaveBeenCalledWith('/fieis/fiel-1');
    expect(result).toEqual(mockFiel);
  });

  test('createFiel should post new fiel data', async () => {
    const payload: CreateFielPayload = {
      nome: 'João Maria',
      cpf: '12345678900',
      dataNascimento: '1985-05-15',
      email: 'joao@example.com',
      telefone: '(11) 98765-4321',
      endereco: 'Rua das Flores, 123',
      vocacao: 'LEIGO',
      batizado: true,
      primeiraComunhao: true,
      crismado: true,
      casadoIgrejaCatolica: false,
    };
    (apiClient.post as any).mockResolvedValue({ data: mockFiel });

    const result = await createFiel(payload);

    expect(apiClient.post).toHaveBeenCalledWith('/fieis', payload);
    expect(result).toEqual(mockFiel);
  });

  test('updateFiel should put/patch updated fiel data', async () => {
    const payload: UpdateFielPayload = {
      nome: 'João Maria Alterado',
    };
    (apiClient.put as any).mockResolvedValue({ data: { ...mockFiel, nome: 'João Maria Alterado' } });

    const result = await updateFiel('fiel-1', payload);

    expect(apiClient.put).toHaveBeenCalledWith('/fieis/fiel-1', payload);
    expect(result).toEqual({ ...mockFiel, nome: 'João Maria Alterado' });
  });
});
