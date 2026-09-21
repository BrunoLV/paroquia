import { useEffect, useMemo } from 'react';
import { useForm, useWatch, type FieldErrors } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { isGlobalCreator, isSecretario } from '@/contextos/iam/presentation/auth/permissions';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { listarReservas } from '@/contextos/reserva/infrastructure/http/reservas';
import { getRecorrencia } from '@/contextos/calendario/infrastructure/http/eventos';
import { AnoParoquial, AtivoPatrimonial, Evento, Local, Organizacao, Projeto } from '@/shared/domain/types/types';
import { formatIsoToLocal, formatLocalDate, getRecurrenceDates } from '@/shared/utils/recurrenceUtils';
import { eventoFormSchema, EventoFormInput } from '@/contextos/calendario/presentation/schemas/eventoSchema';
import { calcularLimitesHorario, calcularLimitesMultiplosPeriodos } from '@/contextos/calendario/presentation/utils/horarioEventoUtils';

interface UseCadastroEventoFormParams {
  open: boolean;
  onSubmit: (data: Record<string, unknown>) => void;
  organizations: Organizacao[];
  locais: Local[];
  ativos: AtivoPatrimonial[];
  projetos: Projeto[];
  anosParoquiais: AnoParoquial[];
  initialDate?: Date;
  initialEvent?: Evento | null;
}

const defaultValues: EventoFormInput = {
  titulo: '',
  descricao: '',
  categoria: 'PASTORAL',
  status: 'CONFIRMADO',
  grupoParoquialResponsavelId: '',
  igrejaVinculoId: '',
  localId: '',
  localIds: [],
  projetoId: '',
  participantes: [],
  selectedAtivoIds: [],
  formato: 'PRESENCIAL',
  tipoHorario: 'PERSONALIZADO',
  periodos: [],
  eventDate: '',
  startDate: '',
  endDate: '',
  endTime: '10:00',
  duracaoHoras: 1,
  duracaoMinutos: 0,
  justification: '',
  isRecurring: false,
  liberarConflitoAgenda: false,
  recurrenceRule: { frequencia: 'SEMANAL', intervalo: 1, diasDaSemana: [] },
};

/**
 * Orquestra o formulário de cadastro/edição de evento: busca os dados
 * derivados que o backend não entrega prontos (ativos já alocados via
 * reservas, regra completa de recorrência), calcula as opções filtradas de
 * cada dropdown, valida o cruzamento evento×projeto e monta o payload final
 * de `onSubmit` — mantendo `CadastroEventoModal` como uma casca de JSX fina.
 */
export const useCadastroEventoForm = ({
  open,
  onSubmit,
  organizations,
  locais,
  ativos,
  projetos,
  anosParoquiais,
  initialDate,
  initialEvent,
}: UseCadastroEventoFormParams) => {
  const { user } = useAuth();
  const { showError } = useNotification();

  const form = useForm<EventoFormInput>({
    resolver: zodResolver(eventoFormSchema),
    defaultValues,
  });
  const { control, handleSubmit, reset, setValue } = form;

  const [startDate, orgId, igrejaVinculoId, localId, localIds, projetoId, isRecurring, recurrenceRule] = useWatch({
    control,
    name: ['startDate', 'grupoParoquialResponsavelId', 'igrejaVinculoId', 'localId', 'localIds', 'projetoId', 'isRecurring', 'recurrenceRule'],
  });
  const endDate = useWatch({ control, name: 'endDate' });

  const selectedYear = useMemo(() => {
    if (!startDate) return new Date().getFullYear();
    const d = new Date(startDate);
    return isNaN(d.getTime()) ? new Date().getFullYear() : d.getFullYear();
  }, [startDate]);

  const isClosedYear = useMemo(() => {
    const anoConfig = anosParoquiais.find((a) => a.ano === selectedYear);
    return anoConfig?.status === 'FECHADO';
  }, [anosParoquiais, selectedYear]);

  const isParoco = useMemo(() => {
    if (!user) return false;
    return user.roles?.some((r) => r === 'PAROCO' || r === 'ROLE_CLERO_PAROCO' || r === 'ADMIN' || r === 'ROLE_ADMIN') || false;
  }, [user]);

  // Inclui a igreja/organização/local/projeto já vinculados ao evento mesmo que tenham sido
  // desativados/concluídos depois — senão o Select some da lista de opções e aparenta vazio ao
  // editar, embora o valor esteja salvo corretamente (mesma causa do bug já corrigido em
  // ativos: opções filtradas por "ativo" escondendo o valor atual do registro).
  const churches = useMemo(() => {
    return organizations.filter((o) => (o.ativo || o.id === igrejaVinculoId) && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'));
  }, [organizations, igrejaVinculoId]);

  const grupoParoquialOptions = useMemo(() => {
    return organizations.filter((o) => (o.ativo || o.id === orgId) && o.tipo !== 'MATRIZ' && o.tipo !== 'COMUNIDADE' && o.tipo !== 'CLERO');
  }, [organizations, orgId]);

  const filteredOrganizations = useMemo(() => {
    if (!user) return [];
    if (isGlobalCreator(user)) return grupoParoquialOptions;
    const userOrgIds = Array.isArray(user.organizations) ? user.organizations.map((org: { id: string }) => org.id) : [];
    return grupoParoquialOptions.filter((org) => userOrgIds.includes(org.id) || org.id === orgId);
  }, [user, grupoParoquialOptions, orgId]);

  const selectedLocalIds = Array.isArray(localIds) ? localIds : [];
  const localOptions = useMemo(
    () => locais.filter((l) => l.ativo || l.id === localId || selectedLocalIds.includes(l.id)),
    [locais, localId, selectedLocalIds]
  );
  const projetoOptions = useMemo(() => projetos.filter((p) => p.status === 'ABERTO' || p.status === 'EM_ANDAMENTO' || p.id === projetoId), [projetos, projetoId]);
  const selectedProject = useMemo(() => (projetoId ? projetos.find((p) => p.id === projetoId) || null : null), [projetoId, projetos]);

  const selectedProjectInfo = useMemo(() => {
    if (!selectedProject) return '';
    const startStr = selectedProject.inicio ? new Date(selectedProject.inicio).toLocaleDateString('pt-BR') : '';
    const endStr = selectedProject.fim ? new Date(selectedProject.fim).toLocaleDateString('pt-BR') : '';
    if (startStr && endStr) return `${startStr} a ${endStr}`;
    if (startStr) return `A partir de ${startStr}`;
    if (endStr) return `Até ${endStr}`;
    return 'Sem limite de datas';
  }, [selectedProject]);

  const projectValidationError = useMemo(() => {
    if (!selectedProject) return null;
    const projStart = selectedProject.inicio ? new Date(selectedProject.inicio) : null;
    const projEnd = selectedProject.fim ? new Date(selectedProject.fim) : null;
    if (!projStart && !projEnd) return null;
    if (!startDate) return null;
    const eventStart = new Date(startDate);

    if (projStart && eventStart < projStart) {
      return `A data de início do evento (${eventStart.toLocaleDateString('pt-BR')}) é anterior ao início do projeto (${projStart.toLocaleDateString('pt-BR')}).`;
    }

    if (isRecurring) {
      const { dataLimite, numeroOcorrencias } = recurrenceRule;
      if (!dataLimite && !numeroOcorrencias) {
        return 'Para eventos vinculados a projetos, a repetição deve possuir um limite de data ou número de ocorrências.';
      }
      if (projEnd) {
        if (dataLimite) {
          const limitDate = new Date(dataLimite + 'T23:59:59');
          if (limitDate > projEnd) {
            return `O limite da repetição (${limitDate.toLocaleDateString('pt-BR')}) excede o fim do projeto (${projEnd.toLocaleDateString('pt-BR')}).`;
          }
        }
        if (numeroOcorrencias) {
          const paddingLimit = new Date(projEnd);
          paddingLimit.setDate(paddingLimit.getDate() + 10);
          const occurrences = getRecurrenceDates(eventStart, recurrenceRule, paddingLimit);
          if (occurrences.length > 0) {
            const lastOccurrence = occurrences[occurrences.length - 1];
            if (lastOccurrence > projEnd) {
              return `A última ocorrência (${lastOccurrence.toLocaleDateString('pt-BR')}) excede o fim do projeto (${projEnd.toLocaleDateString('pt-BR')}).`;
            }
          }
        }
      }
    } else {
      if (!endDate) return null;
      const eventEnd = new Date(endDate);
      if (projEnd && eventEnd > projEnd) {
        return `A data de término do evento (${eventEnd.toLocaleDateString('pt-BR')}) excede o término do projeto (${projEnd.toLocaleDateString('pt-BR')}).`;
      }
    }
    return null;
  }, [selectedProject, startDate, endDate, isRecurring, recurrenceRule]);

  // `Evento.ativoIds` nunca é populado pelo backend (EventoResponse não carrega esse campo —
  // é write-only, usado só na criação/edição para gerar Reservas). A alocação real vive na
  // tabela reservas, então ao editar buscamos as reservas ATIVO ainda ocupando agenda para
  // pré-selecionar os ativos já vinculados; sem isso o campo abria vazio e salvar cancelaria
  // silenciosamente as reservas existentes.
  const ativosAlocadosQuery = useQuery({
    queryKey: ['reservas', 'evento-ativos', initialEvent?.id],
    queryFn: () => listarReservas({ eventoId: initialEvent!.id, tipoItem: 'ATIVO' }),
    enabled: open && Boolean(initialEvent?.id),
  });

  // `Evento.recorrencia` também nunca vinha preenchido pelo GET /eventos (só o recorrenciaId);
  // a regra completa (frequência, dias, limite) só existe em GET /eventos/{id}/recorrencia.
  const recorrenciaQuery = useQuery({
    queryKey: ['evento-recorrencia', initialEvent?.id],
    queryFn: () => getRecorrencia(initialEvent!.id),
    enabled: open && Boolean(initialEvent?.id) && Boolean(initialEvent?.recorrenciaId),
  });

  // Reset form ou define valores iniciais quando o modal abre ou initialEvent muda.
  useEffect(() => {
    if (!open) return;
    if (initialEvent) {
      const end = initialEvent.fim ? new Date(initialEvent.fim) : null;
      const startStr = formatIsoToLocal(initialEvent.inicio);
      const endStr = formatIsoToLocal(initialEvent.fim);
      const eventDateStr = startStr ? startStr.split('T')[0] : '';
      const tipoHorario = initialEvent.tipoHorario || 'PERSONALIZADO';

      let diffHoras = 1;
      let diffMinutos = 0;
      if (initialEvent.inicio && initialEvent.fim) {
        const diffMs = Math.max(0, new Date(initialEvent.fim).getTime() - new Date(initialEvent.inicio).getTime());
        const totalMinutos = Math.round(diffMs / 60000);
        diffHoras = Math.floor(totalMinutos / 60);
        diffMinutos = totalMinutos % 60;
      }

      const periodosIniciais = initialEvent.periodos && initialEvent.periodos.length > 0
        ? initialEvent.periodos
        : (['MADRUGADA', 'MANHA', 'TARDE', 'NOITE'].includes(tipoHorario) ? [tipoHorario] : []);

      reset({
        ...defaultValues,
        titulo: initialEvent.titulo || '',
        descricao: initialEvent.descricao || '',
        categoria: initialEvent.categoria || 'PASTORAL',
        grupoParoquialResponsavelId: initialEvent.grupoParoquialResponsavelId || '',
        igrejaVinculoId: initialEvent.igrejaVinculoId || '',
        localId: initialEvent.localId || (initialEvent.localIds && initialEvent.localIds.length > 0 ? initialEvent.localIds[0] : ''),
        localIds: initialEvent.localIds && initialEvent.localIds.length > 0 ? initialEvent.localIds : (initialEvent.localId ? [initialEvent.localId] : []),
        projetoId: initialEvent.projetoId || '',
        formato: initialEvent.formato || 'PRESENCIAL',
        tipoHorario,
        periodos: periodosIniciais,
        eventDate: eventDateStr,
        startDate: startStr,
        endDate: endStr,
        duracaoHoras: diffHoras,
        duracaoMinutos: diffMinutos,
        // ativoIds real vem de ativosAlocadosQuery (efeito abaixo) — o backend não devolve
        // esse campo em Evento, só aceita no payload de create/update.
        endTime: end ? `${String(end.getHours()).padStart(2, '0')}:${String(end.getMinutes()).padStart(2, '0')}` : '10:00',
        status: initialEvent.status || 'CONFIRMADO',
        justification: initialEvent.adicionadoExtraJustificativa || '',
        participantes: initialEvent.participantes || [],
        isRecurring: !!initialEvent.recorrenciaId,
        // regra completa vem de recorrenciaQuery (efeito abaixo) — initialEvent.recorrencia
        // nunca é preenchido pelo GET /eventos, só o recorrenciaId.
      });
    } else {
      const start = initialDate ? new Date(initialDate) : new Date();
      start.setHours(9, 0, 0, 0);
      const end = new Date(start);
      end.setHours(10, 0, 0, 0);
      const startLocal = formatLocalDate(start);
      const endLocal = formatLocalDate(end);
      const eventDateLocal = startLocal ? startLocal.split('T')[0] : '';
      reset({
        ...defaultValues,
        tipoHorario: 'PERSONALIZADO',
        eventDate: eventDateLocal,
        startDate: startLocal,
        endDate: endLocal,
        duracaoHoras: 1,
        duracaoMinutos: 0,
      });
    }
    // Não inclui filteredOrganizations/churches nas deps: esses arrays são recriados a
    // cada render do pai (Calendario.tsx faz `.filter()` inline, sem memo), e reagir a eles
    // aqui reexecutaria o reset acima a cada re-render — apagando o formulário, por
    // exemplo, assim que um erro de validação disparasse um setState no componente pai.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, initialDate, initialEvent, reset]);

  // Seleciona organização/igreja/local padrão só quando a lista chega e o campo ainda está
  // vazio; roda isolado do reset acima para não ser afetado pela recriação de array.
  useEffect(() => {
    if (!open || initialEvent) return;
    if (filteredOrganizations.length > 0 && !orgId) setValue('grupoParoquialResponsavelId', filteredOrganizations[0].id);
    if (churches.length > 0 && !igrejaVinculoId) setValue('igrejaVinculoId', churches[0].id);
    if (localOptions.length > 0 && (!localIds || localIds.length === 0) && !localId) {
      setValue('localId', localOptions[0].id);
      setValue('localIds', [localOptions[0].id]);
    }
  }, [open, initialEvent, filteredOrganizations, churches, localOptions, orgId, igrejaVinculoId, localId, localIds, setValue]);

  // Hidrata os ativos já alocados ao editar, a partir das reservas ainda ocupando agenda
  // (PENDENTE/CONFIRMADA/RETIRADO) — ver comentário de ativosAlocadosQuery acima.
  useEffect(() => {
    if (!open) return;
    if (!initialEvent) {
      setValue('selectedAtivoIds', []);
      return;
    }
    if (!ativosAlocadosQuery.data) return;
    const idsAtivos = ativosAlocadosQuery.data.filter((r) => ['PENDENTE', 'CONFIRMADA', 'RETIRADO'].includes(r.status)).map((r) => r.itemId);
    setValue('selectedAtivoIds', idsAtivos);
  }, [open, initialEvent, ativosAlocadosQuery.data, setValue]);

  // Hidrata a regra de recorrência ao editar um evento gerado por série — ver comentário de
  // recorrenciaQuery acima.
  useEffect(() => {
    if (!open || !initialEvent || !recorrenciaQuery.data) return;
    setValue('recurrenceRule', recorrenciaQuery.data);
  }, [open, initialEvent, recorrenciaQuery.data, setValue]);

  // Prioridade de exibição de erro estrutural (um toast por vez, igual ao comportamento
  // original de validação sequencial em cascata).
  const onInvalid = (errors: FieldErrors<EventoFormInput>) => {
    const priority: (keyof EventoFormInput)[] = [
      'titulo',
      'grupoParoquialResponsavelId',
      'eventDate',
      'startDate',
      'endTime',
      'endDate',
      'justification',
    ];
    for (const key of priority) {
      const message = errors[key]?.message;
      if (message) {
        showError(message as string);
        return;
      }
    }
  };

  const onValid = (data: EventoFormInput) => {
    if (churches.length > 0 && !data.igrejaVinculoId) {
      showError('A comunidade/igreja de vínculo é obrigatória.');
      return;
    }
    if (projectValidationError) {
      showError(projectValidationError);
      return;
    }
    if (isClosedYear && !data.justification.trim()) {
      showError(`A justificativa é obrigatória pois o Ano Paroquial de ${selectedYear} está FECHADO.`);
      return;
    }
    if (isSecretario(user) && !data.justification.trim()) {
      showError('A justificativa é obrigatória para operações submetidas pelo Secretário.');
      return;
    }

    let inicioIso = '';
    let fimIso = '';
    const tipoHorario = data.tipoHorario || 'PERSONALIZADO';
    const periodosValidos = (data.periodos || []).filter(
      (p) => p && p !== 'PERSONALIZADO' && p !== 'DIA_INTEIRO'
    );

    if (tipoHorario === 'PERSONALIZADO') {
      let finalEndDate = data.endDate;
      if (data.isRecurring && data.startDate) {
        const datePart = data.startDate.split('T')[0];
        finalEndDate = `${datePart}T${data.endTime}`;
      }
      inicioIso = data.startDate ? new Date(data.startDate).toISOString() : '';
      fimIso = finalEndDate ? new Date(finalEndDate).toISOString() : '';
    } else if (tipoHorario === 'DIA_INTEIRO') {
      const dateBase = data.eventDate || (data.startDate ? data.startDate.split('T')[0] : '');
      const limites = calcularLimitesHorario('DIA_INTEIRO', dateBase);
      inicioIso = limites.inicioIso;
      fimIso = limites.fimIso;
    } else {
      const dateBase = data.eventDate || (data.startDate ? data.startDate.split('T')[0] : '');
      const listaPeriodos = periodosValidos.length > 0 ? periodosValidos : [tipoHorario];
      const limites = calcularLimitesMultiplosPeriodos(listaPeriodos, dateBase);
      inicioIso = limites.inicioIso;
      fimIso = limites.fimIso;
    }

    const finalLocalIds = Array.isArray(data.localIds) && data.localIds.length > 0
      ? data.localIds
      : (data.localId ? [data.localId] : []);
    const primaryLocalId = finalLocalIds.length > 0 ? finalLocalIds[0] : null;

    onSubmit({
      titulo: data.titulo,
      descricao: data.descricao,
      categoria: data.categoria,
      formato: data.formato,
      grupoParoquialResponsavelId: data.grupoParoquialResponsavelId,
      igrejaVinculoId: data.igrejaVinculoId || null,
      localId: primaryLocalId,
      localIds: finalLocalIds,
      projetoId: data.projetoId || null,
      participantes: data.participantes,
      ativoIds: data.selectedAtivoIds,
      tipoHorario: periodosValidos.length > 0 ? periodosValidos[0] : tipoHorario,
      periodos: periodosValidos,
      inicio: inicioIso,
      fim: fimIso,
      status: isClosedYear && data.status !== 'RASCUNHO' ? 'ADICIONADO_EXTRA' : data.status,
      adicionadoExtraJustificativa: data.justification || null,
      recurrence: data.isRecurring ? data.recurrenceRule : null,
      liberarConflitoAgenda: isParoco ? data.liberarConflitoAgenda : false,
    });
  };

  const isSecretarioUser = useMemo(() => isSecretario(user), [user]);

  return {
    form,
    handleFormSubmit: handleSubmit(onValid, onInvalid),
    selectedYear,
    isClosedYear,
    isParoco,
    isSecretarioUser,
    churches,
    filteredOrganizations,
    localOptions,
    projetoOptions,
    selectedProjectInfo,
    projectValidationError,
    isRecurring,
  };
};
