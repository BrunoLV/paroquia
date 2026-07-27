import { vi } from "vitest";
import apiClient from './client';
import { getOrganizacoes, createOrganizacao, updateOrganizacao, deleteOrganizacao, getOrganograma } from './organizacoes';
import { Organizacao, OrganogramaNode } from './types';

vi.mock('./client', () => ({
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

  test('getOrganograma should fetch organograma tree', async () => {
    const mockNode: OrganogramaNode = {
      id: 'root',
      label: 'Paróquia Teste',
      type: 'PAROQUIA',
      subtype: 'ROOT',
      responsavelNome: null,
      responsavelPapel: null,
      viceResponsavelNome: null,
      children: [],
    };
    (apiClient.get as jest.Mock).mockResolvedValue({ data: mockNode });

    const result = await getOrganograma();

    expect(apiClient.get).toHaveBeenCalledWith('/organizacoes/organograma');
    expect(result).toEqual(mockNode);
  });
});
