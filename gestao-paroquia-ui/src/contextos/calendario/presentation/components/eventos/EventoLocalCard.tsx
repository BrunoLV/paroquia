import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { Card, CardContent, FormControl, Grid, MenuItem, Stack, Typography } from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { Controller, type UseFormReturn } from 'react-hook-form';
import { AtivoPatrimonial, Local } from '@/shared/domain/types/types';
import { EventoFormInput } from '@/contextos/calendario/presentation/schemas/eventoSchema';

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
      <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: 2.5 }}>
          <LocationOnIcon color="primary" fontSize="small" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Local e recursos
          </Typography>
        </Stack>
        <Grid container spacing={2.5}>
          <Grid size={{ xs: 12 }}>
            <Controller
              name="localIds"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">

                  <SearchableSelect
                    {...field}
                    labelId="new-locais-label"
                    label="Local / Espaços Físicos"
                    multiple
                    disabled={disabled}
                    value={Array.isArray(field.value) ? field.value : []}
                    onChange={(e) => {
                      const val = e.target.value;
                      field.onChange(val);
                      form.setValue('localId', val.length > 0 ? val[0] : '');
                    }}

                  >
                    {localOptions.map((l) => (
                      <MenuItem key={l.id} value={l.id}>
                        {l.nome}
                      </MenuItem>
                    ))}
                  </SearchableSelect>
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

                    <SearchableSelect
                      {...field}
                      labelId="new-ativos-label"
                      multiple
                      label="Bens / Ativos Patrimoniais"
                      disabled={disabled}
                      onChange={(e) => field.onChange(e.target.value)}

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
                    </SearchableSelect>
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
