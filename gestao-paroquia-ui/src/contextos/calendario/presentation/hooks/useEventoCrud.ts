import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import type { AxiosError } from 'axios';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import apiClient from '@/shared/infrastructure/http/client';
import { getRecorrencia } from '@/contextos/calendario/infrastructure/http/eventos';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import { Evento, ReservaConflitoDetalhe } from '@/shared/domain/types/types';
import { EscopoEdicao } from '@/contextos/calendario/presentation/components/DialogEscopoEdicao';

/**
 * Todo o fluxo de escrita de eventos do calendário: abrir/fechar os diálogos de
 * detalhes/criação/edição/cancelamento/escopo-de-recorrência/conflito-de-recurso, as mutações
 * TanStack Query (criar/editar/cancelar) e a checagem de permissão de edição.
 *
 * Eventos recorrentes (recorrenciaId presente) exigem que o usuário decida o escopo da edição
 * (`EditScopeDialog`) antes de abrir o formulário — ver {@link handleOpenEdit}. Um conflito de
 * recurso (HTTP 409 com `conflitos`) abre um diálogo dedicado em vez do alerta genérico — ver
 * {@link handleEventSubmit}.
 *
 * @param refetchCalendario chamado após criar/editar/cancelar com sucesso, para atualizar o grid.
 */
export const useEventoCrud = (refetchCalendario: () => void) => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  const [selectedEvent, setSelectedEvent] = useState<Evento | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  // Evento.recorrencia nunca vem preenchido pelo GET /eventos (só o recorrenciaId) — a regra
  // completa (frequência, dias, limite) só existe em GET /eventos/{id}/recorrencia.
  const selectedEventRecorrenciaQuery = useQuery({
    queryKey: ['evento-recorrencia', selectedEvent?.id],
    queryFn: () => getRecorrencia(selectedEvent!.id),
    enabled: isDetailsOpen && Boolean(selectedEvent?.id) && Boolean(selectedEvent?.recorrenciaId),
  });

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<Evento | null>(null);
  const [createInitialDate, setCreateInitialDate] = useState<Date | undefined>(undefined);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [isCancelOpen, setIsCancelOpen] = useState(false);
  const [cancellingEvent, setCancellingEvent] = useState<Evento | null>(null);
  const [isCancelling, setIsCancelling] = useState(false);

  const [isEditScopeOpen, setIsEditScopeOpen] = useState(false);
  const [selectedEditScope, setSelectedEditScope] = useState<EscopoEdicao | null>(null);

  const [conflitoRecursoModalOpen, setConflitoRecursoModalOpen] = useState(false);
  const [conflitoRecursoDetalhes, setConflitoRecursoDetalhes] = useState<ReservaConflitoDetalhe[]>([]);
  const [conflitoRecursoMensagem, setConflitoRecursoMensagem] = useState<string>('');
  const [pendingSubmitData, setPendingSubmitData] = useState<{
    requestData: Record<string, unknown>;
    recurrence: unknown;
  } | null>(null);

  // Permissions check (simple for now, matches backend logic)
  const canEditEvent = (event: Evento) => {
    if (!user) return false;
    if (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN')) return true;

    // Member can edit if they belong to the organization responsible
    return user.organizations.some((org) => org.id === event.grupoParoquialResponsavelId);
  };

  const handleOpenDetails = (event: Evento) => {
    setSelectedEvent(event);
    setIsDetailsOpen(true);
  };

  const handleOpenCancel = (event: Evento) => {
    setCancellingEvent(event);
    setIsCancelOpen(true);
  };

  const handleOpenCreate = (prefilledDate?: Date) => {
    setEditingEvent(null);
    setCreateInitialDate(prefilledDate);
    setIsCreateOpen(true);
  };

  const handleOpenEdit = (event: Evento) => {
    if (event.recorrenciaId) {
      setEditingEvent(event);
      setIsEditScopeOpen(true);
    } else {
      setEditingEvent(event);
      setIsDetailsOpen(false);
      setIsCreateOpen(true);
    }
  };

  // Escopo escolhido (ex.: ONLY_THIS vs. série) é guardado em selectedEditScope e só
  // é enviado ao backend quando o formulário é de fato submetido (ver
  // handleEventSubmit -> updateData.editScope), pois o backend precisa saber se a
  // alteração deve tocar apenas esta ocorrência ou toda a série recorrente.
  const handleScopeSelect = (scope: EscopoEdicao) => {
    setSelectedEditScope(scope);
    setIsEditScopeOpen(false);
    setIsDetailsOpen(false);
    setIsCreateOpen(true);
  };

  // Mutações de evento (criar/editar/cancelar) via TanStack Query — cada uma só faz a
  // chamada HTTP; o tratamento de sucesso/erro/estado de submissão continua nos
  // handlers abaixo, que já orquestravam fluxos com efeitos colaterais distintos
  // (diálogo de conflito 409, escopo de recorrência) difíceis de expressar só em onSuccess/onError.
  const updateEventoMutation = useMutation({
    mutationFn: ({ id, updateData }: { id: string; updateData: Record<string, unknown> }) =>
      apiClient.patch(`/eventos/${id}`, updateData),
  });

  const createEventoMutation = useMutation({
    mutationFn: async ({ requestData, recurrence }: { requestData: Record<string, unknown>; recurrence: unknown }) => {
      const idempotencyKey =
        typeof crypto !== 'undefined' && crypto.randomUUID
          ? crypto.randomUUID()
          : Math.random().toString(36).substring(2) + Date.now().toString(36);

      const res = await apiClient.post<{ id?: string; solicitacaoAprovacaoId?: string; status?: string }>('/eventos', requestData, {
        headers: { 'Idempotency-Key': idempotencyKey },
      });

      if (recurrence && res.data?.id) {
        await apiClient.put(`/eventos/${res.data.id}/recorrencia`, recurrence);
      }
      return res;
    },
  });

  const cancelEventoMutation = useMutation({
    mutationFn: ({ id, motivo }: { id: string; motivo: string }) =>
      apiClient.post<{ id?: string; status?: string; solicitacaoAprovacaoId?: string }>(`/eventos/${id}/cancelar`, { motivo }),
  });

  const handleEventSubmit = async (data: Record<string, unknown>) => {
    setIsSubmitting(true);
    try {
      if (editingEvent) {
        // UPDATE (PATCH)
        // escopoEdicao diz ao backend o alcance da edição: SOMENTE_ESTE altera apenas esta
        // ocorrência (destacando-a da série), enquanto um escopo de série propaga a
        // mudança para as demais ocorrências futuras da recorrência. Default
        // SOMENTE_ESTE cobre o caso de evento não-recorrente (selectedEditScope nunca é
        // definido nesse caminho, ver handleOpenEdit).
        const { recurrence, ...eventBaseData } = data;
        const updateData = {
          ...eventBaseData,
          ativoIds: Array.isArray(data.ativoIds) ? data.ativoIds : [],
          escopoEdicao: selectedEditScope || 'SOMENTE_ESTE',
        };
        const res = await updateEventoMutation.mutateAsync({ id: editingEvent.id, updateData });
        const resData = res.data as { status?: string; solicitacaoAprovacaoId?: string } | undefined;
        if (res.status === 202 || resData?.status === 'PENDENTE' || resData?.solicitacaoAprovacaoId) {
          showSuccess('Alterações encaminhadas para deliberação do Conselho/Pároco.');
        } else {
          showSuccess('Evento atualizado com sucesso!');
        }
        setSelectedEditScope(null);
      } else {
        // CREATE (POST)
        const { recurrence, ...eventBaseData } = data;
        const requestData = {
          ...eventBaseData,
          projetoId: data.projetoId || null,
          localId: data.localId || null,
          participantes: data.participantes || [],
          ativoIds: Array.isArray(data.ativoIds) ? data.ativoIds : [],
          regraRecorrencia: (data.isRecurring && (recurrence || data.recurrenceRule)) ? (recurrence || data.recurrenceRule) : null,
          confirmarComConflito: false,
        };

        // Verificação preventiva de conflito para dar feedback humanizado ao usuário antes de salvar
        if (!data.liberarConflitoAgenda) {
          try {
            const checkRes = await apiClient.post<ReservaConflitoDetalhe[]>('/eventos/verificar-conflitos', requestData);
            if (checkRes.data && checkRes.data.length > 0) {
              setConflitoRecursoDetalhes(checkRes.data);
              setConflitoRecursoMensagem(
                'Identificamos conflitos com eventos ou reservas já aprovados para este espaço ou horário. Você pode ajustar os horários ou submeter para deliberação do Conselho.'
              );
              setPendingSubmitData({ requestData, recurrence });
              setConflitoRecursoModalOpen(true);
              setIsSubmitting(false);
              return;
            }
          } catch {
            // Continua com a submissão direta se falhar o check prévio
          }
        }

        const res = await createEventoMutation.mutateAsync({ requestData, recurrence });
        if (res.status === 202 || res.data?.status === 'PENDENTE' || res.data?.solicitacaoAprovacaoId) {
          showSuccess('Solicitação de evento encaminhada para deliberação do Conselho/Pároco.');
        } else {
          showSuccess('Evento criado com sucesso!');
        }
      }

      setIsCreateOpen(false);
      refetchCalendario();
    } catch (err: unknown) {
      const axiosErr = err as AxiosError<{ conflitos?: ReservaConflitoDetalhe[]; message?: string }>;
      if (axiosErr.response?.status === 409) {
        const conflitos = axiosErr.response?.data?.conflitos || [];
        setConflitoRecursoDetalhes(conflitos);
        setConflitoRecursoMensagem(
          axiosErr.response?.data?.message ??
            'Conflito detectado: o espaço ou recurso selecionado já possui reservas ativas no mesmo intervalo.'
        );
        const { recurrence, ...eventBaseData } = data;
        setPendingSubmitData({
          requestData: {
            ...eventBaseData,
            projetoId: data.projetoId || null,
            localId: data.localId || null,
            participantes: data.participantes || [],
            ativoIds: Array.isArray(data.ativoIds) ? data.ativoIds : [],
            regraRecorrencia: (data.isRecurring && (recurrence || data.recurrenceRule)) ? (recurrence || data.recurrenceRule) : null,
          },
          recurrence,
        });
        setConflitoRecursoModalOpen(true);
      } else {
        showError(extractErrorMessage(err, 'Erro ao processar evento. Verifique as regras de negócio.'));
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmarDiscussaoConselho = async () => {
    if (!pendingSubmitData) {
      setConflitoRecursoModalOpen(false);
      return;
    }
    setIsSubmitting(true);
    try {
      const submitPayload = {
        ...pendingSubmitData.requestData,
        confirmarComConflito: true,
      };
      const res = await createEventoMutation.mutateAsync({
        requestData: submitPayload,
        recurrence: pendingSubmitData.recurrence,
      });
      if (res.status === 202 || res.data?.status === 'PENDENTE' || res.data?.solicitacaoAprovacaoId) {
        showSuccess('Evento submetido para deliberação do Conselho Paroquial com os conflitos apontados.');
      } else {
        showSuccess('Evento criado com sucesso!');
      }
      setConflitoRecursoModalOpen(false);
      setPendingSubmitData(null);
      setIsCreateOpen(false);
      refetchCalendario();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao submeter evento para o Conselho.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancelConfirm = async (reason: string) => {
    if (!cancellingEvent) return;

    setIsCancelling(true);
    try {
      const res = await cancelEventoMutation.mutateAsync({ id: cancellingEvent.id, motivo: reason });
      const resData = res.data as { status?: string; solicitacaoAprovacaoId?: string } | undefined;
      if (res.status === 202 || resData?.status === 'PENDENTE') {
        showSuccess('Solicitação de cancelamento enviada para aprovação do Conselho/Pároco.');
      } else {
        showSuccess('Evento cancelado com sucesso!');
      }
      setIsCancelOpen(false);
      setIsDetailsOpen(false);
      refetchCalendario();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao cancelar evento.'));
    } finally {
      setIsCancelling(false);
    }
  };

  return {
    selectedEvent,
    isDetailsOpen,
    setIsDetailsOpen,
    selectedEventRecorrenciaQuery,
    isCreateOpen,
    setIsCreateOpen,
    editingEvent,
    createInitialDate,
    isSubmitting,
    isCancelOpen,
    setIsCancelOpen,
    cancellingEvent,
    isCancelling,
    isEditScopeOpen,
    setIsEditScopeOpen,
    conflitoRecursoModalOpen,
    setConflitoRecursoModalOpen,
    conflitoRecursoDetalhes,
    conflitoRecursoMensagem,
    canEditEvent,
    handleOpenDetails,
    handleOpenCancel,
    handleOpenCreate,
    handleOpenEdit,
    handleScopeSelect,
    handleEventSubmit,
    handleConfirmarDiscussaoConselho,
    handleCancelConfirm,
  };
};
