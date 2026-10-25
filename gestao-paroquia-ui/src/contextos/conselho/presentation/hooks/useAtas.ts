import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  atualizarAta,
  baixarAta,
  baixarCompilacao,
  homologarAta,
  listarAtas,
  listarClero,
  listarTiposReuniao,
  obterPresencaRedator,
  obterAta,
  obterTrilha,
  rascunharAta,
  reabrirAta,
  retificarAta,
  submeterAta,
  vetarAta,
} from '@/contextos/conselho/infrastructure/http/atas';
import {
  ConteudoAtaPayload,
  FiltroAtas,
  OpcoesExportacao,
  RascunharAtaPayload,
  TipoReuniao,
} from '@/contextos/conselho/domain/ata';
import { getLocais } from '@/contextos/local/infrastructure/http/locais';
import { getOrganizacaoMembros } from '@/contextos/organizacao/infrastructure/http/organizacoes';
import { Candidato, funcaoSugerida, funcaoSugeridaDeOrganismo } from '@/contextos/conselho/presentation/components/ataFormTipos';
import { Organizacao } from '@/shared/domain/types/types';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';

/** Chaves de cache do contexto de conselho. */
export const atasKeys = {
  todas: ['atas'] as const,
  lista: (filtro: FiltroAtas) => ['atas', 'lista', filtro] as const,
  detalhe: (id: string) => ['atas', 'detalhe', id] as const,
  trilha: (id: string) => ['atas', 'trilha', id] as const,
  tipos: ['atas', 'tipos'] as const,
  clero: ['atas', 'clero'] as const,
  redator: (tipo: string, organizacaoId: string) => ['atas', 'redator', tipo, organizacaoId] as const,
  membros: (organizacaoId: string) => ['atas', 'membros', organizacaoId] as const,
};

export const useAtas = (filtro: FiltroAtas) =>
  useQuery({ queryKey: atasKeys.lista(filtro), queryFn: () => listarAtas(filtro), placeholderData: (anterior) => anterior });

export const useAta = (id: string | null) =>
  useQuery({ queryKey: atasKeys.detalhe(id ?? ''), queryFn: () => obterAta(id as string), enabled: Boolean(id) });

/** A trilha só é consultada por quem tem a ação `VER_TRILHA`; `habilitado` evita requisições negadas. */
export const useTrilha = (id: string | null, habilitado: boolean) =>
  useQuery({
    queryKey: atasKeys.trilha(id ?? ''),
    queryFn: () => obterTrilha(id as string),
    enabled: Boolean(id) && habilitado,
  });

/** Catálogo de tipos de reunião; muda raramente, então fica em cache por toda a sessão. */
export const useTiposReuniao = () =>
  useQuery({ queryKey: atasKeys.tipos, queryFn: listarTiposReuniao, staleTime: Infinity });

/** Presença do usuário como redator do tipo no órgão, para já incluí-lo na lista com a função certa. */
export const usePresencaRedator = (tipo: string, organizacaoId: string) =>
  useQuery({
    queryKey: atasKeys.redator(tipo, organizacaoId),
    queryFn: () => obterPresencaRedator(tipo as TipoReuniao, organizacaoId),
    enabled: Boolean(tipo && organizacaoId),
    staleTime: Infinity,
  });

/** Mutação que invalida o cache de atas e informa sucesso ou erro ao usuário. */
const useMutacaoAta = <TInput, TOutput>(fn: (input: TInput) => Promise<TOutput>, sucesso: string, erro: string) => {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useNotification();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => {
      showSuccess(sucesso);
      void queryClient.invalidateQueries({ queryKey: atasKeys.todas });
    },
    onError: (e) => showError(extractErrorMessage(e, erro)),
  });
};

export const useRascunharAta = () =>
  useMutacaoAta((payload: RascunharAtaPayload) => rascunharAta(payload), 'Rascunho da ata salvo.', 'Erro ao salvar a ata.');

export const useAtualizarAta = () =>
  useMutacaoAta(
    ({ id, conteudo }: { id: string; conteudo: ConteudoAtaPayload }) => atualizarAta(id, conteudo),
    'Rascunho da ata atualizado.',
    'Erro ao atualizar a ata.',
  );

export const useSubmeterAta = () =>
  useMutacaoAta((id: string) => submeterAta(id), 'Ata submetida.', 'Erro ao submeter a ata.');

export const useHomologarAta = () =>
  useMutacaoAta((id: string) => homologarAta(id), 'Ata homologada e numerada.', 'Erro ao homologar a ata.');

export const useVetarAta = () =>
  useMutacaoAta(
    ({ id, motivo }: { id: string; motivo: string }) => vetarAta(id, motivo),
    'Ata vetada e devolvida ao redator.',
    'Erro ao vetar a ata.',
  );

export const useReabrirAta = () =>
  useMutacaoAta((id: string) => reabrirAta(id), 'Ata reaberta para correção.', 'Erro ao reabrir a ata.');

export const useRetificarAta = () =>
  useMutacaoAta(
    ({ id, justificativa }: { id: string; justificativa: string }) => retificarAta(id, justificativa),
    'Ata retificadora aberta em rascunho.',
    'Erro ao abrir a ata retificadora.',
  );

/** Download não altera dados de tela, mas gera trilha no servidor; por isso só informa o erro. */
const useDownload = <TInput>(fn: (input: TInput) => Promise<void>) => {
  const { showError } = useNotification();
  return useMutation({ mutationFn: fn, onError: (e) => showError(extractErrorMessage(e, 'Erro ao exportar a ata.')) });
};

export const useExportarAta = () =>
  useDownload(({ id, opcoes }: { id: string; opcoes: OpcoesExportacao }) => baixarAta(id, opcoes));

export const useExportarCompilacao = () =>
  useDownload(({ filtro, opcoes }: { filtro: FiltroAtas; opcoes: OpcoesExportacao }) =>
    baixarCompilacao(filtro, opcoes));

/**
 * Pessoas que podem constar na presença ou declarar conflito: membros ativos do órgão convocante e, na reunião
 * conjunta, do associado, e o clero. Nas reuniões de presença livre, `organismoIds` acrescenta os membros dos
 * organismos filhos do conselho, identificados pelo nome da pastoral. Sem duplicatas (o órgão convocante prevalece);
 * membros do órgão, depois os dos organismos, depois o clero, cada grupo em ordem de nome.
 */
export const useCandidatosPresenca = (
  organizacaoIds: string[], organizacoes: Organizacao[] = [], organismoIds: string[] = [],
): Candidato[] => {
  const centrais = organizacaoIds.filter(Boolean);
  const ids = [...centrais, ...organismoIds];
  const resultados = useQueries({
    queries: ids.map((id) => ({ queryKey: atasKeys.membros(id), queryFn: () => getOrganizacaoMembros(id) })),
  });
  const porId = new Map<string, Candidato>();
  resultados.forEach((r, i) => (r.data ?? []).forEach((m) => {
    if (m.fielId && m.ativo && !porId.has(m.fielId)) {
      const org = organizacoes.find((o) => o.id === ids[i]);
      const organismo = i >= centrais.length;
      porId.set(m.fielId, {
        id: m.fielId,
        nome: m.fielNome ?? m.nomeCompleto ?? 'Pessoa sem nome',
        funcaoSugerida: organismo ? funcaoSugeridaDeOrganismo(m.papel) : funcaoSugerida(org?.tipo, m.papel),
        organismo: organismo ? org?.nome : undefined,
      });
    }
  }));
  const clero = useQuery({ queryKey: atasKeys.clero, queryFn: listarClero, staleTime: Infinity }).data ?? [];
  const ordenar = (a: Candidato, b: Candidato) => a.nome.localeCompare(b.nome, 'pt-BR');
  const doOrgao = Array.from(porId.values()).filter((c) => !c.organismo).sort(ordenar);
  const dosOrganismos = Array.from(porId.values()).filter((c) => c.organismo).sort(ordenar);
  return [
    ...doOrgao, ...dosOrganismos,
    ...clero.map((c) => ({ id: c.id, nome: c.nome, clerigo: true, paroco: c.paroco })).sort(ordenar),
  ];
};

/** Nomes dos locais ativos cadastrados, em ordem alfabética, para escolher onde a reunião aconteceu. */
export const useLocaisAtivos = (): string[] => {
  const { data = [] } = useQuery({ queryKey: ['locais'], queryFn: getLocais });
  return data.filter((l) => l.ativo).map((l) => l.nome).sort((a, b) => a.localeCompare(b, 'pt-BR'));
};
