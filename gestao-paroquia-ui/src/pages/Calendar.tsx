import { useState } from 'react';
import { Box } from '@mui/material';
import { Add as AddIcon, CalendarMonth as CalendarIcon, Download as DownloadIcon, Event as EventIcon } from '@mui/icons-material';
import { useAuth } from '../auth/AuthContext';
import CadastroEventoModal from '../components/CadastroEventoModal';
import CancelarEventoDialog from '../components/CancelarEventoDialog';
import EditScopeDialog from '../components/EditScopeDialog';
import { ExportCalendarDialog } from '../components/ExportCalendarDialog';
import { EventoConflitoRecursoDialog } from '../components/EventoConflitoRecursoDialog';
import PageHeader from '../components/PageHeader';
import ActionButton from '../components/ActionButton';
import CalendarToolbar from '../components/calendar/CalendarToolbar';
import CalendarMonthGrid from '../components/calendar/CalendarMonthGrid';
import CalendarAgendaList from '../components/calendar/CalendarAgendaList';
import EventoDetailsDialog from '../components/calendar/EventoDetailsDialog';
import SolenidadesDialog from '../components/calendar/SolenidadesDialog';
import { useCalendarGrid } from '../hooks/useCalendarGrid';
import { useCalendarFilterState } from '../hooks/useCalendarFilterState';
import { useCalendarData } from '../hooks/useCalendarData';
import { useEventoCrud } from '../hooks/useEventoCrud';

/**
 * Página do Calendário Paroquial — orquestrador fino que compõe os hooks de estado/dados
 * (`useCalendarGrid`, `useCalendarFilterState`, `useCalendarData`, `useEventoCrud`) com os
 * subcomponentes de apresentação (`components/calendar/*`). Toda a lógica de negócio (fetch,
 * merge de eventos pendentes, mutações de evento, permissões) vive nos hooks — este arquivo só
 * conecta estado a JSX.
 */
const Calendar = () => {
  useAuth(); // mantém o contexto de auth montado para os hooks filhos que o consultam

  const grid = useCalendarGrid();
  const filterState = useCalendarFilterState();
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [isSolemnitiesOpen, setIsSolemnitiesOpen] = useState(false);

  const startOfGrid = grid.calendarDays[0].date;
  const endOfGrid = grid.calendarDays[grid.calendarDays.length - 1].date;

  const data = useCalendarData({
    startOfGrid,
    endOfGrid,
    selectedOrgFilter: filterState.selectedOrgFilter,
    selectedCategoryFilter: filterState.selectedCategoryFilter,
    currentYear: grid.currentYear,
  });

  const crud = useEventoCrud(data.refetchCalendario);

  return (
    <Box>
      <PageHeader
        icon={<CalendarIcon sx={{ fontSize: 32, color: 'primary.main' }} />}
        title="Calendário Paroquial"
        subtitle="Agendamento de eventos, celebrações e atividades pastorais da paróquia."
        actions={
          <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
            <ActionButton title="Solenidades & Santoral" icon={<EventIcon />} color="secondary" onClick={() => setIsSolemnitiesOpen(true)} />
            <ActionButton title="Exportar" icon={<DownloadIcon />} color="info" onClick={() => setIsExportOpen(true)} />
            <ActionButton title="Novo Evento" icon={<AddIcon />} color="primary" onClick={() => crud.handleOpenCreate()} />
          </Box>
        }
      />

      <CalendarToolbar
        currentMonth={grid.currentMonth}
        currentYear={grid.currentYear}
        onPrevMonth={grid.handlePrevMonth}
        onNextMonth={grid.handleNextMonth}
        onToday={grid.handleToday}
        liturgicalInfo={data.liturgicalInfo}
        viewMode={grid.viewMode}
        onViewModeChange={grid.setViewMode}
        selectedOrgFilter={filterState.selectedOrgFilter}
        onSelectedOrgFilterChange={filterState.setSelectedOrgFilter}
        selectedCategoryFilter={filterState.selectedCategoryFilter}
        onSelectedCategoryFilterChange={filterState.setSelectedCategoryFilter}
        filteredOrganizations={data.filteredOrganizations}
        isLoading={data.isLoading}
        onRefetch={data.refetchCalendario}
      />

      {grid.viewMode === 'month' ? (
        <CalendarMonthGrid
          calendarDays={grid.calendarDays}
          getEventsForDay={data.getEventsForDay}
          onDayClick={crud.handleOpenCreate}
          onEventClick={crud.handleOpenDetails}
        />
      ) : (
        <CalendarAgendaList agendaEvents={data.agendaEvents} onEventClick={crud.handleOpenDetails} getOrgName={data.getOrgName} />
      )}

      <EventoDetailsDialog
        open={crud.isDetailsOpen}
        onClose={() => crud.setIsDetailsOpen(false)}
        event={crud.selectedEvent}
        recorrencia={crud.selectedEventRecorrenciaQuery.data}
        canEdit={crud.selectedEvent ? crud.canEditEvent(crud.selectedEvent) : false}
        onEdit={crud.handleOpenEdit}
        onCancel={crud.handleOpenCancel}
        getOrgName={data.getOrgName}
        getLocalName={data.getLocalName}
      />

      {/* Create/Edit Dialog Modal Component.
          Listas completas (sem filtrar ativo/status/DISPONIVEL): o modal já inclui o valor
          atual do evento mesmo fora do subconjunto "ativo/aberto", senão o Select aparenta
          vazio ao editar um evento vinculado a algo desativado/concluído. */}
      <CadastroEventoModal
        open={crud.isCreateOpen}
        onClose={() => crud.setIsCreateOpen(false)}
        onSubmit={crud.handleEventSubmit}
        isSubmitting={crud.isSubmitting}
        organizations={data.organizations}
        locais={data.locais}
        ativos={data.ativos}
        projetos={data.projetos}
        initialDate={crud.createInitialDate}
        initialEvent={crud.editingEvent}
      />

      <CancelarEventoDialog
        open={crud.isCancelOpen}
        onClose={() => crud.setIsCancelOpen(false)}
        onConfirm={crud.handleCancelConfirm}
        isSubmitting={crud.isCancelling}
        eventTitle={crud.cancellingEvent?.titulo}
      />

      <EditScopeDialog open={crud.isEditScopeOpen} onClose={() => crud.setIsEditScopeOpen(false)} onSelect={crud.handleScopeSelect} />

      <ExportCalendarDialog open={isExportOpen} onClose={() => setIsExportOpen(false)} organizations={data.organizations} />

      <EventoConflitoRecursoDialog
        open={crud.conflitoRecursoModalOpen}
        onClose={() => crud.setConflitoRecursoModalOpen(false)}
        detalhes={crud.conflitoRecursoDetalhes}
        mensagem={crud.conflitoRecursoMensagem}
      />

      <SolenidadesDialog
        open={isSolemnitiesOpen}
        onClose={() => setIsSolemnitiesOpen(false)}
        currentYear={grid.currentYear}
        liturgicalCycle={data.liturgicalInfo.cycle}
        solemnities={data.solemnities}
      />
    </Box>
  );
};

export default Calendar;
