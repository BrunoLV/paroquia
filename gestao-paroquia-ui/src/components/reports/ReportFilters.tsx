import React from 'react';
import { Box, TextField, Button, Stack } from '@mui/material';
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff';

interface ReportFiltersProps {
  startDate: string;
  endDate: string;
  onStartDateChange: (val: string) => void;
  onEndDateChange: (val: string) => void;
  onClearFilters: () => void;
}

const ReportFilters: React.FC<ReportFiltersProps> = ({
  startDate,
  endDate,
  onStartDateChange,
  onEndDateChange,
  onClearFilters,
}) => {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
        <TextField
          label="Data Início"
          type="date"
          value={startDate}
          onChange={(e) => onStartDateChange(e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
          size="small"
          sx={{
            minWidth: 160,
            '& .MuiOutlinedInput-root': { borderRadius: 0 },
          }}
        />
        <TextField
          label="Data Fim"
          type="date"
          value={endDate}
          onChange={(e) => onEndDateChange(e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
          size="small"
          sx={{
            minWidth: 160,
            '& .MuiOutlinedInput-root': { borderRadius: 0 },
          }}
        />
        <Button
          variant="outlined"
          color="secondary"
          size="small"
          onClick={onClearFilters}
          startIcon={<FilterAltOffIcon />}
          sx={{
            borderRadius: 0,
            textTransform: 'none',
            fontWeight: 600,
            height: 40,
          }}
        >
          Limpar Filtros
        </Button>
      </Stack>
    </Box>
  );
};

export default ReportFilters;
