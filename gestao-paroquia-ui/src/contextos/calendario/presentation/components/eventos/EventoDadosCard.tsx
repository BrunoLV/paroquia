import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { Card, CardContent, FormControl, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
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
      <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: 2.5 }}>
          <EventIcon color="primary" fontSize="small" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Dados do evento
          </Typography>
        </Stack>
        <Grid container spacing={2.5}>
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
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name="formato"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">

                  <SearchableSelect {...field} labelId="new-formato-label" label="Formato" disabled={disabled}>
                    <MenuItem value="PRESENCIAL">Presencial</MenuItem>
                    <MenuItem value="ONLINE">Online / Remoto</MenuItem>
                    <MenuItem value="HIBRIDO">Híbrido</MenuItem>
                  </SearchableSelect>
                </FormControl>
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name="categoria"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">

                  <SearchableSelect {...field} labelId="new-cat-label" label="Categoria" disabled={disabled}>
                    {CATEGORIES.map((cat) => (
                      <MenuItem key={cat} value={cat}>
                        {cat}
                      </MenuItem>
                    ))}
                  </SearchableSelect>
                </FormControl>
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name="status"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">

                  <SearchableSelect {...field} labelId="new-status-label" label="Status" disabled={disabled}>
                    {STATUSES.map((st) => (
                      <MenuItem key={st} value={st}>
                        {st}
                      </MenuItem>
                    ))}
                  </SearchableSelect>
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
