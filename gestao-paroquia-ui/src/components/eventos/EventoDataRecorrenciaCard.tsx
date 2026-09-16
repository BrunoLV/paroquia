import React from 'react';
import { Box, Card, CardContent, Collapse, FormControlLabel, Grid, Stack, Switch, TextField, Typography } from '@mui/material';
import RepeatIcon from '@mui/icons-material/Repeat';
import { Controller, type UseFormReturn } from 'react-hook-form';
import RecurrenceForm from '@/components/RecurrenceForm';
import { EventoFormInput } from '@/schemas/eventoSchema';

interface EventoDataRecorrenciaCardProps {
  form: UseFormReturn<EventoFormInput>;
  disabled: boolean;
  isRecurring: boolean;
  isEditing: boolean;
  isParoco: boolean;
}

/** Seção "Data e recorrência": janela do evento, regra de repetição (só na criação) e bypass de conflito. */
const EventoDataRecorrenciaCard: React.FC<EventoDataRecorrenciaCardProps> = ({ form, disabled, isRecurring, isEditing, isParoco }) => {
  const { register, control } = form;
  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <RepeatIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Data e recorrência
          </Typography>
        </Stack>
        <Grid container spacing={2}>
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Data e Hora de Início"
              type="datetime-local"
              required
              slotProps={{ inputLabel: { shrink: true } }}
              fullWidth
              disabled={disabled}
              size="small"
              {...register('startDate')}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            {isRecurring ? (
              <TextField label="Hora de Término" type="time" required slotProps={{ inputLabel: { shrink: true } }} fullWidth disabled={disabled} size="small" {...register('endTime')} />
            ) : (
              <TextField label="Data e Hora de Término" type="datetime-local" required slotProps={{ inputLabel: { shrink: true } }} fullWidth disabled={disabled} size="small" {...register('endDate')} />
            )}
          </Grid>

          {!isEditing && (
            <>
              <Grid size={{ xs: 12 }}>
                <Controller
                  name="isRecurring"
                  control={control}
                  render={({ field }) => (
                    <>
                      <FormControlLabel control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} disabled={disabled} />} label="Evento Recorrente" />
                      <Collapse in={field.value}>
                        <Box sx={{ mb: 1, mt: 1 }}>
                          <Controller
                            name="recurrenceRule"
                            control={control}
                            render={({ field: ruleField }) => <RecurrenceForm rule={ruleField.value} onChange={ruleField.onChange} disabled={disabled} />}
                          />
                        </Box>
                      </Collapse>
                    </>
                  )}
                />
              </Grid>

              {isParoco && (
                <Grid size={{ xs: 12 }}>
                  <Box sx={{ pt: 1, borderTop: '1px dashed', borderColor: 'divider' }}>
                    <Controller
                      name="bypassAgendaConflict"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} color="warning" disabled={disabled} />}
                          label="Ignorar Conflitos de Agenda Eclesial (Bypass Pároco)"
                        />
                      )}
                    />
                  </Box>
                </Grid>
              )}
            </>
          )}

          <Grid size={{ xs: 12 }}>
            <TextField
              label="Justificativa Extra (Opcional)"
              placeholder="Preencha caso seja um agendamento extraordinário"
              fullWidth
              disabled={disabled}
              size="small"
              slotProps={{ htmlInput: { maxLength: 4000 } }}
              {...register('justification')}
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default EventoDataRecorrenciaCard;
