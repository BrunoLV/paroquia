import React from 'react';
import { Card, CardContent, FormControl, Grid, InputLabel, MenuItem, Select, Stack, TextField, Typography } from '@mui/material';
import EventIcon from '@mui/icons-material/Event';
import { Controller, type UseFormReturn } from 'react-hook-form';
import { EventoFormInput } from '@/contextos/calendario/presentation/schemas/eventoSchema';

export const CATEGORIES = ['SOLENIDADE', 'PASTORAL', 'SOCIAL', 'LITURGICO', 'ADMINISTRATIVO', 'SACRAMENTAL', 'FORMATIVO', 'ASSISTENCIAL', 'MOVIMENTO'];
export const STATUSES = ['RASCUNHO', 'CONFIRMADO', 'ADICIONADO_EXTRA', 'CANCELADO'];

interface EventoDadosCardProps {
  form: UseFormReturn<EventoFormInput>;
  disabled: boolean;
}

/** Seção "Dados do evento": título, descrição, categoria e status. */
const EventoDadosCard: React.FC<EventoDadosCardProps> = ({ form, disabled }) => {
  const { register, control } = form;
  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <EventIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Dados do evento
          </Typography>
        </Stack>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12 }}>
            <TextField
              label="Título do evento"
              required
              fullWidth
              disabled={disabled}
              size="small"
              slotProps={{ htmlInput: { maxLength: 160 } }}
              {...register('titulo')}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              label="Descrição"
              multiline
              rows={3}
              fullWidth
              disabled={disabled}
              size="small"
              slotProps={{ htmlInput: { maxLength: 4000 } }}
              {...register('descricao')}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <Controller
              name="categoria"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">
                  <InputLabel id="new-cat-label">Categoria</InputLabel>
                  <Select {...field} labelId="new-cat-label" label="Categoria" disabled={disabled}>
                    {CATEGORIES.map((cat) => (
                      <MenuItem key={cat} value={cat}>
                        {cat}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}
            />
          </Grid>
          <Grid size={{ xs: 6 }}>
            <Controller
              name="status"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">
                  <InputLabel id="new-status-label">Status</InputLabel>
                  <Select {...field} labelId="new-status-label" label="Status" disabled={disabled}>
                    {STATUSES.map((st) => (
                      <MenuItem key={st} value={st}>
                        {st}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default EventoDadosCard;
