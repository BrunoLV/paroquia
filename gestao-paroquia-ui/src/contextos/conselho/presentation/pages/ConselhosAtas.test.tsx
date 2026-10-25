import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ConselhosAtas from './ConselhosAtas';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import * as atasApi from '@/contextos/conselho/infrastructure/http/atas';
import { Ata, AtaResumo, TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import { getOrganizacaoMembros, getOrganizacoes } from '@/contextos/organizacao/infrastructure/http/organizacoes';

const usuario = vi.hoisted(() => ({ roles: ['PAROCO'] as string[] }));

vi.mock('@/contextos/iam/presentation/auth/AuthContext', () => ({
  useAuth: () => ({
    user: { id: '1', username: 'u', name: 'Usuário', roles: usuario.roles, activeOrganizationId: 'org-1', organizations: [] },
    isAuthenticated: true,
  }),
}));
vi.mock('@/contextos/conselho/infrastructure/http/atas');
vi.mock('@/contextos/organizacao/infrastructure/http/organizacoes', () => ({
  getOrganizacoes: vi.fn().mockResolvedValue([]),
  getOrganizacaoMembros: vi.fn().mockResolvedValue([]),
}));

vi.mock('@/contextos/local/infrastructure/http/locais', () => ({
  getLocais: vi.fn().mockResolvedValue([
    { id: 'l1', nome: 'Salão paroquial', tipo: 'SALAO', capacidadeMaxima: 80, ativo: true },
    { id: 'l2', nome: 'Capela São José', tipo: 'CAPELA', capacidadeMaxima: 50, ativo: true },
    { id: 'l3', nome: 'Sala interditada', tipo: 'SALA', capacidadeMaxima: 10, ativo: false },
  ]),
}));

const tipoRegra = (codigo: TipoReuniaoInfo['codigo'], rotulo: string, subtipo: string, orgaos: string[]): TipoReuniaoInfo => ({
  codigo, rotulo, exigeHomologacao: true, aplicaQuorum: true, modoPresenca: 'NOMINAL_MEMBROS', permiteVotacao: true,
  exigeOrganizacaoAssociada: false, tiposConvocantes: ['CONSELHO'], subtiposConselho: [subtipo],
  papeisRedatores: ['SECRETARIO_CONSELHO'], organizacoesRedacao: orgaos, presidenciaNata: true,
});
const tipoCpp = tipoRegra('REUNIAO_CPP', 'Reunião do CPP', 'CPP', ['cpp-1']);
const tipoCaep = tipoRegra('REUNIAO_CAEP', 'Reunião do CAEP', 'CAEP', []);

const pendente: AtaResumo = {
  id: 'ata-1', tipo: 'REUNIAO_CPP', tipoRotulo: 'Reunião do Conselho Pastoral Paroquial (CPP)',
  titulo: 'Reforma do telhado da capela', dataReuniao: '2026-06-01', organizacaoConvocanteId: 'cpp-1',
  organizacaoConvocante: 'CPP Matriz', status: 'PENDENTE_HOMOLOGACAO', numero: null,
  acoes: ['LER', 'EXPORTAR', 'HOMOLOGAR', 'VETAR', 'VER_TRILHA'],
};

const homologada: AtaResumo = {
  ...pendente, id: 'ata-2', titulo: 'Calendário de festas', status: 'HOMOLOGADA', numero: '001/2026',
  acoes: ['LER', 'EXPORTAR', 'VER_TRILHA'],
};

const detalhe: Ata = {
  id: 'ata-1', tipo: 'REUNIAO_CPP', tipoRotulo: pendente.tipoRotulo, organizacaoConvocanteId: 'cpp-1',
  organizacaoConvocante: 'CPP Matriz', organizacaoAssociadaId: null, organizacaoAssociada: null,
  status: 'PENDENTE_HOMOLOGACAO', numero: null,
  conteudo: {
    titulo: 'Reforma do telhado da capela', dataReuniao: '2026-06-01', local: 'Salão paroquial',
    carater: 'ORDINARIA', modalidade: 'PRESENCIAL',
    presencas: [{ fielId: 'f1', nome: 'Maria da Silva', funcao: 'Secretária', situacao: 'PRESENTE' }],
    totalParticipantes: null, ataAnteriorId: null, aprovacaoAtaAnterior: 'NAO_APLICAVEL', ressalvasAtaAnterior: null,
    itens: [{
      ordem: 1, titulo: 'Orçamento', relato: 'Discutiu-se o valor.',
      deliberacoes: [{ ordem: 1, texto: 'Aprovar o orçamento', resultado: 'PARECER_FAVORAVEL', votacao: null, conflitos: [] }],
    }],
  },
  quorum: { membrosConvocados: 5, minimoNecessario: 3, presentes: 3, atingido: true },
  redator: 'secretario', homologador: null, motivoVeto: null, decididoEmUtc: null, ataRetificadaId: null,
  justificativaRetificacao: null, criadoEmUtc: '2026-06-01T10:00:00Z', acoes: pendente.acoes,
};

const renderizar = () => render(
  <QueryProvider>
    <NotificationProvider>
      <BrowserRouter>
        <ConselhosAtas />
      </BrowserRouter>
    </NotificationProvider>
  </QueryProvider>,
);

describe('ConselhosAtas', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    usuario.roles = ['PAROCO'];
    vi.mocked(atasApi.listarAtas).mockResolvedValue({ itens: [pendente, homologada], total: 2, pagina: 0, tamanho: 10 });
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([]);
    vi.mocked(atasApi.obterPresencaRedator).mockResolvedValue(null);
    vi.mocked(atasApi.listarClero).mockResolvedValue([]);
    vi.mocked(atasApi.obterAta).mockResolvedValue(detalhe);
    vi.mocked(atasApi.obterTrilha).mockResolvedValue([
      { id: 't1', acao: 'RASCUNHAR', ator: 'secretario', perfil: 'SECRETARIO_CONSELHO', ocorridoEmUtc: '2026-06-01T10:00:00Z' },
    ]);
    vi.mocked(atasApi.homologarAta).mockResolvedValue({ ...detalhe, status: 'HOMOLOGADA', numero: '001/2026' });
    vi.mocked(atasApi.vetarAta).mockResolvedValue({ ...detalhe, status: 'VETADA' });
    vi.mocked(atasApi.baixarAta).mockResolvedValue(undefined);
  });

  it('lista as atas com situação e número, e minuta quando ainda não tem número', async () => {
    renderizar();

    expect(await screen.findByText('Reforma do telhado da capela')).toBeInTheDocument();
    expect(screen.getByText('Minuta')).toBeInTheDocument();
    expect(screen.getByText('Ata nº 001/2026')).toBeInTheDocument();
    expect(screen.getByText('Pendente de homologação')).toBeInTheDocument();
    expect(screen.getByText('Homologada')).toBeInTheDocument();
  });

  it('o botão "Nova ata" é oferecido a redatores, não ao Pároco', async () => {
    renderizar();
    await screen.findByText('Reforma do telhado da capela');
    expect(screen.queryByRole('button', { name: /Nova ata/i })).not.toBeInTheDocument();
  });

  it('"Nova ata" só é oferecido a quem redige algum tipo, segundo o servidor', async () => {
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([tipoCpp, tipoCaep]);
    renderizar();

    expect(await screen.findByRole('button', { name: /Nova ata/i })).toBeInTheDocument();
  });

  it('o menu da linha oferece só o que o servidor autorizou e homologa pelo menu', async () => {
    const user = userEvent.setup();
    renderizar();
    const linha = (await screen.findByText('Reforma do telhado da capela')).closest('tr') as HTMLElement;

    await user.click(within(linha).getByRole('button', { name: 'Ações' }));
    expect(screen.queryByRole('menuitem', { name: /Editar rascunho/i })).not.toBeInTheDocument();
    await user.click(screen.getByRole('menuitem', { name: /Homologar/i }));

    await waitFor(() => expect(atasApi.homologarAta).toHaveBeenCalledWith('ata-1'));
  });

  it('veto exige motivo e envia o motivo informado', async () => {
    const user = userEvent.setup();
    renderizar();
    const linha = (await screen.findByText('Reforma do telhado da capela')).closest('tr') as HTMLElement;

    await user.click(within(linha).getByRole('button', { name: 'Ações' }));
    await user.click(screen.getByRole('menuitem', { name: /Vetar/i }));
    const dialogo = await screen.findByRole('dialog', { name: /Vetar ata/i });
    await user.click(within(dialogo).getByRole('button', { name: /^Vetar ata$/i }));
    expect(await within(dialogo).findByText('Este campo é obrigatório.')).toBeInTheDocument();
    expect(atasApi.vetarAta).not.toHaveBeenCalled();

    await user.type(within(dialogo).getByLabelText('Motivo do veto'), 'Falta o orçamento');
    await user.click(within(dialogo).getByRole('button', { name: /^Vetar ata$/i }));

    await waitFor(() => expect(atasApi.vetarAta).toHaveBeenCalledWith('ata-1', 'Falta o orçamento'));
  });

  it('o detalhe mostra presença, quórum e deliberações, e exporta no formato escolhido', async () => {
    const user = userEvent.setup();
    renderizar();

    await user.click(await screen.findByText('Reforma do telhado da capela'));
    const dialogo = await screen.findByRole('dialog');
    expect(await within(dialogo).findByText(/Maria da Silva \(Secretária\)/)).toBeInTheDocument();
    expect(within(dialogo).getByText(/Quórum atingido: 3 de 5 membros/)).toBeInTheDocument();
    expect(within(dialogo).getByText(/Aprovar o orçamento/)).toBeInTheDocument();
    expect(await within(dialogo).findByText(/RASCUNHAR · secretario/)).toBeInTheDocument();

    await user.click(within(dialogo).getByRole('button', { name: 'Exportar' }));
    await user.click(await screen.findByRole('menuitem', { name: /Word — texto de Livro de Atas/i }));

    await waitFor(() => expect(atasApi.baixarAta).toHaveBeenCalledWith('ata-1',
      { formato: 'DOCX', layout: 'NARRATIVA', conteudo: 'ATA_COMPLETA' }));
  });

  it('o livro de atas exporta a compilação com os filtros atuais', async () => {
    const user = userEvent.setup();
    vi.mocked(atasApi.baixarCompilacao).mockResolvedValue(undefined);
    renderizar();
    await screen.findByText('Reforma do telhado da capela');

    await user.click(screen.getByRole('button', { name: /Exportar livro de atas/i }));
    await user.click(await screen.findByRole('menuitem', { name: /PDF — somente as decisões/i }));

    await waitFor(() => expect(atasApi.baixarCompilacao).toHaveBeenCalledWith(
      expect.objectContaining({ organizacaoId: undefined }),
      { formato: 'PDF', layout: 'ESTRUTURADA', conteudo: 'SOMENTE_DECISOES' }));
  });

  it('local e função são escolhidos de listas, não digitados', async () => {
    const user = userEvent.setup();
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([tipoCpp]);
    renderizar();

    await user.click(await screen.findByRole('button', { name: /Nova ata/i }));
    const dialogo = await screen.findByRole('dialog');

    await user.click(within(dialogo).getByRole('combobox', { name: /Local/i }));
    expect(await screen.findByRole('option', { name: 'Salão paroquial' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Capela São José' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Sala interditada' })).not.toBeInTheDocument();
    await user.click(screen.getByRole('option', { name: 'Capela São José' }));

    await user.click(within(dialogo).getByRole('button', { name: /Adicionar pessoa/i }));
    await user.click(within(dialogo).getByRole('combobox', { name: /Função/i }));
    expect(await screen.findByRole('option', { name: 'Pároco' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Secretário(a)' })).toBeInTheDocument();
  });

  it('o formulário só oferece tipos e órgãos em que o usuário redige', async () => {
    const user = userEvent.setup();
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([tipoCpp, tipoCaep]);
    vi.mocked(getOrganizacoes).mockResolvedValue([
      { id: 'cpp-1', nome: 'CPP Matriz', tipo: 'CONSELHO', subtipoConselho: 'CPP', ativo: true },
      { id: 'cpp-2', nome: 'CPP Outra Comunidade', tipo: 'CONSELHO', subtipoConselho: 'CPP', ativo: true },
      { id: 'caep-1', nome: 'Conselho Econômico', tipo: 'CONSELHO', subtipoConselho: 'CAEP', ativo: true },
    ] as Awaited<ReturnType<typeof getOrganizacoes>>);
    renderizar();

    await user.click(await screen.findByRole('button', { name: /Nova ata/i }));
    const dialogo = await screen.findByRole('dialog');

    await user.click(within(dialogo).getByRole('combobox', { name: /Tipo de reunião/i }));
    expect(await screen.findByRole('option', { name: 'Reunião do CPP' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Reunião do CAEP' })).not.toBeInTheDocument();
    await user.click(screen.getByRole('option', { name: 'Reunião do CPP' }));

    await user.click(within(dialogo).getByRole('combobox', { name: /Órgão convocante/i }));
    expect(await screen.findByRole('option', { name: 'CPP Matriz' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'CPP Outra Comunidade' })).not.toBeInTheDocument();
  });

  it('já inclui o redator como presente e Secretário(a), e não deixa repetir função única nem pessoa', async () => {
    const user = userEvent.setup();
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([tipoCpp]);
    vi.mocked(atasApi.obterPresencaRedator).mockResolvedValue({ fielId: 'f1', funcao: 'Secretário(a)' });
    vi.mocked(getOrganizacoes).mockResolvedValue([
      { id: 'cpp-1', nome: 'CPP Matriz', tipo: 'CONSELHO', subtipoConselho: 'CPP', ativo: true },
    ] as Awaited<ReturnType<typeof getOrganizacoes>>);
    vi.mocked(getOrganizacaoMembros).mockResolvedValue([
      { fielId: 'f1', fielNome: 'Maria da Silva', ativo: true },
      { fielId: 'f2', fielNome: 'João Pereira', ativo: true },
    ] as Awaited<ReturnType<typeof getOrganizacaoMembros>>);
    renderizar();

    await user.click(await screen.findByRole('button', { name: /Nova ata/i }));
    const dialogo = await screen.findByRole('dialog');
    await user.click(within(dialogo).getByRole('combobox', { name: /Tipo de reunião/i }));
    await user.click(await screen.findByRole('option', { name: 'Reunião do CPP' }));
    await user.click(within(dialogo).getByRole('combobox', { name: /Órgão convocante/i }));
    await user.click(await screen.findByRole('option', { name: 'CPP Matriz' }));

    expect(await within(dialogo).findByDisplayValue('Maria da Silva')).toBeDisabled();
    expect(within(dialogo).getByDisplayValue('Secretário(a)')).toBeDisabled();
    expect(within(dialogo).getAllByRole('button', { name: 'Remover pessoa' })[0]).toBeDisabled();

    await user.click(within(dialogo).getByRole('button', { name: /Adicionar pessoa/i }));
    const funcoes = within(dialogo).getAllByRole('combobox', { name: /Função/i });
    await user.click(funcoes[1]);
    expect(await screen.findByRole('option', { name: 'Secretário(a)' })).toHaveAttribute('aria-disabled', 'true');
    expect(screen.getByRole('option', { name: 'Presidente' })).not.toHaveAttribute('aria-disabled', 'true');
    await user.keyboard('{Escape}');

    const pessoas = within(dialogo).getAllByRole('combobox', { name: /Pessoa/i });
    await user.click(pessoas[1]);
    expect(await screen.findByRole('option', { name: 'João Pereira' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Maria da Silva' })).not.toBeInTheDocument();
  });

  it('na reunião conjunta, só oferece o CAEP da mesma unidade do órgão convocante', async () => {
    const user = userEvent.setup();
    const conjunta = { ...tipoRegra('REUNIAO_CONJUNTA_CPP_CAEP', 'Reunião conjunta CPP e CAEP', 'CPP', ['cpp-1']),
      exigeOrganizacaoAssociada: true };
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([conjunta]);
    vi.mocked(getOrganizacoes).mockResolvedValue([
      { id: 'cpp-1', nome: 'CPP Matriz', tipo: 'CONSELHO', subtipoConselho: 'CPP', ativo: true, parentId: 'matriz' },
      { id: 'caep-1', nome: 'CAEP Matriz', tipo: 'CONSELHO', subtipoConselho: 'CAEP', ativo: true, parentId: 'matriz' },
      { id: 'caep-2', nome: 'CAEP Comunidade', tipo: 'CONSELHO', subtipoConselho: 'CAEP', ativo: true, parentId: 'comunidade' },
    ] as Awaited<ReturnType<typeof getOrganizacoes>>);
    renderizar();

    await user.click(await screen.findByRole('button', { name: /Nova ata/i }));
    const dialogo = await screen.findByRole('dialog');
    await user.click(within(dialogo).getByRole('combobox', { name: /Tipo de reunião/i }));
    await user.click(await screen.findByRole('option', { name: 'Reunião conjunta CPP e CAEP' }));
    await user.click(within(dialogo).getByRole('combobox', { name: /Órgão convocante/i }));
    await user.click(await screen.findByRole('option', { name: 'CPP Matriz' }));
    await user.click(within(dialogo).getByRole('combobox', { name: /Conselho de Assuntos Econômicos/i }));

    expect(await screen.findByRole('option', { name: 'CAEP Matriz' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'CAEP Comunidade' })).not.toBeInTheDocument();
  });

  it('já inclui o Pároco como Presidente nos conselhos, sem poder trocá-lo nem removê-lo', async () => {
    const user = userEvent.setup();
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([tipoCpp]);
    vi.mocked(atasApi.listarClero).mockResolvedValue([
      { id: 'c1', nome: 'Pe. Pedro da Silva', funcao: 'Pároco', paroco: true },
      { id: 'c2', nome: 'Pe. João Vigário', funcao: 'Vigário paroquial', paroco: false },
    ]);
    vi.mocked(getOrganizacoes).mockResolvedValue([
      { id: 'cpp-1', nome: 'CPP Matriz', tipo: 'CONSELHO', subtipoConselho: 'CPP', ativo: true },
    ] as Awaited<ReturnType<typeof getOrganizacoes>>);
    renderizar();

    await user.click(await screen.findByRole('button', { name: /Nova ata/i }));
    const dialogo = await screen.findByRole('dialog');
    await user.click(within(dialogo).getByRole('combobox', { name: /Tipo de reunião/i }));
    await user.click(await screen.findByRole('option', { name: 'Reunião do CPP' }));
    await user.click(within(dialogo).getByRole('combobox', { name: /Órgão convocante/i }));
    await user.click(await screen.findByRole('option', { name: 'CPP Matriz' }));

    expect(await within(dialogo).findByDisplayValue('Pe. Pedro da Silva')).toBeDisabled();
    expect(within(dialogo).getByDisplayValue('Presidente')).toBeDisabled();
    expect(within(dialogo).getByRole('button', { name: 'Remover pessoa' })).toBeDisabled();

    await user.click(within(dialogo).getByRole('button', { name: /Adicionar pessoa/i }));
    await user.click(within(dialogo).getAllByRole('combobox', { name: /Pessoa/i })[1]);
    expect(await screen.findByRole('option', { name: 'Pe. João Vigário' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Pe. Pedro da Silva' })).not.toBeInTheDocument();
  });

  it('o coordenador e o vice do conselho são membros e já vêm com a função que o vínculo sugere', async () => {
    const user = userEvent.setup();
    vi.mocked(atasApi.listarTiposReuniao).mockResolvedValue([tipoCpp]);
    vi.mocked(getOrganizacoes).mockResolvedValue([
      { id: 'cpp-1', nome: 'CPP Matriz', tipo: 'CONSELHO', subtipoConselho: 'CPP', ativo: true },
    ] as Awaited<ReturnType<typeof getOrganizacoes>>);
    vi.mocked(getOrganizacaoMembros).mockResolvedValue([
      { fielId: 'f1', fielNome: 'Maria da Silva', papel: 'SECRETARIO_CONSELHO', ativo: true },
      { fielId: 'f2', fielNome: 'José Coordenador', papel: 'COORDENADOR', ativo: true },
      { fielId: 'f3', fielNome: 'Ana Vice', papel: 'SECRETARIO_GRUPO', ativo: true },
      { fielId: 'f4', fielNome: 'Carlos Membro', papel: 'MEMBRO_CONSELHO_PASTORAL', ativo: true },
    ] as Awaited<ReturnType<typeof getOrganizacaoMembros>>);
    renderizar();

    await user.click(await screen.findByRole('button', { name: /Nova ata/i }));
    const dialogo = await screen.findByRole('dialog');
    await user.click(within(dialogo).getByRole('combobox', { name: /Tipo de reunião/i }));
    await user.click(await screen.findByRole('option', { name: 'Reunião do CPP' }));
    await user.click(within(dialogo).getByRole('combobox', { name: /Órgão convocante/i }));
    await user.click(await screen.findByRole('option', { name: 'CPP Matriz' }));
    await user.click(within(dialogo).getByRole('button', { name: /Incluir todos os membros/i }));

    expect(await within(dialogo).findByDisplayValue('Coordenador(a) do conselho')).toBeInTheDocument();
    expect(within(dialogo).getByDisplayValue('Vice-coordenador(a) do conselho')).toBeInTheDocument();
    expect(within(dialogo).getByDisplayValue('Secretário(a)')).toBeInTheDocument();
    expect(within(dialogo).getByDisplayValue('Carlos Membro')).toBeInTheDocument();
  });
});
