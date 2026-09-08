import React, {useEffect, useMemo, useState} from 'react';
import { useQuery } from '@tanstack/react-query';
import {
    Alert,
    alpha,
    Box,
    Card,
    CardContent,
    Chip,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Typography,
} from '@mui/material';
import {
    Add as AddIcon,
    Block as BlockIcon,
    Business as BusinessIcon,
    CalendarMonth as CalendarIcon,
    ChevronLeft,
    ChevronRight,
    Close as CloseIcon,
    Description as DescriptionIcon,
    Download as DownloadIcon,
    Edit as EditIcon,
    Event as EventIcon,
    FormatListBulleted as ListIcon,
    Groups as PeopleIcon,
    LocationOn as LocationIcon,
    Refresh as RefreshIcon,
    Schedule as ScheduleIcon,
    Today as TodayIcon,
} from '@mui/icons-material';
import {useAuth} from '../auth/AuthContext';
import { isGlobalCreator } from '../auth/permissions';
import {useNotification} from '../context/NotificationContext';
import CadastroEventoModal from '../components/CadastroEventoModal';
import CancelarEventoDialog from '../components/CancelarEventoDialog';
import EditScopeDialog, {EditScope} from '../components/EditScopeDialog';
import {ExportCalendarDialog} from '../components/ExportCalendarDialog';
import { EventoConflitoRecursoDialog } from '../components/EventoConflitoRecursoDialog';
import ObservacoesEvento from '../components/ObservacoesEvento';
import AtivosAlocadosSection from '../components/AtivosAlocadosSection';
import PageHeader from '../components/PageHeader';
import ActionButton from '../components/ActionButton';
import StatusIcon from '../components/StatusIcon';
import { getEventoStatusMeta } from '../utils/statusMeta/eventoStatus';
import { getCategoriaEventoMeta } from '../utils/statusMeta/categoriaEventoStatus';
import theme from '../theme';
import apiClient from '../api/client';
import { getOrganizacoes } from '../api/organizacoes';
import { getAtivos } from '../api/ativos';
import { extractErrorMessage } from '../utils/errorUtils';
import {AnoParoquial, AtivoPatrimonial, Evento, Local, Organizacao, Projeto} from '../api/types';
import {getAnosParoquiais} from '../api/anosParoquiais';
import {fetchSolenidadesLiturgicasAtivas} from '../api/solenidades';
import {
  getLiturgicalSeason,
  getLiturgicalSeasonStyle,
  getLiturgicalCycleLetter,
  getSolemnitiesForYear,
  buildSolemnitiesFromApi,
  SolemnityInfo,
} from '../utils/liturgicalCalendar';

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
}

const CATEGORIES = [
  'PASTORAL',
  'SOCIAL',
  'LITURGICO',
  'ADMINISTRATIVO',
  'SACRAMENTAL',
  'FORMATIVO',
  'ASSISTENCIAL',
];

const getCategoryColor = (category: string) => {
  switch (category) {
    case 'PASTORAL':
      return { bg: '#e8f5e9', text: '#2e7d32', border: '#4caf50' };
    case 'SOCIAL':
      return { bg: '#fff3e0', text: '#e65100', border: '#ff9800' };
    case 'LITURGICO':
      return { bg: '#f3e5f5', text: '#7b1fa2', border: '#9c27b0' };
    case 'ADMINISTRATIVO':
      return { bg: '#eceff1', text: '#37474f', border: '#607d8b' };
    case 'SACRAMENTAL':
      return { bg: '#e1f5fe', text: '#0277bd', border: '#03a9f4' };
    case 'FORMATIVO':
      return { bg: '#fce4ec', text: '#c2185b', border: '#e91e63' };
    case 'ASSISTENCIAL':
      return { bg: '#e0f7fa', text: '#00838f', border: '#00bcd4' };
    default:
      return { bg: '#f5f5f5', text: '#616161', border: '#9e9e9e' };
  }
};

const isSameDay = (d1: Date, d2: Date) => {
  return (
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
  );
};

const Calendar = () => {
  const { user } = useAuth();
  const { showError, showSuccess } = useNotification();

  // Calendar view states
  const [currentDate, setCurrentDate] = useState(new Date());
  const [viewMode, setViewMode] = useState<'month' | 'agenda'>('month');
  const [events, setEvents] = useState<Evento[]>([]);
  const [organizations, setOrganizations] = useState<Organizacao[]>([]);
  const [locais, setLocais] = useState<Local[]>([]);
  const [ativos, setAtivos] = useState<AtivoPatrimonial[]>([]);
  const [projetos, setProjetos] = useState<Projeto[]>([]);
  const [anosParoquiais, setAnosParoquiais] = useState<AnoParoquial[]>([]);

  // Filtering states
  const [selectedOrgFilter, setSelectedOrgFilter] = useState<string>('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('');

  // Status/loading states
  const [isLoading, setIsLoading] = useState(false);
  const [aprovacoesPendentes, setAprovacoesPendentes] = useState<any[]>([]);

  // Modal details states
  const [selectedEvent, setSelectedEvent] = useState<Evento | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  // Modal creation/editing states
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<Evento | null>(null);
  const [createInitialDate, setCreateInitialDate] = useState<Date | undefined>(undefined);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Modal cancellation states
  const [isCancelOpen, setIsCancelOpen] = useState(false);
  const [cancellingEvent, setCancellingEvent] = useState<Evento | null>(null);
  const [isCancelling, setIsCancelling] = useState(false);

  // Recurrence edit scope states
  const [isEditScopeOpen, setIsEditScopeOpen] = useState(false);
  const [selectedEditScope, setSelectedEditScope] = useState<EditScope | null>(null);

  // Export states
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [isSolemnitiesOpen, setIsSolemnitiesOpen] = useState(false);

  // Resource Conflict (409) states
  const [conflitoRecursoModalOpen, setConflitoRecursoModalOpen] = useState(false);
  const [conflitoRecursoDetalhes, setConflitoRecursoDetalhes] = useState<any[]>([]);
  const [conflitoRecursoMensagem, setConflitoRecursoMensagem] = useState<string>('');

  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth();

  const { data: apiSolenidades = [] } = useQuery({
    queryKey: ['solenidadesLiturgicasAtivas'],
    queryFn: fetchSolenidadesLiturgicasAtivas,
  });

  const solemnities = useMemo(
    () => buildSolemnitiesFromApi(apiSolenidades, currentYear),
    [apiSolenidades, currentYear]
  );

  const liturgicalInfo = useMemo(() => {
    const cycleLetter = getLiturgicalCycleLetter(currentYear);
    const anoConfig = anosParoquiais.find(a => a.ano === currentYear);
    const isClosed = anoConfig?.status === 'FECHADO';
    return {
      year: currentYear,
      cycle: `Ano ${cycleLetter} (${currentYear})`,
      letter: cycleLetter,
      isClosed,
    };
  }, [currentYear, anosParoquiais]);

  // Permissions check (simple for now, matches backend logic)
  const canEditEvent = (event: Evento) => {
    if (!user) return false;
    if (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN')) return true;
    
    // Member can edit if they belong to the organization responsible
    return user.organizations.some(org => org.id === event.organizacaoResponsavelId);
  };

  // Generate calendar days for monthly grid
  const calendarDays = useMemo(() => {
    const days: CalendarDay[] = [];
    const firstDay = new Date(currentYear, currentMonth, 1);
    const startDayOfWeek = firstDay.getDay(); // 0 (Sun) to 6 (Sat)
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const prevMonthDays = new Date(currentYear, currentMonth, 0).getDate();

    // Fill days from previous month
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      const d = new Date(currentYear, currentMonth - 1, prevMonthDays - i);
      days.push({
        date: d,
        isCurrentMonth: false,
        isToday: isSameDay(d, new Date()),
      });
    }

    // Fill current month days
    for (let i = 1; i <= daysInMonth; i++) {
      const d = new Date(currentYear, currentMonth, i);
      days.push({
        date: d,
        isCurrentMonth: true,
        isToday: isSameDay(d, new Date()),
      });
    }

    // Fill days from next month to complete the 6 weeks (42 cells) grid
    const totalCells = 42;
    const remainingCells = totalCells - days.length;
    for (let i = 1; i <= remainingCells; i++) {
      const d = new Date(currentYear, currentMonth + 1, i);
      days.push({
        date: d,
        isCurrentMonth: false,
        isToday: isSameDay(d, new Date()),
      });
    }

    return days;
  }, [currentYear, currentMonth]);

  // Restrict organizations based on user role
  const filteredOrganizations = useMemo(() => {
    if (!user) return [];
    if (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN')) return organizations;
    
    const userOrgIds = user.organizations.map(org => org.id);
    return organizations.filter(org => userOrgIds.includes(org.id));
  }, [user, organizations]);

  // Fetch events and organizations
  const fetchData = async () => {
    setIsLoading(true);
    try {
      // Calculate start and end of current visible month grid
      const startOfGrid = calendarDays[0].date;
      const endOfGrid = calendarDays[calendarDays.length - 1].date;

      const fetchEvents = async () => {
        // Prepare request parameters for event filters
        const params: Record<string, any> = {
          dataInicio: startOfGrid.toISOString(),
          dataFim: endOfGrid.toISOString(),
          size: 200, // retrieve up to 200 events for full grid density
        };

        // Apply organization filter
        if (selectedOrgFilter) {
          params.organizacaoIds = [selectedOrgFilter];
        } else if (user && !isGlobalCreator(user)) {
          // Implicitly filter by user's organizations if none selected and not global creator
          const userOrgIds = Array.isArray(user.organizations) ? user.organizations.map((org: { id: string }) => org.id) : [];
          if (userOrgIds.length > 0) {
            params.organizacaoIds = userOrgIds;
          } else {
            // If user has no organizations and is not global creator, they shouldn't see events
            setEvents([]);
            return;
          }
        }

        if (selectedCategoryFilter) {
          params.categoria = selectedCategoryFilter;
        }

        // Fetch events
        const eventsRes = await apiClient.get<any>('/eventos', { 
          params,
          // Ensure arrays are serialized as organizacaoIds=id1&organizacaoIds=id2
          paramsSerializer: {
            indexes: null
          }
        });
        
        // Handle page response format
        if (eventsRes.data && Array.isArray(eventsRes.data.content)) {
          setEvents(eventsRes.data.content);
        } else if (Array.isArray(eventsRes.data)) {
          setEvents(eventsRes.data);
        } else {
          setEvents([]);
        }
      };

      const fetchOrgs = async () => {
        // Fetch organizations list (only if empty)
        if (organizations.length === 0) {
          try {
            const orgsData = await getOrganizacoes();
            if (Array.isArray(orgsData)) {
              setOrganizations(orgsData);
            }
          } catch (err) {
          }
        }
      };

      const fetchLocais = async () => {
        try {
          const res = await apiClient.get<Local[]>('/locais');
          if (Array.isArray(res.data)) {
            setLocais(res.data);
          }
        } catch (err) {
        }
      };

      const fetchProjetos = async () => {
        try {
          const res = await apiClient.get<Projeto[]>('/projetos');
          if (Array.isArray(res.data)) {
            setProjetos(res.data);
          }
        } catch (err) {
        }
      };

      const buscarAprovacoes = async () => {
        try {
          const res = await apiClient.get<any>('/aprovacoes', {
            params: { status: 'PENDENTE', size: 100 }
          });
          if (res.data && Array.isArray(res.data.content)) {
            setAprovacoesPendentes(res.data.content);
          } else if (Array.isArray(res.data)) {
            setAprovacoesPendentes(res.data);
          } else {
            setAprovacoesPendentes([]);
          }
        } catch (err) {
          setAprovacoesPendentes([]);
        }
      };

      const fetchAnosParoquiais = async () => {
        try {
          const anos = await getAnosParoquiais();
          if (Array.isArray(anos)) {
            setAnosParoquiais(anos);
          }
        } catch (err) {
        }
      };

      const fetchAtivos = async () => {
        if (ativos.length === 0) {
          try {
            const data = await getAtivos();
            if (Array.isArray(data)) {
              setAtivos(data);
            }
          } catch (err) {
          }
        }
      };

      await Promise.all([fetchEvents(), fetchOrgs(), fetchLocais(), fetchProjetos(), buscarAprovacoes(), fetchAnosParoquiais(), fetchAtivos()]);
    } catch (err: unknown) {
      const errMsg = 'Não foi possível carregar as informações do calendário.';
      showError(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, currentDate, selectedOrgFilter, selectedCategoryFilter]);

  // Navigation handlers
  const handlePrevMonth = () => {
    setCurrentDate(new Date(currentYear, currentMonth - 1, 1));
  };

  const handleNextMonth = () => {
    setCurrentDate(new Date(currentYear, currentMonth + 1, 1));
  };

  const handleToday = () => {
    setCurrentDate(new Date());
  };

  // Open Event details dialog
  const handleOpenDetails = (event: Evento) => {
    setSelectedEvent(event);
    setIsDetailsOpen(true);
  };

  // Open Event cancellation dialog
  const handleOpenCancel = (event: Evento) => {
    setCancellingEvent(event);
    setIsCancelOpen(true);
  };

  // Open Event creation dialog with optional prefilled date
  const handleOpenCreate = (prefilledDate?: Date) => {
    setEditingEvent(null);
    setCreateInitialDate(prefilledDate);
    setIsCreateOpen(true);
  };

  // Open Event editing dialog
  // Eventos recorrentes (recorrenciaId presente) exigem que o usuário decida o
  // escopo da edição antes de abrir o formulário: alterar só esta ocorrência ou a
  // série inteira (EditScopeDialog). Eventos avulsos não têm essa ambiguidade e vão
  // direto para o formulário de edição.
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
  const handleScopeSelect = (scope: EditScope) => {
    setSelectedEditScope(scope);
    setIsEditScopeOpen(false);
    setIsDetailsOpen(false);
    setIsCreateOpen(true);
  };

  // Handle event submission (create or update)
  const handleEventSubmit = async (data: Record<string, unknown>) => {
    setIsSubmitting(true);
    try {
      if (editingEvent) {
        // UPDATE (PATCH)
        // editScope diz ao backend o alcance da edição: ONLY_THIS altera apenas esta
        // ocorrência (destacando-a da série), enquanto um escopo de série propaga a
        // mudança para as demais ocorrências futuras da recorrência. Default
        // ONLY_THIS cobre o caso de evento não-recorrente (selectedEditScope nunca é
        // definido nesse caminho, ver handleOpenEdit).
        const { recurrence, ...eventBaseData } = data;
        const updateData = {
          ...eventBaseData,
          ativoIds: Array.isArray(data.ativoIds) ? data.ativoIds : [],
          editScope: selectedEditScope || 'ONLY_THIS'
        };
        await apiClient.patch(`/eventos/${editingEvent.id}`, updateData);
        const msg = 'Evento atualizado com sucesso!';
        showSuccess(msg);
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
        };

        // Generate a simple unique idempotency key for the request headers
        const idempotencyKey =
          typeof crypto !== 'undefined' && crypto.randomUUID
            ? crypto.randomUUID()
            : Math.random().toString(36).substring(2) + Date.now().toString(36);

        const res = await apiClient.post('/eventos', requestData, {
          headers: {
            'Idempotency-Key': idempotencyKey,
          },
        });

        // If recurrence is enabled, call the recurrence endpoint
        if (recurrence && res.data?.id) {
          await apiClient.put(`/eventos/${res.data.id}/recorrencia`, recurrence);
        }

        const msg = 'Evento criado com sucesso!';
        showSuccess(msg);
      }

      setIsCreateOpen(false);
      fetchData();
    } catch (err: any) {
      if (err.response?.status === 409 && err.response?.data?.conflitos) {
        setConflitoRecursoDetalhes(err.response.data.conflitos);
        setConflitoRecursoMensagem(err.response.data.message);
        setConflitoRecursoModalOpen(true);
      } else {
        showError(extractErrorMessage(err, 'Erro ao processar evento. Verifique as regras de negócio.'));
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCancelConfirm = async (reason: string) => {
    if (!cancellingEvent) return;
    
    setIsCancelling(true);
    try {
      await apiClient.post(`/eventos/${cancellingEvent.id}/cancel`, { motivo: reason });
      const msg = 'Evento cancelado com sucesso!';
      showSuccess(msg);
      setIsCancelOpen(false);
      setIsDetailsOpen(false);
      fetchData();
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao cancelar evento.'));
    } finally {
      setIsCancelling(false);
    }
  };

  // Combine API events with transient events from pending creation requests
  //
  // O calendário precisa mostrar não só os eventos já confirmados (endpoint
  // /eventos), mas também o efeito de solicitações de aprovação ainda PENDENTES
  // (buscadas em aprovacoesPendentes), para que o usuário veja "o que vai acontecer
  // se isso for aprovado" sem que a mudança já tenha sido aplicada de fato — nenhum
  // desses eventos "pendentes" existe de verdade no backend ainda; é só uma
  // pré-visualização client-side construída a partir do payload da solicitação.
  const combinedEvents = useMemo(() => {
    // 1. Transient creation events: uma solicitação de CRIACAO_EVENTO ainda não gerou
    // um EventoEntity real, então sintetizamos aqui um objeto Evento "fantasma" só
    // para desenhar no grid/agenda, usando o payload serializado da solicitação.
    // Esse evento nunca existe no array `events` vindo da API.
    const transientEvents = aprovacoesPendentes
      .filter((a) => a.tipoSolicitacao === 'CRIACAO_EVENTO' && a.actionPayloadJson)
      .map((a): Evento | null => {
        try {
          const payload = JSON.parse(a.actionPayloadJson);
          return {
            id: `pending-creation-${a.id}`,
            titulo: payload.titulo || 'Novo Evento (Pendente)',
            descricao: payload.descricao || '',
            categoria: payload.categoria || 'PASTORAL',
            organizacaoResponsavelId: payload.organizacaoResponsavelId || '',
            projetoId: payload.projetoId || null,
            nomeProjeto: null,
            localId: payload.localId || null,
            inicio: payload.inicio,
            fim: payload.fim,
            status: 'PENDENTE_CRIACAO', // custom status for pending creation
            // Evento ainda não existe de fato, então não há como o motor de conflitos
            // do backend tê-lo avaliado — não se aplica estado de conflito aqui.
            estadoConflito: null,
            motivoConflito: null,
            // aprovacaoPendenteId / tipoSolicitacaoPendente marcam este evento como
            // "sujeito a aprovação" para a UI (ex.: exibir badge [Pendente], opacidade
            // reduzida e ocultar ações de editar/cancelar — ver JSX mais abaixo).
            aprovacaoPendenteId: a.id,
            solicitantePendenteId: a.solicitanteId,
            tipoSolicitacaoPendente: a.tipoSolicitacao
          };
        } catch (e) {
          return null;
        }
      })
      .filter((e): e is Evento => e !== null);

    // 2. Decorated events: eventos que JÁ existem no backend mas têm uma edição ou
    // cancelamento aguardando aprovação são "decorados" com os mesmos campos de
    // pendência acima, para que a UI sinalize que este evento tem uma mudança em
    // trânsito (o conteúdo exibido ainda é o estado atual/confirmado do evento — só
    // a mudança proposta é que está pendente, não o evento em si).
    const decoratedEvents = events.map((e) => {
      const acaoPendente = aprovacoesPendentes.find((a) => a.eventoId === e.id);
      if (acaoPendente) {
        return {
          ...e,
          aprovacaoPendenteId: acaoPendente.id,
          solicitantePendenteId: acaoPendente.solicitanteId,
          tipoSolicitacaoPendente: acaoPendente.tipoSolicitacao,
          tipoAcaoPendente: acaoPendente.tipoSolicitacao
        };
      }
      return e;
    });

    return [...decoratedEvents, ...transientEvents];
  }, [events, aprovacoesPendentes]);

  // Map events to their dates for quick lookup
  const getEventsForDay = (day: Date) => {
    return combinedEvents.filter((event) => {
      const eventStart = new Date(event.inicio);
      return isSameDay(eventStart, day);
    });
  };

  // Month name mapping
  const monthNames = [
    'Janeiro',
    'Fevereiro',
    'Março',
    'Abril',
    'Maio',
    'Junho',
    'Julho',
    'Agosto',
    'Setembro',
    'Outubro',
    'Novembro',
    'Dezembro',
  ];

  // Helper to find organization name by ID
  const getOrgName = (orgId: string, igrejaVinculoId?: string | null) => {
    const org = organizations.find((o) => o.id === orgId);
    const orgName = org ? org.nome : 'Organização Paroquial';
    if (igrejaVinculoId) {
      const church = organizations.find((o) => o.id === igrejaVinculoId);
      if (church) {
        return `${orgName} - ${church.nome}`;
      }
    }
    return orgName;
  };

  const getLocalName = (localId: string | null) => {
    if (!localId) return null;
    const local = locais.find((l) => l.id === localId);
    return local ? local.nome : null;
  };

  // Filtered upcoming events list (for Agenda/List View)
  const agendaEvents = useMemo(() => {
    return [...combinedEvents]
      .filter((e) => new Date(e.inicio) >= calendarDays[0].date)
      .sort((a, b) => new Date(a.inicio).getTime() - new Date(b.inicio).getTime());
  }, [combinedEvents, calendarDays]);

  return (
    <Box>
      <PageHeader
        icon={<CalendarIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Calendário Paroquial"
        subtitle="Agendamento de eventos, celebrações e atividades pastorais da paróquia."
        actions={
          <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
            <ActionButton
              title="Solenidades & Santoral"
              icon={<EventIcon />}
              color="secondary"
              onClick={() => setIsSolemnitiesOpen(true)}
            />
            <ActionButton
              title="Exportar"
              icon={<DownloadIcon />}
              color="info"
              onClick={() => setIsExportOpen(true)}
            />
            <ActionButton
              title="Novo Evento"
              icon={<AddIcon />}
              color="primary"
              onClick={() => handleOpenCreate()}
            />
          </Box>
        }
      />

      {/* Toolbar / Controls Card Padronizado */}
      <Card
        elevation={0}
        sx={{
          p: 1,
          mb: 3,
          
          border: '1px solid',
          borderColor: 'divider',
          backgroundColor: 'action.hover',
          overflow: 'visible',
        }}
      >
        <CardContent sx={{ p: { xs: 2, md: 3 } }}>
          <Grid container spacing={2} sx={{ alignItems: 'center' }}>
            {/* Top Line: Month Navigation (Left) & Action Buttons (Right) */}
            <Grid size={{ xs: 12, lg: 8 }} sx={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 1 }}>
              <ActionButton label="Anterior" title="Mês anterior" icon={<ChevronLeft />} onClick={handlePrevMonth} color="primary" />
              <Typography variant="h5" sx={{ minWidth: 130, textAlign: 'center', fontWeight: 600, color: 'text.primary' }}>
                {monthNames[currentMonth]} {currentYear}
              </Typography>
              <ActionButton label="Próximo" title="Próximo mês" icon={<ChevronRight />} onClick={handleNextMonth} color="primary" />
              <ActionButton label="Hoje" title="Ir para Mês Atual" icon={<TodayIcon />} onClick={handleToday} color="primary" />
              <Chip
                label={`Ano Litúrgico: ${liturgicalInfo.cycle}`}
                color="primary"
                variant="outlined"
                size="small"
                sx={{ fontWeight: 700 }}
              />
              {liturgicalInfo.isClosed && (
                <Chip
                  label="ANO FECHADO"
                  color="error"
                  size="small"
                  sx={{ fontWeight: 700 }}
                />
              )}
            </Grid>

            <Grid size={{ xs: 12, lg: 4 }} sx={{ display: 'flex', justifyContent: { xs: 'flex-start', lg: 'flex-end' }, alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
              <Box sx={{ border: '1px solid', borderColor: 'divider', overflow: 'hidden', display: 'flex' }}>
                <ActionButton
                  label="Grade"
                  title="Visualização em grade"
                  ariaLabel="Mês"
                  icon={<CalendarIcon />}
                  color={viewMode === 'month' ? 'primary' : undefined}
                  onClick={() => setViewMode('month')}
                  sx={{ border: 'none', bgcolor: viewMode === 'month' ? (theme) => alpha(theme.palette.primary.main, 0.1) : 'transparent' }}
                />
                <ActionButton
                  label="Agenda"
                  title="Visualização em agenda"
                  icon={<ListIcon />}
                  color={viewMode === 'agenda' ? 'primary' : undefined}
                  onClick={() => setViewMode('agenda')}
                  sx={{ border: 'none', bgcolor: viewMode === 'agenda' ? (theme) => alpha(theme.palette.primary.main, 0.1) : 'transparent' }}
                />
              </Box>
            </Grid>

            {/* Line Below: Filtering Controls */}
            <Grid size={{ xs: 12 }} sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center', mt: 1, pt: 1, borderTop: '1px solid', borderColor: 'divider' }}>
              <FormControl size="small" sx={{ minWidth: 200, maxWidth: 300, flexGrow: 1 }}>
                <InputLabel id="org-filter-label">Organização</InputLabel>
                <Select
                  labelId="org-filter-label"
                  value={selectedOrgFilter}
                  label="Organização"
                  onChange={(e) => setSelectedOrgFilter(e.target.value)}
                 
                >
                  <MenuItem value="">
                    <em>Todas</em>
                  </MenuItem>
                  {filteredOrganizations.map((org) => (
                    <MenuItem key={org.id} value={org.id}>
                      {org.nome}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <FormControl size="small" sx={{ minWidth: 180, maxWidth: 260, flexGrow: 1 }}>
                <InputLabel id="cat-filter-label">Categoria</InputLabel>
                <Select
                  labelId="cat-filter-label"
                  value={selectedCategoryFilter}
                  label="Categoria"
                  onChange={(e) => setSelectedCategoryFilter(e.target.value)}
                 
                >
                  <MenuItem value="">
                    <em>Todas</em>
                  </MenuItem>
                  {CATEGORIES.map((cat) => (
                    <MenuItem key={cat} value={cat}>
                      {cat}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <ActionButton
                title="Recarregar dados do calendário"
                icon={isLoading ? <CircularProgress size={24} /> : <RefreshIcon />}
                onClick={fetchData}
                disabled={isLoading}
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* View Content */}
      {viewMode === 'month' ? (
        /* Monthly Grid View */
         <Card sx={{ overflow: 'hidden' }}>
          {/* Days of week header */}
          <Grid container columns={7} sx={{ bgcolor: 'action.hover', borderBottom: '1px solid', borderColor: 'divider', textAlign: 'center', py: 1.5 }}>
            {['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'].map((day) => (
              <Grid size={{ xs: 1 }} key={day}>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'text.secondary' }}>
                  {day}
                </Typography>
              </Grid>
            ))}
          </Grid>

          {/* Calendar Grid Cells */}
          <Grid container columns={7}>
            {calendarDays.map(({ date, isCurrentMonth, isToday }, idx) => {
              const dayEvents = getEventsForDay(date);
              const season = getLiturgicalSeason(date);
              const seasonStyle = getLiturgicalSeasonStyle(season);
              return (
                <Grid
                  size={{ xs: 1 }}
                  key={idx}
                  onClick={() => handleOpenCreate(date)}
                  sx={{
                    minHeight: 130,
                    borderRight: '1px solid',
                    borderBottom: '1px solid',
                    borderColor: 'divider',
                    bgcolor: isToday
                      ? (theme) => alpha(theme.palette.success.main, 0.08)
                      : isCurrentMonth ? seasonStyle.backgroundColor : 'action.hover',
                    p: 1,
                    cursor: 'pointer',
                    transition: 'all 0.15s ease',
                    position: 'relative',
                    '&:hover': {
                      bgcolor: isToday
                        ? (theme) => alpha(theme.palette.success.main, 0.16)
                        : 'action.selected',
                      '& .add-day-btn': { opacity: 1 },
                    },
                  }}
                >
                  {/* Day number, liturgical season label (desktop) and indicator */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: isToday ? 700 : 500,
                        color: isToday ? 'success.dark' : isCurrentMonth ? 'text.primary' : 'text.disabled',
                        bgcolor: isToday ? (theme) => alpha(theme.palette.success.main, 0.2) : 'transparent',
                        borderRadius: '50%',
                        width: 24,
                        height: 24,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      {date.getDate()}
                    </Typography>

                    {/* Liturgical Season Label for Desktop */}
                    <Typography
                      variant="caption"
                      sx={{
                        fontSize: '0.625rem',
                        fontWeight: 700,
                        color: seasonStyle.textColor,
                        opacity: isCurrentMonth ? 0.85 : 0.45,
                        textTransform: 'uppercase',
                        lineHeight: 1,
                        display: { xs: 'none', md: 'block' },
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                      }}
                    >
                      {seasonStyle.label}
                    </Typography>

                    {/* Plus icon on hover */}
                    <AddIcon
                      className="add-day-btn"
                      color="action"
                      sx={{
                        fontSize: '1rem',
                        opacity: 0,
                        transition: 'opacity 0.2s',
                        '&:hover': { color: 'success.dark' },
                      }}
                    />
                  </Box>

                  {/* Events list in the day cell */}
                  <Box sx={{ height: 90, overflowY: 'auto', pr: 0.5 }}>
                    {dayEvents.map((evt) => {
                      const styleColors = getCategoryColor(evt.categoria);
                      return (
                        <Box
                          key={evt.id}
                          onClick={(e) => {
                            e.stopPropagation(); // prevent opening create modal
                            handleOpenDetails(evt);
                          }}
                          sx={{
                            bgcolor: evt.status?.toUpperCase() === 'CANCELADO'
                              ? (theme) => alpha(theme.palette.error.main, 0.12)
                              : styleColors.bg,
                            color: evt.status?.toUpperCase() === 'CANCELADO' ? 'error.dark' : styleColors.text,
                            borderLeft: `3px solid ${evt.status?.toUpperCase() === 'CANCELADO' ? 'error.main' : styleColors.border}`,
                            px: 1,
                            py: 0.5,
                            mb: 0.5,
                            fontSize: '0.72rem',
                            fontWeight: 600,
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            transition: 'all 0.2s',
                            textDecoration: evt.status?.toUpperCase() === 'CANCELADO' ? 'line-through' : 'none',
                            opacity: evt.aprovacaoPendenteId ? 0.6 : (evt.status?.toUpperCase() === 'CANCELADO' ? 0.75 : 1),
                            '&:hover': {
                              filter: 'brightness(0.93)',
                              transform: 'translateX(2px)',
                            },
                          }}
                          title={evt.aprovacaoPendenteId ? `[Pendente] ${evt.titulo}` : evt.titulo}
                        >
                          {evt.aprovacaoPendenteId ? `[Pendente] ${evt.titulo}` : evt.titulo}
                        </Box>
                      );
                    })}
                  </Box>
                </Grid>
              );
            })}
          </Grid>
        </Card>
      ) : (
        /* Agenda / List View */
        agendaEvents.length === 0 ? (
          <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
            <CalendarIcon sx={{ fontSize: 48, color: 'action.disabled' }} />
            <Typography variant="body2" color="text.secondary">
              Nenhum evento agendado para o período visível.
            </Typography>
          </Card>
        ) : (
          <Paper elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
            {agendaEvents.map((evt, idx) => {
              const styleColors = getCategoryColor(evt.categoria);
              const start = new Date(evt.inicio);
              const end = new Date(evt.fim);
              const isCancelado = evt.status?.toUpperCase() === 'CANCELADO';

              return (
                <Box
                  key={evt.id}
                  onClick={() => handleOpenDetails(evt)}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 2,
                    p: 2,
                    cursor: 'pointer',
                    borderBottom: idx < agendaEvents.length - 1 ? '1px solid' : 'none',
                    borderColor: 'divider',
                    borderLeft: '4px solid',
                    borderLeftColor: isCancelado ? 'error.main' : styleColors.border,
                    opacity: evt.aprovacaoPendenteId ? 0.6 : (isCancelado ? 0.8 : 1),
                    '&:hover': { bgcolor: 'action.hover' },
                  }}
                >
                  {/* Date badge */}
                  <Box
                    sx={{
                      width: 56,
                      height: 56,
                      flexShrink: 0,
                      borderRadius: 1,
                      bgcolor: 'action.hover',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      lineHeight: 1,
                    }}
                  >
                    <Typography variant="caption" sx={{ fontWeight: 700, fontSize: '0.65rem', color: isCancelado ? 'error.main' : 'text.secondary', textTransform: 'uppercase' }}>
                      {monthNames[start.getMonth()].substring(0, 3)}
                    </Typography>
                    <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.1, color: isCancelado ? 'error.dark' : 'text.primary' }}>
                      {start.getDate()}
                    </Typography>
                  </Box>

                  {/* Title, description & badges */}
                  <Box sx={{ minWidth: 0, flexGrow: 1 }}>
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: 700,
                        color: isCancelado ? 'error.dark' : 'text.primary',
                        textDecoration: isCancelado ? 'line-through' : 'none',
                      }}
                    >
                      {evt.aprovacaoPendenteId ? `[Pendente] ${evt.titulo}` : evt.titulo}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ lineClamp: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', mb: 0.5 }}>
                      {evt.descricao || 'Sem descrição.'}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
                      <StatusIcon {...getCategoriaEventoMeta(evt.categoria)} />
                      <Chip size="small" variant="outlined" label={getOrgName(evt.organizacaoResponsavelId, evt.igrejaVinculoId)} icon={<BusinessIcon sx={{ fontSize: '0.9rem' }} />} />
                      <StatusIcon {...getEventoStatusMeta(evt.status)} />
                    </Box>
                  </Box>

                  {/* Event time range */}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary', flexShrink: 0 }}>
                    <ScheduleIcon sx={{ fontSize: 18 }} />
                    <Typography variant="body2" sx={{ fontWeight: 500, whiteSpace: 'nowrap' }}>
                      {start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })} -{' '}
                      {end.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                    </Typography>
                  </Box>
                </Box>
              );
            })}
          </Paper>
        )
      )}

      {/* Details Dialog Modal */}
       <Dialog open={isDetailsOpen} onClose={() => setIsDetailsOpen(false)} maxWidth="sm" fullWidth>
        {selectedEvent && (
          <>
            <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Typography variant="h6" component="span" sx={{ fontWeight: 600 }}>
                  Detalhes do Evento
                </Typography>
                <StatusIcon {...getEventoStatusMeta(selectedEvent.status)} sx={{ fontWeight: 600 }} />
              </Box>
              <ActionButton title="Fechar" icon={<CloseIcon />} onClick={() => setIsDetailsOpen(false)} />
            </DialogTitle>
            <DialogContent dividers sx={{py: 2, maxHeight: 'calc(100vh - 220px)'}}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                {/* Quando este evento tem uma solicitação de aprovação em aberto
                    (ver merge em combinedEvents), avisamos qual tipo de mudança está
                    pendente — o conteúdo abaixo ainda reflete o estado atual/salvo do
                    evento, não a mudança proposta ainda não aprovada. */}
                {selectedEvent.tipoSolicitacaoPendente && (
                  <Alert severity="warning">
                    {selectedEvent.tipoSolicitacaoPendente === 'CRIACAO_EVENTO'
                      ? 'Solicitação de Criação Pendente'
                      : selectedEvent.tipoSolicitacaoPendente === 'CANCELAMENTO'
                      ? 'Solicitação de Cancelamento Pendente'
                      : selectedEvent.tipoSolicitacaoPendente === 'EDICAO_EVENTO'
                      ? 'Solicitação de Edição Pendente'
                      : 'Solicitação Pendente'}
                  </Alert>
                )}
                {/* Title */}
                <Box>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main', mb: 1 }}>
                    {selectedEvent.titulo}
                  </Typography>
                  <StatusIcon {...getCategoriaEventoMeta(selectedEvent.categoria)} />
                </Box>

                {/* Date & Time */}
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                  <ScheduleIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                      Horário
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600, color: 'text.primary' }}>
                      {new Date(selectedEvent.inicio).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })} às{' '}
                      {new Date(selectedEvent.fim).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}
                    </Typography>
                  </Box>
                </Box>

                {/* Organization */}
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                  <BusinessIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                      Organização Responsável
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600, color: 'text.primary' }}>
                      {getOrgName(selectedEvent.organizacaoResponsavelId, selectedEvent.igrejaVinculoId)}
                    </Typography>
                  </Box>
                </Box>

                {/* Description */}
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
                  <DescriptionIcon color="action" sx={{ mt: 0.3 }} />
                  <Box sx={{ width: '100%' }}>
                    <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500, mb: 0.5 }}>
                      Descrição
                    </Typography>
                     <Typography variant="body1" sx={{ color: 'text.secondary', whiteSpace: 'pre-wrap', bgcolor: theme.custom.subtlePanelBg, p: 1.5, border: '1px solid', borderColor: 'divider' }}>
                      {selectedEvent.descricao || 'Nenhuma descrição fornecida.'}
                    </Typography>
                  </Box>
                </Box>

                {/* Local */}
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                  <LocationIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                      Local
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600, color: 'text.primary' }}>
                      {getLocalName(selectedEvent.localId) || 'Não especificado'}
                    </Typography>
                  </Box>
                </Box>

                {/* Project */}
                {selectedEvent.nomeProjeto && (
                  <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                    <BusinessIcon color="action" /> {/* Using BusinessIcon for project as well or another if available */}
                    <Box>
                      <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                        Projeto
                      </Typography>
                      <Typography variant="body1" sx={{ fontWeight: 600, color: 'text.primary' }}>
                        {selectedEvent.nomeProjeto}
                      </Typography>
                    </Box>
                  </Box>
                )}

                {/* Supporting Organizations */}
                {selectedEvent.participantes && selectedEvent.participantes.length > 0 && (
                  <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
                    <PeopleIcon color="action" sx={{ mt: 0.3 }} />
                    <Box>
                      <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500, mb: 0.5 }}>
                        Organizações de Apoio
                      </Typography>
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {selectedEvent.participantes.map(id => (
                          <Chip key={id} label={getOrgName(id)} size="small" variant="outlined" />
                        ))}
                      </Box>
                    </Box>
                  </Box>
                )}

                {/* Ativos Patrimoniais vinculados */}
                {selectedEvent.ativoIds && selectedEvent.ativoIds.length > 0 && (
                  <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
                    <BusinessIcon color="action" sx={{ mt: 0.3 }} />
                    <Box>
                      <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500, mb: 0.5 }}>
                        Bens / Ativos Patrimoniais
                      </Typography>
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {selectedEvent.ativoIds.map(id => {
                          const ativo = ativos.find(a => a.id === id);
                          return (
                            <Chip
                              key={id}
                              label={ativo ? `${ativo.nome} (${ativo.categoria})` : id}
                              size="small"
                              variant="outlined"
                              color="primary"
                            />
                          );
                        })}
                      </Box>
                    </Box>
                  </Box>
                )}

                {/* Conflict warning: estadoConflito/motivoConflito são calculados pelo
                    backend (motor de validação de agenda) quando este evento colide
                    com outro compromisso — mesmo local, mesma organização/recurso,
                    ou regra de prioridade da agenda paroquial violada. O evento ainda
                    é salvo/exibido normalmente; o conflito é apenas um alerta para o
                    responsável avaliar manualmente, não um bloqueio automático. */}
                {selectedEvent.motivoConflito && (
                  <Alert severity="warning">
                    Conflito detectado: {selectedEvent.motivoConflito}
                  </Alert>
                )}

                {/* Allocated Assets */}
                <AtivosAlocadosSection eventoId={selectedEvent.id} />

                {/* Event Observations */}
                <ObservacoesEvento eventoId={selectedEvent.id} />
              </Box>
            </DialogContent>
            <DialogActions sx={{flexWrap: 'wrap', gap: 1}}>
              <ActionButton title="Fechar" icon={<CloseIcon />} onClick={() => setIsDetailsOpen(false)} color="secondary" />
              {/* Editar/Cancelar ficam ocultos enquanto há uma solicitação pendente
                  para este evento (aprovacaoPendenteId) — evita empilhar uma segunda
                  solicitação de mudança sobre a mesma entidade antes que a primeira
                  seja decidida pelo aprovador. */}
              {selectedEvent.status !== 'CANCELADO' && !selectedEvent.aprovacaoPendenteId && canEditEvent(selectedEvent) && (
                <>
                  <ActionButton title="Cancelar Evento" icon={<BlockIcon />} onClick={() => handleOpenCancel(selectedEvent)} color="error" />
                  <ActionButton title="Editar Evento" icon={<EditIcon />} onClick={() => handleOpenEdit(selectedEvent)} color="primary" />
                </>
              )}
            </DialogActions>
          </>
        )}
      </Dialog>

      {/* Create/Edit Dialog Modal Component */}
      <CadastroEventoModal
        open={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleEventSubmit}
        isSubmitting={isSubmitting}
        organizations={organizations.filter(org => org.ativo)}
        locais={locais.filter(l => l.ativo)}
        ativos={ativos.filter(a => a.reservavel && a.status === 'DISPONIVEL')}
        projetos={projetos.filter(p => p.status === 'ABERTO' || p.status === 'EM_ANDAMENTO')}
        initialDate={createInitialDate}
        initialEvent={editingEvent}
      />

      {/* Cancel Event Dialog Modal Component */}
      <CancelarEventoDialog
        open={isCancelOpen}
        onClose={() => setIsCancelOpen(false)}
        onConfirm={handleCancelConfirm}
        isSubmitting={isCancelling}
        eventTitle={cancellingEvent?.titulo}
      />

      {/* Edit Scope Dialog Modal Component */}
      <EditScopeDialog
        open={isEditScopeOpen}
        onClose={() => setIsEditScopeOpen(false)}
        onSelect={handleScopeSelect}
      />

      {/* Export Calendar Dialog Modal Component */}
      <ExportCalendarDialog
        open={isExportOpen}
        onClose={() => setIsExportOpen(false)}
        organizations={organizations}
      />

      {/* Recurso Conflict (409) Dialog */}
      <EventoConflitoRecursoDialog
        open={conflitoRecursoModalOpen}
        onClose={() => setConflitoRecursoModalOpen(false)}
        detalhes={conflitoRecursoDetalhes}
        mensagem={conflitoRecursoMensagem}
      />

      {/* Solenidades & Santoral Dialog Modal */}
      <Dialog
        open={isSolemnitiesOpen}
        onClose={() => setIsSolemnitiesOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: 'primary.main' }}>
          <Typography variant="h6" component="span" sx={{ fontWeight: 600 }}>
            Catálogo Litúrgico & Solenidades — {currentYear} ({liturgicalInfo.cycle})
          </Typography>
          <ActionButton title="Fechar" icon={<CloseIcon />} onClick={() => setIsSolemnitiesOpen(false)} />
        </DialogTitle>
        <DialogContent sx={{ pt: 3 }}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Solenidades fixas (Santoral) e datas móveis derivadas via algoritmo de Meeus/Jones/Butcher.
          </Typography>
          <Box sx={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #e0e0e0', backgroundColor: '#f5f5f5' }}>
                  <th style={{ padding: '8px 12px' }}>Data</th>
                  <th style={{ padding: '8px 12px' }}>Solenidade / Marco</th>
                  <th style={{ padding: '8px 12px' }}>Tipo</th>
                  <th style={{ padding: '8px 12px' }}>Cor Litúrgica</th>
                  <th style={{ padding: '8px 12px' }}>Fator Demanda</th>
                </tr>
              </thead>
              <tbody>
                {solemnities.map((sol) => (
                  <tr key={sol.chave} style={{ borderBottom: '1px solid #e0e0e0' }}>
                    <td style={{ padding: '8px 12px', fontWeight: 600 }}>
                      {sol.date.toLocaleDateString('pt-BR')}
                    </td>
                    <td style={{ padding: '8px 12px' }}>{sol.nome}</td>
                    <td style={{ padding: '8px 12px' }}>
                      <Chip
                        label={sol.tipo === 'FIXA' ? 'Santoral (Fixa)' : 'Móvel'}
                        size="small"
                        color={sol.tipo === 'FIXA' ? 'info' : 'secondary'}
                        variant="outlined"
                        sx={{ fontSize: '0.75rem', height: 20 }}
                      />
                    </td>
                    <td style={{ padding: '8px 12px' }}>
                      <Chip
                        label={sol.corLiturgica}
                        size="small"
                        sx={{
                          fontSize: '0.75rem',
                          height: 20,
                          backgroundColor:
                            sol.corLiturgica === 'ROXO' ? '#f3e5f5' :
                            sol.corLiturgica === 'VERMELHO' ? '#ffebee' :
                            sol.corLiturgica === 'VERDE' ? '#e8f5e9' : '#fff9c4',
                          color:
                            sol.corLiturgica === 'ROXO' ? '#7b1fa2' :
                            sol.corLiturgica === 'VERMELHO' ? '#c62828' :
                            sol.corLiturgica === 'VERDE' ? '#2e7d32' : '#f57f17',
                          fontWeight: 700,
                        }}
                      />
                    </td>
                    <td style={{ padding: '8px 12px', fontWeight: 600 }}>
                      {sol.fatorDemanda.toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Fechar" icon={<CloseIcon />} onClick={() => setIsSolemnitiesOpen(false)} color="primary" />
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Calendar;
