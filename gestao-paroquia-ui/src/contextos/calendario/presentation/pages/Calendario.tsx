import { useState } from 'react';
import { Box } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import DownloadIcon from '@mui/icons-material/Download';
import EventIcon from '@mui/icons-material/Event';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import CadastroEventoModal from '@/contextos/calendario/presentation/components/CadastroEventoModal';
import CancelarEventoDialog from '@/contextos/calendario/presentation/components/CancelarEventoDialog';
import DialogEscopoEdicao from '@/contextos/calendario/presentation/components/DialogEscopoEdicao';
import { DialogExportarCalendario } from '@/contextos/calendario/presentation/components/DialogExportarCalendario';
import { EventoConflitoRecursoDialog } from '@/contextos/calendario/presentation/components/EventoConflitoRecursoDialog';
import PageHeader from '@/shared/presentation/components/PageHeader';
import ActionButton from '@/shared/presentation/components/ActionButton';
import BarraFerramentasCalendario from '@/contextos/calendario/presentation/components/calendario/BarraFerramentasCalendario';
import GradeMesCalendario from '@/contextos/calendario/presentation/components/calendario/GradeMesCalendario';
import VisualizacaoHojeCalendario from '@/contextos/calendario/presentation/components/calendario/VisualizacaoHojeCalendario';
import VisualizacaoSemanaCalendario from '@/contextos/calendario/presentation/components/calendario/VisualizacaoSemanaCalendario';
import VisualizacaoAnoCalendario from '@/contextos/calendario/presentation/components/calendario/VisualizacaoAnoCalendario';
import VisualizacaoDiasEspecificosCalendario from '@/contextos/calendario/presentation/components/calendario/VisualizacaoDiasEspecificosCalendario';
import DialogDetalhesEvento from '@/contextos/calendario/presentation/components/calendario/DialogDetalhesEvento';
import SolenidadesDialog from '@/contextos/calendario/presentation/components/calendario/SolenidadesDialog';
import { useCalendarGrid } from '@/contextos/calendario/presentation/hooks/useCalendarGrid';
import { useCalendarFilterState } from '@/contextos/calendario/presentation/hooks/useCalendarFilterState';
import { useCalendarData } from '@/contextos/calendario/presentation/hooks/useCalendarData';
import { useEventoCrud } from '@/contextos/calendario/presentation/hooks/useEventoCrud';

/**
 * Página do Calendário Paroquial — orquestrador que compõe os hooks de estado/dados
 * (`useCalendarGrid`, `useCalendarFilterState`, `useCalendarData`, `useEventoCrud`) com as
 * 5 modalidades de visualização (HOJE, SEMANA, MÊS, ANO, DIAS ESPECÍFICOS).
 */
const Calendario = () => {
  useAuth(); // mantém o contexto de auth montado para os hooks filhos que o consultam

  const grid = useCalendarGrid();
  const filterState = useCalendarFilterState();
  const [isExportOpen, setIsExportOpen] = useState(false);
  const [isSolemnitiesOpen, setIsSolemnitiesOpen] = useState(false);

  const monthGridStart = grid.calendarDays[0].date;
  const monthGridEnd = grid.calendarDays[grid.calendarDays.length - 1].date;

  const startOfGrid = grid.viewMode === 'ANO'
    ? new Date(grid.currentYear, 0, 1, 0, 0, 0)
    : grid.viewMode === 'DIAS_ESPECIFICOS'
    ? (grid.customStartDate < monthGridStart ? grid.customStartDate : monthGridStart)
    : monthGridStart;

  const endOfGrid = grid.viewMode === 'ANO'
    ? new Date(grid.currentYear, 11, 31, 23, 59, 59)
    : grid.viewMode === 'DIAS_ESPECIFICOS'
    ? (grid.customEndDate > monthGridEnd ? grid.customEndDate : monthGridEnd)
    : monthGridEnd;

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
              onClick={() => crud.handleOpenCreate()}
            />
          </Box>
        }
      />

      <BarraFerramentasCalendario
        currentMonth={grid.currentMonth}
        currentYear={grid.currentYear}
        currentDate={grid.currentDate}
        onPrevMonth={grid.handlePrev}
        onNextMonth={grid.handleNext}
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
        customStartDate={grid.customStartDate}
        customEndDate={grid.customEndDate}
        onCustomRangeChange={grid.setCustomRange}
      />

      {/* Renderização Condicional da Modalidade Ativa */}
      {(grid.viewMode === 'DIA' || grid.viewMode === 'HOJE') && (
        <VisualizacaoHojeCalendario
          currentDate={grid.currentDate}
          events={data.getEventsForDay(grid.currentDate)}
          onDayClick={crud.handleOpenCreate}
          onEventClick={crud.handleOpenDetails}
          getOrgName={data.getOrgName}
          getLocalName={data.getLocalName}
        />
      )}

      {grid.viewMode === 'SEMANA' && (
        <VisualizacaoSemanaCalendario
          weekDays={grid.weekDays}
          getEventsForDay={data.getEventsForDay}
          onDayClick={crud.handleOpenCreate}
          onDaySelect={(date) => {
            grid.setCurrentDate(date);
            grid.setViewMode('DIA');
          }}
          onEventClick={crud.handleOpenDetails}
        />
      )}

      {grid.viewMode === 'MES' && (
        <GradeMesCalendario
          calendarDays={grid.calendarDays}
          getEventsForDay={data.getEventsForDay}
          onDayClick={crud.handleOpenCreate}
          onDaySelect={(date) => {
            grid.setCurrentDate(date);
            grid.setViewMode('DIA');
          }}
          onEventClick={crud.handleOpenDetails}
        />
      )}

      {grid.viewMode === 'ANO' && (
        <VisualizacaoAnoCalendario
          currentYear={grid.currentYear}
          getEventsForDay={data.getEventsForDay}
          combinedEvents={data.combinedEvents}
          onDayClick={(date) => {
            grid.setCurrentDate(date);
            grid.setViewMode('DIA');
          }}
          onEventClick={crud.handleOpenDetails}
          onMonthSelect={(monthIndex) => {
            grid.setCurrentDate(new Date(grid.currentYear, monthIndex, 1));
            grid.setViewMode('MES');
          }}
        />
      )}

      {grid.viewMode === 'DIAS_ESPECIFICOS' && (
        <VisualizacaoDiasEspecificosCalendario
          startDate={grid.customStartDate}
          endDate={grid.customEndDate}
          agendaEvents={data.agendaEvents}
          onEventClick={crud.handleOpenDetails}
          onDayClick={crud.handleOpenCreate}
          getOrgName={data.getOrgName}
          getLocalName={data.getLocalName}
        />
      )}

      <DialogDetalhesEvento
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

      <DialogEscopoEdicao
        open={crud.isEditScopeOpen}
        onClose={() => crud.setIsEditScopeOpen(false)}
        onSelect={crud.handleScopeSelect}
      />

      <DialogExportarCalendario
        open={isExportOpen}
        onClose={() => setIsExportOpen(false)}
        organizations={data.organizations}
      />

      <EventoConflitoRecursoDialog
        open={crud.conflitoRecursoModalOpen}
        onClose={() => crud.setConflitoRecursoModalOpen(false)}
        detalhes={crud.conflitoRecursoDetalhes}
        mensagem={crud.conflitoRecursoMensagem}
        onConfirmarDiscussao={crud.handleConfirmarDiscussaoConselho}
        isSubmitting={crud.isSubmitting}
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

export default Calendario;
