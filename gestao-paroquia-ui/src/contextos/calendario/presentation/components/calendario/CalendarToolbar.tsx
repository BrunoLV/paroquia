import {
  alpha,
  Box,
  Card,
  CardContent,
  CircularProgress,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Typography,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import ChevronLeft from '@mui/icons-material/ChevronLeft';
import ChevronRight from '@mui/icons-material/ChevronRight';
import ListIcon from '@mui/icons-material/FormatListBulleted';
import RefreshIcon from '@mui/icons-material/Refresh';
import TodayIcon from '@mui/icons-material/Today';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { CALENDAR_CATEGORIES } from '@/contextos/calendario/presentation/hooks/useCalendarFilterState';
import { MONTH_NAMES } from '@/shared/utils/calendarHelpers';
import { Organizacao } from '@/shared/domain/types/types';

interface LiturgicalInfo {
  cycle: string;
  isClosed: boolean;
}

interface CalendarToolbarProps {
  currentMonth: number;
  currentYear: number;
  onPrevMonth: () => void;
  onNextMonth: () => void;
  onToday: () => void;
  liturgicalInfo: LiturgicalInfo;
  viewMode: 'month' | 'agenda';
  onViewModeChange: (mode: 'month' | 'agenda') => void;
  selectedOrgFilter: string;
  onSelectedOrgFilterChange: (value: string) => void;
  selectedCategoryFilter: string;
  onSelectedCategoryFilterChange: (value: string) => void;
  filteredOrganizations: Organizacao[];
  isLoading: boolean;
  onRefetch: () => void;
}

/**
 * Barra de navegação de mês, indicadores do ano litúrgico, alternância de visualização
 * (grade/agenda) e filtros de organização/categoria do calendário. Puramente apresentacional —
 * todo o estado vem de `useCalendarGrid`/`useCalendarFilterState`/`useCalendarData` no `Calendario.tsx`.
 */
const CalendarToolbar = ({
  currentMonth,
  currentYear,
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
}: CalendarToolbarProps) => (
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
          <ActionButton label="Anterior" title="Mês anterior" icon={<ChevronLeft />} onClick={onPrevMonth} color="primary" />
          <Typography variant="h5" sx={{ minWidth: 130, textAlign: 'center', fontWeight: 600, color: 'text.primary' }}>
            {MONTH_NAMES[currentMonth]} {currentYear}
          </Typography>
          <ActionButton label="Próximo" title="Próximo mês" icon={<ChevronRight />} onClick={onNextMonth} color="primary" />
          <ActionButton label="Hoje" title="Ir para Mês Atual" icon={<TodayIcon />} onClick={onToday} color="primary" />
          <StatusIcon label={`Ano Litúrgico: ${liturgicalInfo.cycle}`} color="primary" icon={<CalendarIcon />} tooltip="Ciclo litúrgico corrente" />
          {liturgicalInfo.isClosed && (
            <StatusIcon label="ANO FECHADO" color="error" icon={<BlockIcon />} tooltip="Ano paroquial fechado para auditoria" />
          )}
        </Grid>

        <Grid size={{ xs: 12, lg: 4 }} sx={{ display: 'flex', justifyContent: { xs: 'flex-start', lg: 'flex-end' }, alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
          <Box sx={{ border: '1px solid', borderColor: 'divider', overflow: 'hidden', display: 'flex' }}>
            <ActionButton
              label="Grade"
              title="Visualização em grade"
              ariaLabel="Mês"
              icon={<CalendarIcon />}
              color="primary"
              variant="text"
              onClick={() => onViewModeChange('month')}
              sx={{ border: 'none', bgcolor: viewMode === 'month' ? (theme) => alpha(theme.palette.primary.main, 0.1) : 'transparent' }}
            />
            <ActionButton
              label="Agenda"
              title="Visualização em agenda"
              icon={<ListIcon />}
              color="primary"
              variant="text"
              onClick={() => onViewModeChange('agenda')}
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
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 180, maxWidth: 260, flexGrow: 1 }}>
            <InputLabel id="cat-filter-label">Categoria</InputLabel>
            <Select
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
            </Select>
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

export default CalendarToolbar;
