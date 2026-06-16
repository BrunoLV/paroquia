import apiClient from './client';
import { getLocais, createLocal, updateLocal, deleteLocal } from './locais';
import { Local } from './types';

jest.mock('./client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('Locais API Client', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  const mockLocal: Local = {
    id: 'uuid-1',
    nome: 'Salão Principal',
    tipo: 'SALAO',
    endereco: 'Rua Principal, 100',
    capacidade: 150,
    caracteristicas: 'Ar condicionado',
    ativo: true,
  };

  test('getLocais should fetch locations', async () => {
    (apiClient.get as jest.Mock).mockResolvedValue({ data: [mockLocal] });

    const result = await getLocais();

    expect(apiClient.get).toHaveBeenCalledWith('/locais');
    expect(result).toEqual([mockLocal]);
  });

  test('createLocal should post a new location', async () => {
    const input = {
      nome: 'Salão Principal',
      tipo: 'SALAO',
      endereco: 'Rua Principal, 100',
      capacidade: 150,
      caracteristicas: 'Ar condicionado',
      ativo: true,
    };
    (apiClient.post as jest.Mock).mockResolvedValue({ data: mockLocal });

    const result = await createLocal(input);

    expect(apiClient.post).toHaveBeenCalledWith('/locais', input);
    expect(result).toEqual(mockLocal);
  });

  test('updateLocal should put the updated location', async () => {
    const input = {
      nome: 'Salão Principal',
      tipo: 'SALAO',
      endereco: 'Rua Principal, 100',
      capacidade: 150,
      caracteristicas: 'Ar condicionado',
      ativo: true,
    };
    (apiClient.put as jest.Mock).mockResolvedValue({ data: mockLocal });

    const result = await updateLocal('uuid-1', input);

    expect(apiClient.put).toHaveBeenCalledWith('/locais/uuid-1', input);
    expect(result).toEqual(mockLocal);
  });

  test('deleteLocal should delete the location', async () => {
    (apiClient.delete as jest.Mock).mockResolvedValue({});

    await deleteLocal('uuid-1');

    expect(apiClient.delete).toHaveBeenCalledWith('/locais/uuid-1');
  });
});
