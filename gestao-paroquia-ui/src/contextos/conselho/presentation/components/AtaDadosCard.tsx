import React from 'react';
import { Controller, useFormContext, useWatch } from 'react-hook-form';
import { Card, CardContent, FormControl, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
import EventIcon from '@mui/icons-material/Event';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { Organizacao } from '@/shared/domain/types/types';
import { CARATER_LABEL, MODALIDADE_LABEL, TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import { useLocaisAtivos } from '@/contextos/conselho/presentation/hooks/useAtas';
import { AtaFormInput } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { associadosDoTipo, convocantesDoTipo } from '@/contextos/conselho/presentation/components/ataFormTipos';

interface AtaDadosCardProps {
  tipos: TipoReuniaoInfo[];
  regras: TipoReuniaoInfo | undefined;
  organizacoes: Organizacao[];
  /** Na edição do rascunho, tipo e órgãos não mudam. */
  edicao: boolean;
}

/** Cartão "Dados da reunião": tipo, órgãos, título, data, local, caráter e modalidade. */
export const AtaDadosCard: React.FC<AtaDadosCardProps> = ({ tipos, regras, organizacoes, edicao }) => {
  const { control, register, setValue, formState: { errors } } = useFormContext<AtaFormInput>();
  const convocanteId = useWatch({ control, name: 'organizacaoConvocanteId' });
  const locais = useLocaisAtivos();

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <EventIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Dados da reunião</Typography>
        </Stack>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller name="tipo" control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId="ata-tipo" label="Tipo de reunião" required disabled={edicao}
                  error={Boolean(errors.tipo)} helperText={errors.tipo?.message}>
                  {tipos.map((t) => <MenuItem key={t.codigo} value={t.codigo}>{t.rotulo}</MenuItem>)}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller name="organizacaoConvocanteId" control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId="ata-convocante" label="Órgão convocante" required
                  onChange={(e) => {
                    field.onChange(e);
                    setValue('organizacaoAssociadaId', '');
                  }}
                  disabled={edicao || !regras} error={Boolean(errors.organizacaoConvocanteId)}
                  helperText={errors.organizacaoConvocanteId?.message}>
                  {convocantesDoTipo(organizacoes, regras).map((o) => <MenuItem key={o.id} value={o.id}>{o.nome}</MenuItem>)}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
          {regras?.exigeOrganizacaoAssociada && (
            <Grid size={{ xs: 12 }}>
              <Controller name="organizacaoAssociadaId" control={control} render={({ field }) => (
                <FormControl fullWidth size="small">
                  <SearchableSelect {...field} labelId="ata-associada" label="Conselho de Assuntos Econômicos (CAEP)"
                    required disabled={edicao}>
                    {associadosDoTipo(organizacoes, organizacoes.find((o) => o.id === convocanteId)).map((o) => <MenuItem key={o.id} value={o.id}>{o.nome}</MenuItem>)}
                  </SearchableSelect>
                </FormControl>
              )} />
            </Grid>
          )}
          <Grid size={{ xs: 12 }}>
            <TextField label="Título da ata" required fullWidth size="small" error={Boolean(errors.titulo)}
              helperText={errors.titulo?.message} {...register('titulo')} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField label="Data da reunião" type="date" required fullWidth size="small"
              slotProps={{ inputLabel: { shrink: true } }} error={Boolean(errors.dataReuniao)}
              helperText={errors.dataReuniao?.message} {...register('dataReuniao')} />
          </Grid>
          <Grid size={{ xs: 12, sm: 8 }}>
            <Controller name="local" control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId="ata-local" label="Local" required
                  error={Boolean(errors.local)} helperText={errors.local?.message}>
                  {(field.value && !locais.includes(field.value) ? [field.value, ...locais] : locais)
                    .map((nome) => <MenuItem key={nome} value={nome}>{nome}</MenuItem>)}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller name="carater" control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId="ata-carater" label="Caráter">
                  {Object.entries(CARATER_LABEL).map(([v, l]) => <MenuItem key={v} value={v}>{l}</MenuItem>)}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller name="modalidade" control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId="ata-modalidade" label="Modalidade">
                  {Object.entries(MODALIDADE_LABEL).map(([v, l]) => <MenuItem key={v} value={v}>{l}</MenuItem>)}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default AtaDadosCard;
