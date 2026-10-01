import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { Box, Card, CardContent, CircularProgress, FormControl, Grid, MenuItem, TextField, Typography } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import ChevronLeft from '@mui/icons-material/ChevronLeft';
import ChevronRight from '@mui/icons-material/ChevronRight';
import DateRangeIcon from '@mui/icons-material/DateRange';
import ListIcon from '@mui/icons-material/FormatListBulleted';
import RefreshIcon from '@mui/icons-material/Refresh';
import TodayIcon from '@mui/icons-material/Today';
import ViewWeekIcon from '@mui/icons-material/ViewWeek';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { CALENDAR_CATEGORIES } from '@/contextos/calendario/presentation/hooks/useCalendarFilterState';
import {
  CalendarViewMode,
  ModoVisualizacaoCalendario,
} from '@/contextos/calendario/presentation/hooks/useCalendarGrid';
import { MONTH_NAMES } from '@/shared/utils/calendarHelpers';
import { Organizacao } from '@/shared/domain/types/types';

interface LiturgicalInfo {
  cycle: string;
  isClosed: boolean;
}

export interface BarraFerramentasCalendarioProps {
  currentMonth: number;
  currentYear: number;
  currentDate?: Date;
  onPrevMonth: () => void;
  onNextMonth: () => void;
  onToday: () => void;
  liturgicalInfo: LiturgicalInfo;
  viewMode: CalendarViewMode;
  onViewModeChange: (mode: ModoVisualizacaoCalendario) => void;
  selectedOrgFilter: string;
  onSelectedOrgFilterChange: (value: string) => void;
  selectedCategoryFilter: string;
  onSelectedCategoryFilterChange: (value: string) => void;
  filteredOrganizations: Organizacao[];
  isLoading: boolean;
  onRefetch: () => void;
  customStartDate?: Date;
  customEndDate?: Date;
  onCustomRangeChange?: (start: Date, end: Date) => void;
}

export type CalendarToolbarProps = BarraFerramentasCalendarioProps;

/**
 * Barra de navegação temporal, alternância entre as 5 modalidades de visualização
 * (HOJE, SEMANA, MÊS, ANO, DIAS ESPECÍFICOS), controles de período customizado,
 * indicadores litúrgicos e filtros organizacionais/temáticos.
 */
export const BarraFerramentasCalendario = ({
  currentMonth,
  currentYear,
  currentDate,
  onPrevMonth,
  onNextMonth,
  onToday,
  liturgicalInfo,
  viewMode,
  onViewModeChange,
  selectedOrgFilter,
  onSelectedOrgFilterChange,
  selectedCategoryFilter,
  onSelectedCategoryFilterChange,
  filteredOrganizations,
  isLoading,
  onRefetch,
  customStartDate,
  customEndDate,
  onCustomRangeChange,
}: CalendarToolbarProps) => {
  // Normalização do modo atual para destacar a opção correta
  const activeMode: ModoVisualizacaoCalendario =
    viewMode === 'month' ? 'MES' : viewMode === 'agenda' ? 'DIAS_ESPECIFICOS' : viewMode === 'HOJE' ? 'DIA' : viewMode;

  // Título dinâmico de acordo com o modo de visualização
  const renderTituloNavegacao = () => {
    switch (activeMode) {
      case 'DIA':
        return currentDate
          ? `${currentDate.getDate()} de ${MONTH_NAMES[currentDate.getMonth()]}, ${currentDate.getFullYear()}`
          : `${MONTH_NAMES[currentMonth]} ${currentYear}`;
      case 'SEMANA':
        return `Semana • ${MONTH_NAMES[currentMonth]} ${currentYear}`;
      case 'MES':
        return `${MONTH_NAMES[currentMonth]} ${currentYear}`;
      case 'ANO':
        return `Ano ${currentYear}`;
      case 'DIAS_ESPECIFICOS':
        return customStartDate && customEndDate
          ? `${customStartDate.toLocaleDateString('pt-BR')} a ${customEndDate.toLocaleDateString('pt-BR')}`
          : `${MONTH_NAMES[currentMonth]} ${currentYear}`;
    }
  };

  const handleStartDateChange = (val: string) => {
    if (!val || !onCustomRangeChange || !customEndDate) return;
    const [y, m, d] = val.split('-').map(Number);
    const newStart = new Date(y, m - 1, d, 0, 0, 0);
    if (newStart <= customEndDate) {
      onCustomRangeChange(newStart, customEndDate);
    }
  };

  const handleEndDateChange = (val: string) => {
    if (!val || !onCustomRangeChange || !customStartDate) return;
    const [y, m, d] = val.split('-').map(Number);
    const newEnd = new Date(y, m - 1, d, 23, 59, 59);
    if (newEnd >= customStartDate) {
      onCustomRangeChange(customStartDate, newEnd);
    }
  };

  const handlePresetDays = (days: number) => {
    if (!onCustomRangeChange) return;
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const end = new Date(start);
    end.setDate(end.getDate() + days - 1);
    end.setHours(23, 59, 59, 999);
    onCustomRangeChange(start, end);
  };

  const formatInputDate = (date?: Date) => {
    if (!date) return '';
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  };

  return (
    <Card
      elevation={0}
      sx={{
        p: 1,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        backgroundColor: 'action.hover',
        overflow: 'visible',
      }}
    >
      <CardContent sx={{ p: { xs: 2, md: 3 } }}>
        <Grid container spacing={2} sx={{ alignItems: 'center' }}>
          {/* Linha Superior Esquerda: Controles de Navegação Temporal */}
          <Grid size={{ xs: 12, xl: 7 }} sx={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 1 }}>
            <ActionButton
              label="Anterior"
              title="Período anterior"
              icon={<ChevronLeft />}
              onClick={onPrevMonth}
              color="primary"
            />

            <Typography
              variant="h5"
              component="h2"
              sx={{ minWidth: 160, textAlign: 'center', fontWeight: 700, color: 'text.primary', px: 1 }}
            >
              {renderTituloNavegacao()}
            </Typography>

            <ActionButton
              label="Próximo"
              title="Próximo período"
              icon={<ChevronRight />}
              onClick={onNextMonth}
              color="primary"
            />

            <ActionButton
              label="Hoje"
              title="Ir para Mês Atual"
              ariaLabel="Ir para Mês Atual"
              icon={<TodayIcon />}
              onClick={onToday}
              color="primary"
            />

            <StatusIcon
              label={`Ano Litúrgico: ${liturgicalInfo.cycle}`}
              color="primary"
              icon={<CalendarIcon />}
              tooltip="Ciclo litúrgico corrente"
            />

            {liturgicalInfo.isClosed && (
              <StatusIcon
                label="ANO FECHADO"
                color="error"
                icon={<BlockIcon />}
                tooltip="Ano paroquial fechado para auditoria"
              />
            )}
          </Grid>

          {/* Linha Superior Direita: Seletor das 5 Formas de Visualização */}
          <Grid
            size={{ xs: 12, xl: 5 }}
            sx={{
              display: 'flex',
              justifyContent: { xs: 'flex-start', xl: 'flex-end' },
              alignItems: 'center',
              gap: 0.5,
              flexWrap: 'wrap',
            }}
          >
            <Box
              sx={{
                border: '1px solid',
                borderColor: 'divider',
                borderRadius: 2,
                overflow: 'hidden',
                display: 'flex',
                flexWrap: 'wrap',
                bgcolor: 'background.paper',
              }}
            >
              <ActionButton
                label="Dia"
                title="Visualização diária (Timeline)"
                ariaLabel="Dia"
                icon={<TodayIcon sx={{ fontSize: 18 }} />}
                color="primary"
                variant="text"
                onClick={() => onViewModeChange('DIA')}
                sx={{
                  border: 'none',
                  borderRadius: 0,
                  bgcolor: activeMode === 'DIA' ? (theme) => theme.custom.navigationSelectedBg : 'transparent',
                  fontWeight: activeMode === 'DIA' ? 700 : 500,
                }}
              />

              <ActionButton
                label="Semana"
                title="Visualização semanal"
                ariaLabel="Semana"
                icon={<ViewWeekIcon sx={{ fontSize: 18 }} />}
                color="primary"
                variant="text"
                onClick={() => onViewModeChange('SEMANA')}
                sx={{
                  border: 'none',
                  borderRadius: 0,
                  bgcolor: activeMode === 'SEMANA' ? (theme) => theme.custom.navigationSelectedBg : 'transparent',
                  fontWeight: activeMode === 'SEMANA' ? 700 : 500,
                }}
              />

              <ActionButton
                label="Mês"
                title="Visualização em grade (Mês)"
                ariaLabel="Mês"
                icon={<CalendarIcon sx={{ fontSize: 18 }} />}
                color="primary"
                variant="text"
                onClick={() => onViewModeChange('MES')}
                sx={{
                  border: 'none',
                  borderRadius: 0,
                  bgcolor: activeMode === 'MES' ? (theme) => theme.custom.navigationSelectedBg : 'transparent',
                  fontWeight: activeMode === 'MES' ? 700 : 500,
                }}
              />

              <ActionButton
                label="Ano"
                title="Visualização anual"
                ariaLabel="Ano"
                icon={<DateRangeIcon sx={{ fontSize: 18 }} />}
                color="primary"
                variant="text"
                onClick={() => onViewModeChange('ANO')}
                sx={{
                  border: 'none',
                  borderRadius: 0,
                  bgcolor: activeMode === 'ANO' ? (theme) => theme.custom.navigationSelectedBg : 'transparent',
                  fontWeight: activeMode === 'ANO' ? 700 : 500,
                }}
              />

              <ActionButton
                label="Dias Específicos"
                title="Visualização em agenda de dias específicos"
                ariaLabel="Dias Específicos (Visualização em Agenda)"
                icon={<ListIcon sx={{ fontSize: 18 }} />}
                color="primary"
                variant="text"
                onClick={() => onViewModeChange('DIAS_ESPECIFICOS')}
                sx={{
                  border: 'none',
                  borderRadius: 0,
                  bgcolor:
                    activeMode === 'DIAS_ESPECIFICOS'
                      ? (theme) => theme.custom.navigationSelectedBg
                      : 'transparent',
                  fontWeight: activeMode === 'DIAS_ESPECIFICOS' ? 700 : 500,
                }}
              />
            </Box>
          </Grid>

          {/* Linha Intermediária: Controles de Intervalo Personalizado (exibida apenas no modo DIAS_ESPECIFICOS) */}
          {activeMode === 'DIAS_ESPECIFICOS' && (
            <Grid
              size={{ xs: 12 }}
              sx={{
                display: 'flex',
                gap: 1.5,
                flexWrap: 'wrap',
                alignItems: 'center',
                pt: 1,
                borderTop: '1px solid',
                borderColor: 'divider',
              }}
            >
              <TextField
                size="small"
                type="date"
                label="Data Inicial"
                value={formatInputDate(customStartDate)}
                onChange={(e) => handleStartDateChange(e.target.value)}
                slotProps={{ inputLabel: { shrink: true } }}
                sx={{ minWidth: 160 }}
              />

              <TextField
                size="small"
                type="date"
                label="Data Final"
                value={formatInputDate(customEndDate)}
                onChange={(e) => handleEndDateChange(e.target.value)}
                slotProps={{ inputLabel: { shrink: true } }}
                sx={{ minWidth: 160 }}
              />

              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                  Atalhos:
                </Typography>
                <ActionButton
                  label="7 dias"
                  title="Selecionar próximos 7 dias"
                  icon={<DateRangeIcon sx={{ fontSize: 16 }} />}
                  variant="outlined"
                  size="small"
                  onClick={() => handlePresetDays(7)}
                />
                <ActionButton
                  label="15 dias"
                  title="Selecionar próximos 15 dias"
                  icon={<DateRangeIcon sx={{ fontSize: 16 }} />}
                  variant="outlined"
                  size="small"
                  onClick={() => handlePresetDays(15)}
                />
                <ActionButton
                  label="30 dias"
                  title="Selecionar próximos 30 dias"
                  icon={<DateRangeIcon sx={{ fontSize: 16 }} />}
                  variant="outlined"
                  size="small"
                  onClick={() => handlePresetDays(30)}
                />
              </Box>
            </Grid>
          )}

          {/* Linha Inferior: Filtros de Organização e Categoria */}
          <Grid
            size={{ xs: 12 }}
            sx={{
              display: 'flex',
              gap: 2,
              flexWrap: 'wrap',
              alignItems: 'center',
              mt: 0.5,
              pt: 1,
              borderTop: '1px solid',
              borderColor: 'divider',
            }}
          >
            <FormControl size="small" sx={{ minWidth: 200, maxWidth: 300, flexGrow: 1 }}>

              <SearchableSelect
                labelId="org-filter-label"
                value={selectedOrgFilter}
                label="Organização"
                onChange={(e) => onSelectedOrgFilterChange(e.target.value)}
              >
                <MenuItem value="">
                  <em>Todas</em>
                </MenuItem>
                {filteredOrganizations.map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome}
                  </MenuItem>
                ))}
              </SearchableSelect>
            </FormControl>

            <FormControl size="small" sx={{ minWidth: 180, maxWidth: 260, flexGrow: 1 }}>

              <SearchableSelect
                labelId="cat-filter-label"
                value={selectedCategoryFilter}
                label="Categoria"
                onChange={(e) => onSelectedCategoryFilterChange(e.target.value)}
              >
                <MenuItem value="">
                  <em>Todas</em>
                </MenuItem>
                {CALENDAR_CATEGORIES.map((cat) => (
                  <MenuItem key={cat} value={cat}>
                    {cat}
                  </MenuItem>
                ))}
              </SearchableSelect>
            </FormControl>

            <ActionButton
              title="Recarregar dados do calendário"
              icon={isLoading ? <CircularProgress size={24} /> : <RefreshIcon />}
              onClick={onRefetch}
              disabled={isLoading}
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export const CalendarToolbar = BarraFerramentasCalendario;
export default BarraFerramentasCalendario;
