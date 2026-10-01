import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { Card, CardContent, FormControl, FormHelperText, Grid, MenuItem, Stack, Typography } from '@mui/material';
import GroupsIcon from '@mui/icons-material/Groups';
import { Controller, type UseFormReturn } from 'react-hook-form';
import { Organizacao, Projeto } from '@/shared/domain/types/types';
import { EventoFormInput } from '@/contextos/calendario/presentation/schemas/eventoSchema';
import EventoApoioSelect from './EventoApoioSelect';

interface EventoOrganizacaoCardProps {
  form: UseFormReturn<EventoFormInput>;
  disabled: boolean;
  organizations: Organizacao[];
  filteredOrganizations: Organizacao[];
  churches: Organizacao[];
  projetoOptions: Projeto[];
  selectedProjectInfo: string;
  projectValidationError: string | null;
}

/** Seção "Organização e vínculos": organização responsável, igreja, projeto e apoio. */
const EventoOrganizacaoCard: React.FC<EventoOrganizacaoCardProps> = ({
  form,
  disabled,
  organizations,
  filteredOrganizations,
  churches,
  projetoOptions,
  selectedProjectInfo,
  projectValidationError,
}) => {
  const { control, watch } = form;
  const orgId = watch('grupoParoquialResponsavelId');
  const igrejaVinculoId = watch('igrejaVinculoId');

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: 2.5 }}>
          <GroupsIcon color="primary" fontSize="small" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Organização e vínculos
          </Typography>
        </Stack>
        <Grid container spacing={2.5}>
          <Grid size={{ xs: 12 }}>
            <Controller
              name="grupoParoquialResponsavelId"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small" required>

                  <SearchableSelect {...field} labelId="new-org-label" label="Organização Responsável" disabled={disabled}>
                    {filteredOrganizations.map((org) => (
                      <MenuItem key={org.id} value={org.id}>
                        {org.nome}
                      </MenuItem>
                    ))}
                  </SearchableSelect>
                </FormControl>
              )}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <Controller
              name="igrejaVinculoId"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small" required error={!igrejaVinculoId}>

                  <SearchableSelect {...field} labelId="new-igreja-vinculo-label" label="Comunidade / Igreja de Atuação" disabled={disabled} data-testid="event-church-select">
                    <MenuItem value="" disabled>
                      <em>Selecione a Comunidade / Igreja</em>
                    </MenuItem>
                    {churches.map((ch) => (
                      <MenuItem key={ch.id} value={ch.id}>
                        {ch.nome}
                      </MenuItem>
                    ))}
                  </SearchableSelect>
                </FormControl>
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="projetoId"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">

                  <SearchableSelect {...field} labelId="new-projeto-label" label="Projeto" disabled={disabled} error={!!projectValidationError}>
                    <MenuItem value="">
                      <em>Nenhum</em>
                    </MenuItem>
                    {projetoOptions.map((p) => (
                      <MenuItem key={p.id} value={p.id}>
                        {p.nome}
                      </MenuItem>
                    ))}
                  </SearchableSelect>
                  {projectValidationError ? (
                    <FormHelperText error>{projectValidationError}</FormHelperText>
                  ) : selectedProjectInfo ? (
                    <FormHelperText>Período: {selectedProjectInfo}</FormHelperText>
                  ) : null}
                </FormControl>
              )}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <EventoApoioSelect control={control} disabled={disabled} organizations={organizations} orgId={orgId} />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default EventoOrganizacaoCard;
