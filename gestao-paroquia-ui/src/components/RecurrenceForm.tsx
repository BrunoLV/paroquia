import React from 'react';
import {
  Box,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Grid,
  Checkbox,
  FormGroup,
  FormControlLabel,
  RadioGroup,
  Radio,
  FormLabel,
} from '@mui/material';

export type Frequency = 'DIARIA' | 'SEMANAL' | 'MENSAL';

export interface RecurrenceRule {
  frequencia: Frequency;
  intervalo: number;
  diasDaSemana?: string[];
  posicaoNoMes?: string;
  dataLimite?: string;
  numeroOcorrencias?: number;
}

interface RecurrenceFormProps {
  rule: RecurrenceRule;
  onChange: (rule: RecurrenceRule) => void;
  disabled?: boolean;
}

const DAYS_OF_WEEK = [
  { label: 'Dom', value: 'SUNDAY' },
  { label: 'Seg', value: 'MONDAY' },
  { label: 'Ter', value: 'TUESDAY' },
  { label: 'Qua', value: 'WEDNESDAY' },
  { label: 'Qui', value: 'THURSDAY' },
  { label: 'Sex', value: 'FRIDAY' },
  { label: 'Sáb', value: 'SATURDAY' },
];

const RecurrenceForm: React.FC<RecurrenceFormProps> = ({ rule, onChange, disabled }) => {
  const [terminationType, setTerminationType] = React.useState<'NEVER' | 'DATE' | 'COUNT'>(
    rule.dataLimite ? 'DATE' : rule.numeroOcorrencias ? 'COUNT' : 'NEVER'
  );

  const handleFrequencyChange = (freq: Frequency) => {
    onChange({ ...rule, frequencia: freq });
  };

  const handleIntervalChange = (val: number) => {
    onChange({ ...rule, intervalo: Math.max(1, val) });
  };

  const handleDayToggle = (day: string) => {
    const currentDays = rule.diasDaSemana || [];
    const newDays = currentDays.includes(day)
      ? currentDays.filter((d) => d !== day)
      : [...currentDays, day];
    onChange({ ...rule, diasDaSemana: newDays });
  };

  const handleTerminationChange = (type: 'NEVER' | 'DATE' | 'COUNT') => {
    setTerminationType(type);
    if (type === 'NEVER') {
      onChange({ ...rule, dataLimite: undefined, numeroOcorrencias: undefined });
    } else if (type === 'DATE') {
      onChange({ ...rule, numeroOcorrencias: undefined });
    } else if (type === 'COUNT') {
      onChange({ ...rule, dataLimite: undefined });
    }
  };

  return (
    <Box sx={{ p: 2, bgcolor: '#f8f9fa', borderRadius: '8px', border: '1px solid #e0e0e0' }}>
      <Typography variant="subtitle2" sx={{ mb: 2, fontWeight: 600, color: '#455a64' }}>
        Configurações de Repetição
      </Typography>

      <Grid container spacing={2}>
        <Grid item xs={6}>
          <FormControl fullWidth size="small">
            <InputLabel>Frequência</InputLabel>
            <Select
              value={rule.frequencia}
              label="Frequência"
              onChange={(e) => handleFrequencyChange(e.target.value as Frequency)}
              disabled={disabled}
            >
              <MenuItem value="DIARIA">Diária</MenuItem>
              <MenuItem value="SEMANAL">Semanal</MenuItem>
              <MenuItem value="MENSAL">Mensal</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={6}>
          <TextField
            label={rule.frequencia === 'DIARIA' ? 'A cada (dias)' : rule.frequencia === 'SEMANAL' ? 'A cada (semanas)' : 'A cada (meses)'}
            type="number"
            value={rule.intervalo}
            onChange={(e) => handleIntervalChange(parseInt(e.target.value))}
            fullWidth
            size="small"
            disabled={disabled}
            inputProps={{ min: 1 }}
          />
        </Grid>

        {rule.frequencia === 'SEMANAL' && (
          <Grid item xs={12}>
            <FormLabel component="legend" sx={{ fontSize: '0.75rem', mb: 0.5 }}>Repetir em:</FormLabel>
            <FormGroup row sx={{ justifyContent: 'space-between' }}>
              {DAYS_OF_WEEK.map((day) => (
                <FormControlLabel
                  key={day.value}
                  control={
                    <Checkbox
                      size="small"
                      checked={(rule.diasDaSemana || []).includes(day.value)}
                      onChange={() => handleDayToggle(day.value)}
                      disabled={disabled}
                    />
                  }
                  label={<Typography variant="caption">{day.label}</Typography>}
                  sx={{ mr: 1 }}
                />
              ))}
            </FormGroup>
          </Grid>
        )}

        <Grid item xs={12}>
          <FormControl component="fieldset" disabled={disabled}>
            <FormLabel component="legend" sx={{ fontSize: '0.75rem', mb: 1 }}>Termina em:</FormLabel>
            <RadioGroup
              value={terminationType}
              onChange={(e) => handleTerminationChange(e.target.value as any)}
            >
              <Grid container spacing={1} alignItems="center">
                <Grid item xs={4}>
                  <FormControlLabel value="NEVER" control={<Radio size="small" />} label={<Typography variant="body2">Nunca</Typography>} />
                </Grid>
                <Grid item xs={8}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <FormControlLabel value="DATE" control={<Radio size="small" />} label={<Typography variant="body2">Em:</Typography>} />
                    <TextField
                      type="date"
                      size="small"
                      disabled={disabled || terminationType !== 'DATE'}
                      value={rule.dataLimite || ''}
                      onChange={(e) => onChange({ ...rule, dataLimite: e.target.value })}
                      slotProps={{ inputLabel: { shrink: true } }}
                      sx={{ flexGrow: 1 }}
                    />
                  </Box>
                </Grid>
                <Grid item xs={4}>
                </Grid>
                <Grid item xs={8}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <FormControlLabel value="COUNT" control={<Radio size="small" />} label={<Typography variant="body2">Após:</Typography>} />
                    <TextField
                      type="number"
                      size="small"
                      disabled={disabled || terminationType !== 'COUNT'}
                      value={rule.numeroOcorrencias || ''}
                      onChange={(e) => onChange({ ...rule, numeroOcorrencias: parseInt(e.target.value) })}
                      inputProps={{ min: 1 }}
                      sx={{ flexGrow: 1 }}
                    />
                    <Typography variant="body2">ocorrências</Typography>
                  </Box>
                </Grid>
              </Grid>
            </RadioGroup>
          </FormControl>
        </Grid>
      </Grid>
    </Box>
  );
};

export default RecurrenceForm;
