import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import UnidadesTerritoriais from './UnidadesTerritoriais';
import * as unidadesApi from '@/contextos/organizacao/infrastructure/http/unidadesTerritoriais';
import * as governoApi from '@/contextos/clero/infrastructure/http/governoEclesiastico';
import * as clerigosApi from '@/contextos/clero/infrastructure/http/clerigos';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';

let mockCurrentUser = {
  id: '1',
  name: 'Padre Pedro',
  roles: ['ROLE_GLOBAL_PAROCO', 'PAROCO'],
  organizations: [],
};

vi.mock('@/contextos/iam/presentation/auth/AuthContext', () => ({
  useAuth: () => ({
    user: mockCurrentUser,
    isAuthenticated: true,
  }),
}));

vi.mock('@/contextos/organizacao/infrastructure/http/unidadesTerritoriais');
vi.mock('@/contextos/clero/infrastructure/http/governoEclesiastico');
vi.mock('@/contextos/clero/infrastructure/http/clerigos');

const mockUnidadesData = [
  {
    id: 'u-1',
    nome: 'Igreja Matriz Nossa Senhora de Fátima',
    tipo: 'MATRIZ' as const,
    ativo: true,
    padroeiro: 'Nossa Senhora de Fátima',
    dataFestaPadroeira: '05-13',
    dataDedicacaoAltar: '1986-10-12',
    possuiSacrario: true,
    possuiPiaBatismal: true,
    cnpj: '61.378.766/0002-26',
    avcbNumero: '12345',
    avcbValidade: '2028-12-31',
    enderecoLogradouro: 'Praça da Matriz',
    enderecoNumero: '100',
    enderecoBairro: 'Centro',
    enderecoCidade: 'São Paulo',
    enderecoUf: 'SP',
  },
  {
    id: 'u-2',
    nome: 'Capela São José Operário',
    tipo: 'COMUNIDADE' as const,
    ativo: true,
    padroeiro: 'São José',
    possuiSacrario: true,
    possuiPiaBatismal: false,
    enderecoLogradouro: 'Av. São José',
    enderecoNumero: '50',
    enderecoBairro: 'Operário',
    enderecoCidade: 'Campinas',
    enderecoUf: 'SP',
  },
];

const mockGovernoData = {
  id: 'gov-1',
  dioceseId: 'dio-1',
  dioceseNome: 'Diocese de Santo Amaro',
  bispoDiocesano: 'Dom José Negri',
  parocoId: 'clerigo-1',
  parocoNome: 'Padre Pedro da Silva',
  dataPosseParoco: '2020-02-01',
  decretoErecaoParoquia: 'Decreto 10/1960',
  dataInstalacaoParoquia: '1960-05-13',
  livroTomboNumero: '01/1960',
};

const renderComponent = () =>
  render(
    <MemoryRouter>
      <QueryProvider>
        <NotificationProvider>
          <UnidadesTerritoriais />
        </NotificationProvider>
      </QueryProvider>
    </MemoryRouter>
  );

describe('UnidadesTerritoriais Page (Governo & Território)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockCurrentUser = {
      id: '1',
      name: 'Padre Pedro',
      roles: ['ROLE_GLOBAL_PAROCO', 'PAROCO'],
      organizations: [],
    };

    // @ts-expect-error fixture parcial: o teste só preenche os campos que exercita
    vi.mocked(unidadesApi.getUnidadesTerritoriais).mockResolvedValue(mockUnidadesData);
    vi.mocked(governoApi.getGovernoEclesiastico).mockResolvedValue(mockGovernoData);
    vi.mocked(governoApi.getDioceses).mockResolvedValue([]);
    vi.mocked(clerigosApi.listarClerigos).mockResolvedValue([]);
  });

  it('deve renderizar o título da página Governo & Território e dados de governo eclesiástico', async () => {
    renderComponent();

    expect(screen.getByText('Governo & Território')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Diocese de Santo Amaro')).toBeInTheDocument();
      expect(screen.getByText('Dom José Negri', { exact: false })).toBeInTheDocument();
      expect(screen.getByText('Padre Pedro da Silva')).toBeInTheDocument();
    });
  });

  it('deve listar as unidades territoriais na tabela com indicadores de status StatusIcon', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz Nossa Senhora de Fátima')).toBeInTheDocument();
      expect(screen.getByText('Capela São José Operário')).toBeInTheDocument();
      expect(screen.getByText('Igreja Matriz')).toBeInTheDocument();
      expect(screen.getByText('Comunidade')).toBeInTheDocument();
      expect(screen.getAllByText('Sacrário').length).toBeGreaterThan(0);
      expect(screen.getByText('Pia Batismal')).toBeInTheDocument();
      expect(screen.getByText('AVCB Regular')).toBeInTheDocument();
    });
  });

  it('deve filtrar as unidades territoriais via SearchField', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz Nossa Senhora de Fátima')).toBeInTheDocument();
      expect(screen.getByText('Capela São José Operário')).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText('Pesquisar por nome, padroeiro ou cidade...');
    fireEvent.change(searchInput, { target: { value: 'Campinas' } });

    await waitFor(() => {
      expect(screen.getByText('Capela São José Operário')).toBeInTheDocument();
      expect(screen.queryByText('Igreja Matriz Nossa Senhora de Fátima')).not.toBeInTheDocument();
    });
  });

  it('deve abrir modal de visualização detalhada ao clicar na linha da tabela', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz Nossa Senhora de Fátima')).toBeInTheDocument();
    });

    // Clica na linha da Matriz
    fireEvent.click(screen.getByText('Igreja Matriz Nossa Senhora de Fátima'));

    await waitFor(() => {
      expect(screen.getByText('Dimensão Canônica & Litúrgica')).toBeInTheDocument();
      expect(screen.getByText('Endereço & Contatos')).toBeInTheDocument();
      expect(screen.getByText('Identificação Fiscal & Registro de Imóveis (RGI)')).toBeInTheDocument();
      expect(screen.getByText('Segurança (Bombeiros) & Concessionárias de Serviços Públicos')).toBeInTheDocument();
      expect(screen.getByText('61.378.766/0002-26')).toBeInTheDocument();
    });

    // Fecha o modal
    fireEvent.click(screen.getByText('Fechar'));

    await waitFor(() => {
      expect(screen.queryByText('Dimensão Canônica & Litúrgica')).not.toBeInTheDocument();
    });
  });

  it('deve abrir modal de visualização detalhada através da opção no menu Ações', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz Nossa Senhora de Fátima')).toBeInTheDocument();
    });

    const acoesButtons = screen.getAllByRole('button', { name: /^Ações$/i });
    expect(acoesButtons.length).toBeGreaterThan(0);
    fireEvent.click(acoesButtons[0]);

    const viewItem = await screen.findByRole('menuitem', { name: /Visualizar detalhes/i });
    expect(viewItem).toBeInTheDocument();
    fireEvent.click(viewItem);

    await waitFor(() => {
      expect(screen.getByText('Dimensão Canônica & Litúrgica')).toBeInTheDocument();
      expect(screen.getByText('61.378.766/0002-26')).toBeInTheDocument();
    });
  });

  it('deve renderizar botão Ações com menu contendo opções de Visualizar, Editar e Excluir', async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz Nossa Senhora de Fátima')).toBeInTheDocument();
    });

    const acoesButtons = screen.getAllByRole('button', { name: /^Ações$/i });
    expect(acoesButtons.length).toBeGreaterThan(0);

    fireEvent.click(acoesButtons[0]);

    const viewItem = await screen.findByRole('menuitem', { name: /Visualizar detalhes/i });
    expect(viewItem).toBeInTheDocument();

    const editItem = await screen.findByRole('menuitem', { name: /Editar unidade/i });
    expect(editItem).toBeInTheDocument();

    const deleteItem = await screen.findByRole('menuitem', { name: /Excluir unidade/i });
    expect(deleteItem).toBeInTheDocument();
  });

  it('deve exibir o nome do pároco quando retornado via parocoNomeEclesiastico', async () => {
    vi.mocked(governoApi.getGovernoEclesiastico).mockResolvedValue({
      ...mockGovernoData,
      parocoNome: null,
      parocoNomeEclesiastico: 'Pe. Silvestre Estevão',
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Pe. Silvestre Estevão')).toBeInTheDocument();
    });
  });

  it('não deve exibir botões de criação se usuário não for ADMIN nem PÁROCO', async () => {
    mockCurrentUser = {
      id: '2',
      name: 'Leigo Coordenador',
      roles: ['ROLE_COORDENADOR'],
      organizations: [],
    };

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Igreja Matriz Nossa Senhora de Fátima')).toBeInTheDocument();
    });

    expect(screen.queryByText('Nova Unidade Territorial')).not.toBeInTheDocument();
    expect(screen.queryByText('Governo Eclesiástico')).not.toBeInTheDocument();
    expect(screen.queryByText('Nova Diocese')).not.toBeInTheDocument();
  });
});
