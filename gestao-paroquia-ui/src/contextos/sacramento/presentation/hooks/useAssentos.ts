import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  aprovarVinculo,
  atualizarRascunho,
  averbarBatismo,
  cancelarAssento,
  desvincularParticipante,
  emitirCertidao,
  homologarAssento,
  homologarAverbacao,
  lavrarAssento,
  listarAssentos,
  listarAssentosVinculados,
  obterAssento,
  obterHistorico,
  rejeitarVinculo,
  retificarAssento,
  vincularParticipante,
} from '@/contextos/sacramento/infrastructure/http/sacramentos';
import {
  AverbacaoPayload,
  LavrarAssentoPayload,
  RetificacaoPayload,
  TeorPayload,
  TipoSacramento,
} from '@/contextos/sacramento/domain/sacramento';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';

/** Chaves de cache do contexto de sacramentos. */
export const sacramentoKeys = {
  todos: ['sacramentos'] as const,
  lista: (tipo: TipoSacramento) => ['sacramentos', 'lista', tipo] as const,
  detalhe: (id: string) => ['sacramentos', 'detalhe', id] as const,
  vinculados: (pessoaId: string) => ['sacramentos', 'vinculados', pessoaId] as const,
  historico: (id: string) => ['sacramentos', 'historico', id] as const,
};

export const useAssentos = (tipo: TipoSacramento) =>
  useQuery({ queryKey: sacramentoKeys.lista(tipo), queryFn: () => listarAssentos(tipo) });

/** Assentos vinculados a um fiel/clérigo; só consulta quando o perfil pode ler o livro sacramental. */
export const useAssentosVinculados = (pessoaId: string | null, habilitado: boolean) =>
  useQuery({
    queryKey: sacramentoKeys.vinculados(pessoaId ?? ''),
    queryFn: () => listarAssentosVinculados(pessoaId as string),
    enabled: Boolean(pessoaId) && habilitado,
  });

export const useAssento = (id: string | null) =>
  useQuery({ queryKey: sacramentoKeys.detalhe(id ?? ''), queryFn: () => obterAssento(id as string), enabled: Boolean(id) });

/** A trilha só é consultada pelo Pároco; `habilitado` evita requisições negadas para os demais perfis. */
export const useHistorico = (id: string | null, habilitado: boolean) =>
  useQuery({
    queryKey: sacramentoKeys.historico(id ?? ''),
    queryFn: () => obterHistorico(id as string),
    enabled: Boolean(id) && habilitado,
  });

export const useCertidao = () => useMutation({ mutationFn: (id: string) => emitirCertidao(id) });

/** Mutação que invalida o cache do contexto e informa sucesso ou erro ao operador. */
const useMutacaoSacramento = <TInput, TOutput>(
  fn: (input: TInput) => Promise<TOutput>,
  sucesso: string,
  erro: string,
) => {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useNotification();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => {
      showSuccess(sucesso);
      queryClient.invalidateQueries({ queryKey: sacramentoKeys.todos });
    },
    onError: (err) => showError(extractErrorMessage(err, erro)),
  });
};

/** Conjunto das mutações de escrituração sacramental. */
export const useAssentoMutations = () => {
  return {
    lavrar: useMutacaoSacramento((p: LavrarAssentoPayload) => lavrarAssento(p), 'Rascunho lavrado.', 'Erro ao lavrar o assento.'),
    atualizar: useMutacaoSacramento(
      ({ id, teor }: { id: string; teor: TeorPayload }) => atualizarRascunho(id, teor),
      'Rascunho atualizado.',
      'Erro ao atualizar o rascunho.',
    ),
    homologar: useMutacaoSacramento((id: string) => homologarAssento(id), 'Assento homologado.', 'Erro ao homologar o assento.'),
    vincular: useMutacaoSacramento(
      ({ id, participanteId, fielId, clerigoId }: { id: string; participanteId: string; fielId?: string; clerigoId?: string }) =>
        vincularParticipante(id, participanteId, { fielId, clerigoId }),
      'Vínculo registrado (pendente de aprovação do Pároco, se proposto pela Secretaria).',
      'Erro ao vincular o participante.',
    ),
    aprovarVinculo: useMutacaoSacramento(
      ({ id, participanteId }: { id: string; participanteId: string }) => aprovarVinculo(id, participanteId),
      'Vínculo aprovado.',
      'Erro ao aprovar o vínculo.',
    ),
    rejeitarVinculo: useMutacaoSacramento(
      ({ id, participanteId }: { id: string; participanteId: string }) => rejeitarVinculo(id, participanteId),
      'Vínculo rejeitado.',
      'Erro ao rejeitar o vínculo.',
    ),
    desvincular: useMutacaoSacramento(
      ({ id, participanteId }: { id: string; participanteId: string }) => desvincularParticipante(id, participanteId),
      'Vínculo removido.',
      'Erro ao desvincular o participante.',
    ),
    averbar: useMutacaoSacramento(
      ({ id, payload }: { id: string; payload: AverbacaoPayload }) => averbarBatismo(id, payload),
      'Averbação digitada; aguarda homologação do Pároco.',
      'Erro ao averbar.',
    ),
    homologarAverbacao: useMutacaoSacramento(
      ({ id, averbacaoId }: { id: string; averbacaoId: string }) => homologarAverbacao(id, averbacaoId),
      'Averbação homologada.',
      'Erro ao homologar a averbação.',
    ),
    retificar: useMutacaoSacramento(
      ({ id, payload }: { id: string; payload: RetificacaoPayload }) => retificarAssento(id, payload),
      'Ressalva registrada.',
      'Erro ao retificar o assento.',
    ),
    cancelar: useMutacaoSacramento(
      ({ id, motivo }: { id: string; motivo: string }) => cancelarAssento(id, motivo),
      'Assento cancelado.',
      'Erro ao cancelar o assento.',
    ),
  };
};
