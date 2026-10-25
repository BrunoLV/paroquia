import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ConselhosAtas from './ConselhosAtas';
import { QueryProvider } from '@/shared/presentation/context/QueryProvider';
import { NotificationProvider } from '@/shared/presentation/context/NotificationContext';
import * as atasApi from '@/contextos/conselho/infrastructure/http/atas';
import { Ata, AtaResumo } from '@/contextos/conselho/domain/ata';

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

  it('secretário vê "Nova ata"', async () => {
    usuario.roles = ['SECRETARIO_CONSELHO'];
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
});
