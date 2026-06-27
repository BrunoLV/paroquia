import apiClient from './client';
import { getOrganizacoes, createOrganizacao, updateOrganizacao, deleteOrganizacao } from './organizacoes';
import { Organizacao } from './types';

jest.mock('./client', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('Organizacoes API Client', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  const mockOrg: Organizacao = {
    id: 'uuid-org-1',
    nome: 'Pastoral Familiar',
    tipo: 'PASTORAL',
    contato: 'familiar@test.com',
    coordenador: 'Coordenador A',
    viceCoordenador: 'Vice Coordenador B',
    ativo: true,
  };

  test('getOrganizacoes should fetch organizations', async () => {
    (apiClient.get as jest.Mock).mockResolvedValue({ data: [mockOrg] });

    const result = await getOrganizacoes();

    expect(apiClient.get).toHaveBeenCalledWith('/organizacoes');
    expect(result).toEqual([mockOrg]);
  });

  test('createOrganizacao should post a new organization', async () => {
    const input = {
      nome: 'Pastoral Familiar',
      tipo: 'PASTORAL',
      contato: 'familiar@test.com',
      coordenador: 'Coordenador A',
      viceCoordenador: 'Vice Coordenador B',
      ativo: true,
    };
    (apiClient.post as jest.Mock).mockResolvedValue({ data: mockOrg });

    const result = await createOrganizacao(input);

    expect(apiClient.post).toHaveBeenCalledWith('/organizacoes', input);
    expect(result).toEqual(mockOrg);
  });

  test('updateOrganizacao should put the updated organization', async () => {
    const input = {
      nome: 'Pastoral Familiar',
      tipo: 'PASTORAL',
      contato: 'familiar@test.com',
      coordenador: 'Coordenador A',
      viceCoordenador: 'Vice Coordenador B',
      ativo: true,
    };
    (apiClient.put as jest.Mock).mockResolvedValue({ data: mockOrg });

    const result = await updateOrganizacao('uuid-org-1', input);

    expect(apiClient.put).toHaveBeenCalledWith('/organizacoes/uuid-org-1', input);
    expect(result).toEqual(mockOrg);
  });

  test('deleteOrganizacao should delete the organization', async () => {
    (apiClient.delete as jest.Mock).mockResolvedValue({});

    await deleteOrganizacao('uuid-org-1');

    expect(apiClient.delete).toHaveBeenCalledWith('/organizacoes/uuid-org-1');
  });
});
