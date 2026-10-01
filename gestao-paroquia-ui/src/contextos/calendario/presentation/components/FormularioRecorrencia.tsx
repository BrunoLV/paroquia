import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { Box, Checkbox, FormControl, FormControlLabel, FormGroup, FormLabel, Grid, MenuItem, Radio, RadioGroup, TextField, Typography, useTheme } from '@mui/material';

export type Frequency = 'DIARIA' | 'SEMANAL' | 'MENSAL';
export type FrequenciaRecorrencia = Frequency;
type TipoTermino = 'NEVER' | 'DATE' | 'COUNT';
export type TerminationType = TipoTermino;

/**
 * Regra de recorrência enviada ao backend para gerar as ocorrências do evento (ver
 * `RegraRecorrencia`/`EventoRecorrenciaEntity`). Os campos são mutuamente exclusivos conforme a
 * combinação frequência + tipo de repetição mensal + condição de término escolhida no formulário:
 * - `diasDaSemana` só se aplica a `SEMANAL` (dias em que o evento se repete) OU a `MENSAL` no
 *   modo "dia da semana relativo" (nesse caso guarda um único dia, junto com `posicaoNoMes`).
 * - `diaDoMes` só existe no modo mensal "dia fixo" (ex.: todo dia 10); é mutuamente exclusivo
 *   com `posicaoNoMes`/`diasDaSemana`.
 * - `posicaoNoMes` (ex.: "PRIMEIRO", "ÚLTIMO") só existe no modo mensal "relativo" (ex.:
 *   primeira terça-feira do mês) e é sempre combinado com um `diasDaSemana` de um único dia.
 * - `dataLimite` e `numeroOcorrencias` são as duas formas de condição de término e também se
 *   excluem mutuamente: a série termina numa data fixa OU após um número de ocorrências, nunca
 *   ambos (ausência dos dois significa recorrência sem fim/"nunca").
 */
export interface RegraRecorrencia {
  frequencia: Frequency;
  intervalo: number;
  diasDaSemana?: string[];
  posicaoNoMes?: string;
  diaDoMes?: number;
  dataLimite?: string;
  numeroOcorrencias?: number;
}

export type RecurrenceRule = RegraRecorrencia;

export interface FormularioRecorrenciaProps {
  rule: RegraRecorrencia;
  onChange: (rule: RegraRecorrencia) => void;
  disabled?: boolean;
}

export type RecurrenceFormProps = FormularioRecorrenciaProps;

export const DIAS_DA_SEMANA = [
  { label: 'Dom', value: 'SUNDAY' },
  { label: 'Seg', value: 'MONDAY' },
  { label: 'Ter', value: 'TUESDAY' },
  { label: 'Qua', value: 'WEDNESDAY' },
  { label: 'Qui', value: 'THURSDAY' },
  { label: 'Sex', value: 'FRIDAY' },
  { label: 'Sáb', value: 'SATURDAY' },
];

export const DAYS_OF_WEEK = DIAS_DA_SEMANA;

export const FormularioRecorrencia: React.FC<FormularioRecorrenciaProps> = ({ rule, onChange, disabled }) => {
  const theme = useTheme();
  const [terminationType, setTerminationType] = React.useState<TipoTermino>(
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

  const handleTerminationChange = (type: TerminationType) => {
    setTerminationType(type);
    // dataLimite e numeroOcorrencias são mutuamente exclusivos na regra final: trocar o tipo de
    // término sempre limpa o campo do modo anterior para não enviar os dois preenchidos ao
    // backend (o que geraria ambiguidade sobre qual condição de parada vale).
    if (type === 'NEVER') {
      onChange({ ...rule, dataLimite: undefined, numeroOcorrencias: undefined });
    } else if (type === 'DATE') {
      onChange({ ...rule, numeroOcorrencias: undefined });
    } else if (type === 'COUNT') {
      onChange({ ...rule, dataLimite: undefined });
    }
  };

  return (
    <Box sx={{ p: 2, bgcolor: theme.custom?.subtlePanelBg ?? '#F8F9FA', border: '1px solid', borderColor: 'divider' }}>
      <Typography variant="subtitle2" sx={{ mb: 2, fontWeight: 600 }}>
        Configurações de Repetição
      </Typography>

      <Grid container spacing={2}>
        <Grid size={{ xs: 6 }}>
          <FormControl fullWidth size="small">

            <SearchableSelect
              labelId="recurrence-frequency-label"
              id="recurrence-frequency-select"
              value={rule.frequencia}
              label="Frequência"
              onChange={(e) => handleFrequencyChange(e.target.value as Frequency)}
              disabled={disabled}
            >
              <MenuItem value="DIARIA">Diária</MenuItem>
              <MenuItem value="SEMANAL">Semanal</MenuItem>
              <MenuItem value="MENSAL">Mensal</MenuItem>
            </SearchableSelect>
          </FormControl>
        </Grid>
        <Grid size={{ xs: 6 }}>
          <TextField
            label={rule.frequencia === 'DIARIA' ? 'A cada (dias)' : rule.frequencia === 'SEMANAL' ? 'A cada (semanas)' : 'A cada (meses)'}
            type="number"
            value={rule.intervalo}
            onChange={(e) => handleIntervalChange(parseInt(e.target.value))}
            fullWidth
            size="small"
            disabled={disabled}
            slotProps={{ htmlInput: { min: 1 } }}
          />
        </Grid>

        {rule.frequencia === 'SEMANAL' && (
          <Grid size={{ xs: 12 }}>
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

        {rule.frequencia === 'MENSAL' && (
          <Grid size={{ xs: 12 }}>
            <FormControl component="fieldset" disabled={disabled} sx={{ width: '100%', mb: 2 }}>
              <FormLabel component="legend" sx={{ fontSize: '0.75rem', mb: 1 }}>Tipo de repetição mensal:</FormLabel>
              <RadioGroup
                row
                // Na ausência de posicaoNoMes/diasDaSemana já preenchidos, assume-se modo FIXED
                // (dia fixo do mês) como padrão — é o caso mais comum de recorrência mensal.
                value={rule.posicaoNoMes || rule.diasDaSemana?.length ? 'RELATIVE' : 'FIXED'}
                onChange={(e) => {
                  if (e.target.value === 'FIXED') {
                    // Alternar para "dia fixo" limpa os campos do modo relativo (posicaoNoMes/
                    // diasDaSemana) para manter a regra consistente com apenas um modo ativo.
                    onChange({
                      ...rule,
                      diaDoMes: rule.diaDoMes || 1,
                      posicaoNoMes: undefined,
                      diasDaSemana: undefined,
                    });
                  } else {
                    // Modo relativo (ex.: "primeira terça-feira do mês"): diaDoMes não se aplica
                    // e diasDaSemana aqui representa um único dia da semana, não uma lista de
                    // dias como no modo SEMANAL.
                    onChange({
                      ...rule,
                      diaDoMes: undefined,
                      posicaoNoMes: rule.posicaoNoMes || 'FIRST',
                      diasDaSemana: rule.diasDaSemana?.length ? rule.diasDaSemana : ['SUNDAY'],
                    });
                  }
                }}
              >
                <FormControlLabel value="FIXED" control={<Radio size="small" />} label={<Typography variant="body2">Dia fixo do mês</Typography>} />
                <FormControlLabel value="RELATIVE" control={<Radio size="small" />} label={<Typography variant="body2">Dia da semana relativo</Typography>} />
              </RadioGroup>
            </FormControl>

            {!(rule.posicaoNoMes || rule.diasDaSemana?.length) ? (
              <TextField
                label="Dia do mês"
                type="number"
                value={rule.diaDoMes || 1}
                onChange={(e) => onChange({ ...rule, diaDoMes: Math.min(31, Math.max(1, parseInt(e.target.value) || 1)) })}
                fullWidth
                size="small"
                disabled={disabled}
                slotProps={{ htmlInput: { min: 1, max: 31 } }}
              />
            ) : (
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">

                    <SearchableSelect
                      labelId="recurrence-position-label"
                      id="recurrence-position-select"
                      value={rule.posicaoNoMes || 'FIRST'}
                      label="Posição"
                      onChange={(e) => onChange({ ...rule, posicaoNoMes: e.target.value })}
                      disabled={disabled}
                    >
                      <MenuItem value="FIRST">Primeiro(a)</MenuItem>
                      <MenuItem value="SECOND">Segundo(a)</MenuItem>
                      <MenuItem value="THIRD">Terceiro(a)</MenuItem>
                      <MenuItem value="FOURTH">Quarto(a)</MenuItem>
                      <MenuItem value="FIFTH">Quinto(a)</MenuItem>
                      <MenuItem value="LAST">Último(a)</MenuItem>
                    </SearchableSelect>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">

                    <SearchableSelect
                      labelId="recurrence-day-label"
                      id="recurrence-day-select"
                      value={rule.diasDaSemana?.[0] || 'SUNDAY'}
                      label="Dia da semana"
                      onChange={(e) => onChange({ ...rule, diasDaSemana: [e.target.value] })}
                      disabled={disabled}
                    >
                      {DAYS_OF_WEEK.map((day) => (
                        <MenuItem key={day.value} value={day.value}>
                          {day.label}
                        </MenuItem>
                      ))}
                    </SearchableSelect>
                  </FormControl>
                </Grid>
              </Grid>
            )}
          </Grid>
        )}

        <Grid size={{ xs: 12 }}>
          <FormControl component="fieldset" disabled={disabled} sx={{ width: '100%' }}>
            <FormLabel component="legend" sx={{ fontSize: '0.75rem', mb: 1 }}>Termina em:</FormLabel>
            <RadioGroup
              value={terminationType}
              onChange={(e) => {
                const type = e.target.value;
                if (type === 'NEVER' || type === 'DATE' || type === 'COUNT') handleTerminationChange(type);
              }}
            >
              <Grid container spacing={1} sx={{ alignItems: 'center' }}>
                <Grid size={{ xs: 4 }}>
                  <FormControlLabel value="NEVER" control={<Radio size="small" />} label={<Typography variant="body2">Nunca</Typography>} />
                </Grid>
                <Grid size={{ xs: 8 }}>
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
                <Grid size={{ xs: 4 }}>
                </Grid>
                <Grid size={{ xs: 8 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <FormControlLabel value="COUNT" control={<Radio size="small" />} label={<Typography variant="body2">Após:</Typography>} />
                    <TextField
                      type="number"
                      size="small"
                      disabled={disabled || terminationType !== 'COUNT'}
                      value={rule.numeroOcorrencias || ''}
                      onChange={(e) => onChange({ ...rule, numeroOcorrencias: parseInt(e.target.value) })}
                      slotProps={{ htmlInput: { min: 1 } }}
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

export const RecurrenceForm = FormularioRecorrencia;
export default FormularioRecorrencia;
