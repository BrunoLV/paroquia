import React from 'react';
import { Box, Card, CardContent, Chip, FormControl, Grid, InputLabel, MenuItem, Select, Stack, Typography } from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { Controller, type UseFormReturn } from 'react-hook-form';
import { AtivoPatrimonial, Local } from '@/api/types';
import { EventoFormInput } from '@/schemas/eventoSchema';

interface EventoLocalCardProps {
  form: UseFormReturn<EventoFormInput>;
  disabled: boolean;
  localOptions: Local[];
  ativos: AtivoPatrimonial[];
}

/** Seção "Local e recursos": local do evento e ativos patrimoniais reserváveis. */
const EventoLocalCard: React.FC<EventoLocalCardProps> = ({ form, disabled, localOptions, ativos }) => {
  const { control, watch } = form;
  const selectedAtivoIds = watch('selectedAtivoIds');

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <LocationOnIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Local e recursos
          </Typography>
        </Stack>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12 }}>
            <Controller
              name="localId"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">
                  <InputLabel id="new-local-label">Local</InputLabel>
                  <Select {...field} labelId="new-local-label" label="Local" disabled={disabled}>
                    <MenuItem value="">
                      <em>Nenhum</em>
                    </MenuItem>
                    {localOptions.map((l) => (
                      <MenuItem key={l.id} value={l.id}>
                        {l.nome}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              )}
            />
          </Grid>
          {/* Ativos Patrimoniais — apenas exibido quando há ativos reserváveis disponíveis */}
          {ativos.length > 0 && (
            <Grid size={{ xs: 12 }}>
              <Controller
                name="selectedAtivoIds"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth size="small">
                    <InputLabel id="new-ativos-label">Bens / Ativos Patrimoniais</InputLabel>
                    <Select
                      {...field}
                      labelId="new-ativos-label"
                      multiple
                      label="Bens / Ativos Patrimoniais"
                      disabled={disabled}
                      onChange={(e) => field.onChange(typeof e.target.value === 'string' ? e.target.value.split(',') : e.target.value)}
                      renderValue={(selected) => (
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                          {(selected as string[]).map((id) => {
                            const ativo = ativos.find((a) => a.id === id);
                            return <Chip key={id} label={ativo ? `${ativo.nome} (${ativo.categoria})` : id} size="small" />;
                          })}
                        </Box>
                      )}
                    >
                      {/* Inclui também os já alocados a este evento (status RESERVADO),
                          senão sumiriam das opções ao editar mesmo estando selecionados. */}
                      {ativos
                        .filter((a) => (a.isReservavel && a.status === 'DISPONIVEL') || selectedAtivoIds.includes(a.id))
                        .map((a) => (
                          <MenuItem key={a.id} value={a.id}>
                            {a.nome} — {a.categoria}
                          </MenuItem>
                        ))}
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>
          )}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default EventoLocalCard;
