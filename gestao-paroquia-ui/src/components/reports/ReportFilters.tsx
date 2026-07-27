import React, {useEffect, useState} from 'react';
import {Box, Button, Stack, TextField} from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff';

interface ReportFiltersProps {
  startDate: string;
  endDate: string;
  onApplyFilters: (startDate: string, endDate: string) => void;
  onClearFilters: () => void;
}

const ReportFilters: React.FC<ReportFiltersProps> = ({
  startDate,
  endDate,
  onApplyFilters,
  onClearFilters,
}) => {
  const [localStartDate, setLocalStartDate] = useState(startDate);
  const [localEndDate, setLocalEndDate] = useState(endDate);

  useEffect(() => {
    setLocalStartDate(startDate);
    setLocalEndDate(endDate);
  }, [startDate, endDate]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onApplyFilters(localStartDate, localEndDate);
  };

  const handleClear = () => {
    setLocalStartDate('');
    setLocalEndDate('');
    onClearFilters();
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
        <TextField
          label="Data Início"
          type="date"
          value={localStartDate || ''}
          onChange={(e) => setLocalStartDate(e.target.value)}
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
          value={localEndDate || ''}
          onChange={(e) => setLocalEndDate(e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
          size="small"
          sx={{
            minWidth: 160,
            '& .MuiOutlinedInput-root': { borderRadius: 0 },
          }}
        />
        <Button
          type="submit"
          variant="contained"
          size="small"
          startIcon={<FilterListIcon />}
          sx={{
            borderRadius: 0,
            backgroundColor: '#1a237e',
            textTransform: 'none',
            fontWeight: 600,
            height: 40,
            px: 2.5,
            '&:hover': {
              backgroundColor: '#0d47a1',
            },
          }}
        >
          Filtrar
        </Button>
        <Button
          type="button"
          variant="outlined"
          color="secondary"
          size="small"
          onClick={handleClear}
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
