import React, {useEffect, useMemo, useState} from 'react';
import {
    Alert,
    Box,
    Button,
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
    IconButton,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Typography,
} from '@mui/material';
import {
    Add as AddIcon,
    Business as BusinessIcon,
    CalendarMonth as CalendarIcon,
    ChevronLeft,
    ChevronRight,
    Description as DescriptionIcon,
    Download as DownloadIcon,
    FormatListBulleted as ListIcon,
    Groups as PeopleIcon,
    LocationOn as LocationIcon,
    Refresh as RefreshIcon,
    Schedule as ScheduleIcon,
} from '@mui/icons-material';
import {useAuth} from '../auth/AuthContext';
import apiClient from '../api/client';
import {useNotification} from '../context/NotificationContext';
import EventRegistrationModal from '../components/EventRegistrationModal';
import EventCancelDialog from '../components/EventCancelDialog';
import EditScopeDialog, {EditScope} from '../components/EditScopeDialog';
import {ExportCalendarDialog} from '../components/ExportCalendarDialog';
import EventObservations from '../components/EventObservations';
import {Evento, Local, Organizacao, Projeto} from '../api/types';

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

const getStatusColor = (status: string): 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' => {
  switch (status?.toUpperCase()) {
    case 'CONFIRMADO':
      return 'success';
    case 'ADICIONADO_EXTRA':
      return 'secondary';
    case 'RASCUNHO':
      return 'warning';
    case 'CANCELADO':
      return 'error';
    default:
      return 'default';
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
  const [projetos, setProjetos] = useState<Projeto[]>([]);

  // Filtering states
  const [selectedOrgFilter, setSelectedOrgFilter] = useState<string>('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('');

  // Status/loading states
  const [isLoading, setIsLoading] = useState(false);
  const [pendingApprovals, setPendingApprovals] = useState<any[]>([]);

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

  // Get current year and month
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth();

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
        } else if (user && !(user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN'))) {
          // Implicitly filter by user's organizations if none selected and not ADMIN
          const userOrgIds = user.organizations.map(org => org.id);
          if (userOrgIds.length > 0) {
            params.organizacaoIds = userOrgIds;
          } else {
            // If user has no organizations and is not ADMIN, they shouldn't see events
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
            const orgsRes = await apiClient.get<Organizacao[]>('/organizacoes');
            if (Array.isArray(orgsRes.data)) {
              setOrganizations(orgsRes.data);
            }
          } catch (err) {
            console.error('Error fetching organizations:', err);
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
          console.error('Error fetching locales:', err);
        }
      };

      const fetchProjetos = async () => {
        try {
          const res = await apiClient.get<Projeto[]>('/projetos');
          if (Array.isArray(res.data)) {
            setProjetos(res.data);
          }
        } catch (err) {
          console.error('Error fetching projects:', err);
        }
      };

      const fetchApprovals = async () => {
        try {
          const res = await apiClient.get<any>('/aprovacoes', {
            params: { status: 'PENDENTE', size: 100 }
          });
          if (res.data && Array.isArray(res.data.content)) {
            setPendingApprovals(res.data.content);
          } else if (Array.isArray(res.data)) {
            setPendingApprovals(res.data);
          } else {
            setPendingApprovals([]);
          }
        } catch (err) {
          console.warn('Could not fetch pending approvals:', err);
          setPendingApprovals([]);
        }
      };

      await Promise.all([fetchEvents(), fetchOrgs(), fetchLocais(), fetchProjetos(), fetchApprovals()]);
    } catch (err: any) {
      console.error('Error fetching data:', err);
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

  const handleScopeSelect = (scope: EditScope) => {
    setSelectedEditScope(scope);
    setIsEditScopeOpen(false);
    setIsDetailsOpen(false);
    setIsCreateOpen(true);
  };

  // Handle event submission (create or update)
  const handleEventSubmit = async (data: any) => {
    setIsSubmitting(true);
    try {
      if (editingEvent) {
        // UPDATE (PATCH)
        const { recurrence, ...eventBaseData } = data;
        const updateData = {
          ...eventBaseData,
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
      console.error('Submit event error:', err);
      let errorMsg = 'Erro ao processar evento. Verifique as regras de negócio.';
      if (err.response?.data) {
        const data = err.response.data;
        if (Array.isArray(data.errors) && data.errors.length > 0) {
          errorMsg = 'Por favor, corrija os seguintes erros:\n' + data.errors.map((e: any) => `• ${e.message || e.defaultMessage}`).join('\n');
        } else if (data.message) {
          errorMsg = data.message;
        }
      }
      showError(errorMsg);
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
    } catch (err: any) {
      console.error('Cancel event error:', err);
      const errMsg = err.response?.data?.message || 'Erro ao cancelar evento.';
      showError(errMsg);
    } finally {
      setIsCancelling(false);
    }
  };

  // Combine API events with transient events from pending creation requests
  const combinedEvents = useMemo(() => {
    // 1. Transient creation events
    const transientEvents = pendingApprovals
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
            conflictState: null,
            conflictReason: null,
            pendingApprovalId: a.id,
            pendingSolicitanteId: a.solicitanteId,
            pendingTipoSolicitacao: a.tipoSolicitacao
          };
        } catch (e) {
          return null;
        }
      })
      .filter((e): e is Evento => e !== null);

    // 2. Decorated events
    const decoratedEvents = events.map((e) => {
      const pendingAction = pendingApprovals.find((a) => a.eventoId === e.id);
      if (pendingAction) {
        return {
          ...e,
          pendingApprovalId: pendingAction.id,
          pendingSolicitanteId: pendingAction.solicitanteId,
          pendingTipoSolicitacao: pendingAction.tipoSolicitacao,
          pendingActionType: pendingAction.tipoSolicitacao
        };
      }
      return e;
    });

    return [...decoratedEvents, ...transientEvents];
  }, [events, pendingApprovals]);

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
      {/* Toolbar / Controls Card */}
      <Card sx={{ mb: 4, borderRadius: 0, boxShadow: '0 4px 20px rgba(0, 0, 0, 0.05)', overflow: 'visible' }}>
        <CardContent sx={{ p: { xs: 2, md: 3 } }}>
          <Grid container spacing={2} sx={{ alignItems: 'center' }}>
            {/* Top Line: Month Navigation (Left) & Action Buttons (Right) */}
            <Grid size={{ xs: 12, sm: 6 }} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <IconButton onClick={handlePrevMonth} color="primary" sx={{ border: '1px solid #e0e0e0' }}>
                <ChevronLeft />
              </IconButton>
              <Typography variant="h5" sx={{ minWidth: 150, textAlign: 'center', fontWeight: 600, color: '#37474f' }}>
                {monthNames[currentMonth]} {currentYear}
              </Typography>
              <IconButton onClick={handleNextMonth} color="primary" sx={{ border: '1px solid #e0e0e0' }}>
                <ChevronRight />
              </IconButton>
              <Button variant="outlined" onClick={handleToday} sx={{ ml: 0.5, textTransform: 'none' }}>
                Hoje
              </Button>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }} sx={{ display: 'flex', justifyContent: { xs: 'flex-start', sm: 'flex-end' }, alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
              <Box sx={{ border: '1px solid #e0e0e0', borderRadius: 0, overflow: 'hidden', display: 'flex' }}>
                <IconButton
                  color={viewMode === 'month' ? 'primary' : 'default'}
                  onClick={() => setViewMode('month')}
                  sx={{ borderRadius: 0, bgcolor: viewMode === 'month' ? '#e8f5e9' : 'transparent' }}
                  title="Visualização Mensal"
                  aria-label="Mês"
                >
                  <CalendarIcon />
                </IconButton>
                <IconButton
                  color={viewMode === 'agenda' ? 'primary' : 'default'}
                  onClick={() => setViewMode('agenda')}
                  sx={{ borderRadius: 0, bgcolor: viewMode === 'agenda' ? '#e8f5e9' : 'transparent' }}
                  title="Visualização em Agenda"
                  aria-label="Lista"
                >
                  <ListIcon />
                </IconButton>
              </Box>
              <Button
                variant="outlined"
                color="primary"
                startIcon={<DownloadIcon />}
                onClick={() => setIsExportOpen(true)}
                sx={{ textTransform: 'none', px: 2, fontWeight: 500, whiteSpace: 'nowrap' }}
              >
                Exportar
              </Button>
              <Button
                variant="contained"
                color="primary"
                startIcon={<AddIcon />}
                onClick={() => handleOpenCreate()}
                sx={{ textTransform: 'none', px: 2, fontWeight: 500, whiteSpace: 'nowrap' }}
              >
                Novo Evento
              </Button>
            </Grid>

            {/* Line Below: Filtering Controls */}
            <Grid size={{ xs: 12 }} sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center', mt: 1, pt: 1, borderTop: '1px solid #f0f0f0' }}>
              <FormControl size="small" sx={{ minWidth: 200, maxWidth: 300, flexGrow: 1 }}>
                <InputLabel id="org-filter-label">Organização</InputLabel>
                <Select
                  labelId="org-filter-label"
                  value={selectedOrgFilter}
                  label="Organização"
                  onChange={(e) => setSelectedOrgFilter(e.target.value)}
                  sx={{ borderRadius: 0 }}
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
                  sx={{ borderRadius: 0 }}
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

              <IconButton onClick={fetchData} disabled={isLoading} title="Recarregar calendário" sx={{ border: '1px solid #e0e0e0' }}>
                {isLoading ? <CircularProgress size={24} /> : <RefreshIcon />}
              </IconButton>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* View Content */}
      {viewMode === 'month' ? (
        /* Monthly Grid View */
         <Paper elevation={2} sx={{ borderRadius: 0, overflow: 'hidden', boxShadow: '0 4px 25px rgba(0,0,0,0.04)' }}>
          {/* Days of week header */}
          <Grid container columns={7} sx={{ bgcolor: '#eceff1', borderBottom: '1px solid #cfd8dc', textAlign: 'center', py: 1.5 }}>
            {['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'].map((day) => (
              <Grid size={{ xs: 1 }} key={day}>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#455a64' }}>
                  {day}
                </Typography>
              </Grid>
            ))}
          </Grid>

          {/* Calendar Grid Cells */}
          <Grid container columns={7}>
            {calendarDays.map(({ date, isCurrentMonth, isToday }, idx) => {
              const dayEvents = getEventsForDay(date);
              return (
                <Grid
                  size={{ xs: 1 }}
                  key={idx}
                  onClick={() => handleOpenCreate(date)}
                  sx={{
                    minHeight: 130,
                    borderRight: '1px solid #e0e0e0',
                    borderBottom: '1px solid #e0e0e0',
                    bgcolor: isToday ? '#f1f8e9' : isCurrentMonth ? 'white' : '#fafafa',
                    p: 1,
                    cursor: 'pointer',
                    transition: 'all 0.15s ease',
                    position: 'relative',
                    '&:hover': {
                      bgcolor: isToday ? '#e8f5e9' : '#f5f5f5',
                      '& .add-day-btn': { opacity: 1 },
                    },
                  }}
                >
                  {/* Day number and indicator */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: isToday ? 700 : 500,
                        color: isToday ? '#2e7d32' : isCurrentMonth ? '#37474f' : '#b0bec5',
                        bgcolor: isToday ? '#c8e6c9' : 'transparent',
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
                    {/* Plus icon on hover */}
                    <AddIcon
                      className="add-day-btn"
                      color="action"
                      sx={{
                        fontSize: '1rem',
                        opacity: 0,
                        transition: 'opacity 0.2s',
                        '&:hover': { color: '#2e7d32' },
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
                            bgcolor: evt.status?.toUpperCase() === 'CANCELADO' ? '#ffebee' : styleColors.bg,
                            color: evt.status?.toUpperCase() === 'CANCELADO' ? '#c62828' : styleColors.text,
                            borderLeft: `3px solid ${evt.status?.toUpperCase() === 'CANCELADO' ? '#ef5350' : styleColors.border}`,
                             borderRadius: 0,
                            px: 1,
                            py: 0.5,
                            mb: 0.5,
                            fontSize: '0.72rem',
                            fontWeight: 600,
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            transition: 'all 0.2s',
                            boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
                            textDecoration: evt.status?.toUpperCase() === 'CANCELADO' ? 'line-through' : 'none',
                            opacity: evt.pendingApprovalId ? 0.6 : (evt.status?.toUpperCase() === 'CANCELADO' ? 0.75 : 1),
                            '&:hover': {
                              filter: 'brightness(0.93)',
                              transform: 'translateX(2px)',
                            },
                          }}
                          title={evt.pendingApprovalId ? `[Pendente] ${evt.titulo}` : evt.titulo}
                        >
                          {evt.pendingApprovalId ? `[Pendente] ${evt.titulo}` : evt.titulo}
                        </Box>
                      );
                    })}
                  </Box>
                </Grid>
              );
            })}
          </Grid>
        </Paper>
      ) : (
        /* Agenda / List View */
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {agendaEvents.length === 0 ? (
             <Paper sx={{ p: 4, textAlign: 'center', borderRadius: 0, color: 'text.secondary' }}>
              <CalendarIcon sx={{ fontSize: 48, color: '#cfd8dc', mb: 2 }} />
              <Typography variant="h6">Nenhum evento agendado para o período visível.</Typography>
            </Paper>
          ) : (
            agendaEvents.map((evt) => {
              const styleColors = getCategoryColor(evt.categoria);
              const start = new Date(evt.inicio);
              const end = new Date(evt.fim);

              return (
                <Card
                  key={evt.id}
                  onClick={() => handleOpenDetails(evt)}
                   sx={{
                    borderRadius: 0,
                    cursor: 'pointer',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.03)',
                    borderLeft: `5px solid ${evt.status?.toUpperCase() === 'CANCELADO' ? '#ef5350' : styleColors.border}`,
                    transition: 'transform 0.2s, box-shadow 0.2s',
                    opacity: evt.pendingApprovalId ? 0.6 : (evt.status?.toUpperCase() === 'CANCELADO' ? 0.8 : 1),
                    '&:hover': {
                      transform: 'translateY(-2px)',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
                    },
                  }}
                >
                  <CardContent sx={{ py: 2, '&:last-child': { pb: 2 } }}>
                    <Grid container spacing={2} sx={{ alignItems: 'center' }}>
                      {/* Event Date badge */}
                      <Grid size={{ xs: 12, sm: 2, md: 1.5 }} sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <Typography variant="h4" sx={{ fontWeight: 700, color: evt.status?.toUpperCase() === 'CANCELADO' ? '#c62828' : '#37474f', lineHeight: 1 }}>
                          {start.getDate()}
                        </Typography>
                        <Typography variant="caption" sx={{ fontWeight: 700, textTransform: 'uppercase', color: evt.status?.toUpperCase() === 'CANCELADO' ? '#ef5350' : '#78909c', mt: 0.5 }}>
                          {monthNames[start.getMonth()].substring(0, 3)}
                        </Typography>
                      </Grid>

                      {/* Title, description & badges */}
                      <Grid size={{ xs: 12, sm: 7, md: 8 }}>
                        <Typography 
                          variant="h6" 
                          sx={{ 
                            fontWeight: 600, 
                            mb: 0.5, 
                            color: evt.status?.toUpperCase() === 'CANCELADO' ? '#c62828' : '#263238',
                            textDecoration: evt.status?.toUpperCase() === 'CANCELADO' ? 'line-through' : 'none'
                          }}
                        >
                          {evt.pendingApprovalId ? `[Pendente] ${evt.titulo}` : evt.titulo}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ lineClamp: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', mb: 1 }}>
                          {evt.descricao || 'Sem descrição.'}
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                          <Chip size="small" label={evt.categoria} sx={{ bgcolor: styleColors.bg, color: styleColors.text, fontWeight: 600 }} />
                          <Chip size="small" variant="outlined" label={getOrgName(evt.organizacaoResponsavelId, evt.igrejaVinculoId)} icon={<BusinessIcon sx={{ fontSize: '0.9rem' }} />} />
                          <Chip size="small" label={evt.status} color={getStatusColor(evt.status)} sx={{ fontWeight: 600 }} />
                        </Box>
                      </Grid>

                      {/* Event time range */}
                      <Grid size={{ xs: 12, sm: 3, md: 2.5 }} sx={{ textAlign: { sm: 'right' } }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: { xs: 'flex-start', sm: 'flex-end' }, gap: 0.5, color: 'text.secondary' }}>
                          <ScheduleIcon sx={{ fontSize: 18 }} />
                          <Typography variant="body2" sx={{ fontWeight: 500 }}>
                            {start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })} -{' '}
                            {end.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                          </Typography>
                        </Box>
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              );
            })
          )}
        </Box>
      )}

      {/* Details Dialog Modal */}
       <Dialog open={isDetailsOpen} onClose={() => setIsDetailsOpen(false)} maxWidth="sm" fullWidth sx={{ '& .MuiDialog-paper': { borderRadius: 0 } }}>
        {selectedEvent && (
          <>
            <DialogTitle sx={{ borderBottom: '1px solid #eee', pb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontWeight: 700, color: '#37474f', fontSize: '1.25rem' }}>
              Detalhes do Evento
              <Chip label={selectedEvent.status} color={getStatusColor(selectedEvent.status)} size="small" sx={{ fontWeight: 600 }} />
            </DialogTitle>
            <DialogContent sx={{ pt: 3 }}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                {selectedEvent.pendingTipoSolicitacao && (
                  <Alert severity="warning" sx={{ borderRadius: 0 }}>
                    {selectedEvent.pendingTipoSolicitacao === 'CRIACAO_EVENTO'
                      ? 'Solicitação de Criação Pendente'
                      : selectedEvent.pendingTipoSolicitacao === 'CANCELAMENTO'
                      ? 'Solicitação de Cancelamento Pendente'
                      : selectedEvent.pendingTipoSolicitacao === 'EDICAO_EVENTO'
                      ? 'Solicitação de Edição Pendente'
                      : 'Solicitação Pendente'}
                  </Alert>
                )}
                {/* Title */}
                <Box>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main', mb: 1 }}>
                    {selectedEvent.titulo}
                  </Typography>
                  <Chip
                    label={selectedEvent.categoria}
                    sx={{
                      bgcolor: getCategoryColor(selectedEvent.categoria).bg,
                      color: getCategoryColor(selectedEvent.categoria).text,
                      fontWeight: 600,
                    }}
                  />
                </Box>

                {/* Date & Time */}
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                  <ScheduleIcon color="action" />
                  <Box>
                    <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                      Horário
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600, color: '#37474f' }}>
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
                    <Typography variant="body1" sx={{ fontWeight: 600, color: '#37474f' }}>
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
                     <Typography variant="body1" sx={{ color: '#546e7a', whiteSpace: 'pre-wrap', bgcolor: '#fafafa', p: 1.5, borderRadius: 0, border: '1px solid #eceff1' }}>
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
                    <Typography variant="body1" sx={{ fontWeight: 600, color: '#37474f' }}>
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
                      <Typography variant="body1" sx={{ fontWeight: 600, color: '#37474f' }}>
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

                {/* Extra Justification */}
                {selectedEvent.conflictReason && (
                  <Alert severity="warning" sx={{ borderRadius: 0 }}>
                    Conflito detectado: {selectedEvent.conflictReason}
                  </Alert>
                )}

                {/* Event Observations */}
                <EventObservations eventoId={selectedEvent.id} />
              </Box>
            </DialogContent>
            <DialogActions sx={{ borderTop: '1px solid #eee', px: 3, py: 2 }}>
              <Button onClick={() => setIsDetailsOpen(false)} color="secondary" sx={{ fontWeight: 600 }}>
                Fechar
              </Button>
              {selectedEvent.status !== 'CANCELADO' && !selectedEvent.pendingApprovalId && canEditEvent(selectedEvent) && (
                <>
                  <Button 
                    onClick={() => handleOpenCancel(selectedEvent)} 
                    variant="outlined" 
                    color="error" 
                    sx={{ fontWeight: 600 }}
                  >
                    Cancelar Evento
                  </Button>
                  <Button 
                    onClick={() => handleOpenEdit(selectedEvent)} 
                    variant="contained" 
                    color="primary" 
                    sx={{ fontWeight: 600 }}
                  >
                    Editar Evento
                  </Button>
                </>
              )}
            </DialogActions>
          </>
        )}
      </Dialog>

      {/* Create/Edit Dialog Modal Component */}
      <EventRegistrationModal
        open={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleEventSubmit}
        isSubmitting={isSubmitting}
        organizations={organizations.filter(org => org.ativo)}
        locais={locais.filter(l => l.ativo)}
        projetos={projetos.filter(p => p.status === 'ABERTO' || p.status === 'EM_ANDAMENTO')}
        initialDate={createInitialDate}
        initialEvent={editingEvent}
      />

      {/* Cancel Event Dialog Modal Component */}
      <EventCancelDialog
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
    </Box>
  );
};

export default Calendar;
