import React from 'react';
import { Controller, useFieldArray, useFormContext } from 'react-hook-form';
import { Card, CardContent, FormControl, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import GroupsIcon from '@mui/icons-material/Groups';
import ActionButton from '@/shared/presentation/components/ActionButton';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { SITUACAO_PRESENCA_LABEL, TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import { AtaFormInput, presencaVazia } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { Candidato } from '@/contextos/conselho/presentation/components/ataFormTipos';

interface AtaPresencaCardProps {
  regras: TipoReuniaoInfo | undefined;
  candidatos: Candidato[];
}

/**
 * Cartão "Presença": lista nominal (a pessoa é identificada pelo cadastro, nunca digitada) ou, nas assembleias,
 * apenas o total de participantes. Qual modo vale é regra do tipo de reunião.
 */
export const AtaPresencaCard: React.FC<AtaPresencaCardProps> = ({ regras, candidatos }) => {
  const { control, register, getValues, formState: { errors } } = useFormContext<AtaFormInput>();
  const { fields, append, remove } = useFieldArray({ control, name: 'presencas' });
  const quantitativa = regras?.modoPresenca === 'QUANTITATIVA';

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
          <GroupsIcon color="primary" fontSize="small" />
          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>Presença</Typography>
        </Stack>
        {quantitativa ? (
          <TextField label="Total de participantes" type="number" size="small" fullWidth
            error={Boolean(errors.totalParticipantes)} helperText={errors.totalParticipantes?.message}
            slotProps={{ htmlInput: { min: 0 } }}
            {...register('totalParticipantes', { setValueAs: (v: string) => (v === '' || v == null ? null : Number(v)) })} />
        ) : (
          <Stack spacing={2}>
            {fields.map((campo, i) => (
              <Grid container spacing={2} key={campo.id}>
                <Grid size={{ xs: 12, sm: 5 }}>
                  <Controller name={`presencas.${i}.fielId`} control={control} render={({ field }) => (
                    <FormControl fullWidth size="small">
                      <SearchableSelect {...field} labelId={`presenca-${i}`} label="Pessoa" required
                        error={Boolean(errors.presencas?.[i]?.fielId)} helperText={errors.presencas?.[i]?.fielId?.message}>
                        {candidatos.map((c) => <MenuItem key={c.id} value={c.id}>{c.nome}</MenuItem>)}
                      </SearchableSelect>
                    </FormControl>
                  )} />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField label="Função" fullWidth size="small" {...register(`presencas.${i}.funcao`)} />
                </Grid>
                <Grid size={{ xs: 8, sm: 3 }}>
                  <Controller name={`presencas.${i}.situacao`} control={control} render={({ field }) => (
                    <FormControl fullWidth size="small">
                      <SearchableSelect {...field} labelId={`situacao-${i}`} label="Situação">
                        {Object.entries(SITUACAO_PRESENCA_LABEL).map(([v, l]) => <MenuItem key={v} value={v}>{l}</MenuItem>)}
                      </SearchableSelect>
                    </FormControl>
                  )} />
                </Grid>
                <Grid size={{ xs: 4, sm: 1 }}>
                  <ActionButton title="Remover" icon={<DeleteIcon />} color="error" variant="outlined" onClick={() => remove(i)} />
                </Grid>
              </Grid>
            ))}
            <Stack direction="row" spacing={1}>
              <ActionButton title="Adicionar pessoa" icon={<AddIcon />} variant="outlined" onClick={() => append(presencaVazia())} />
              <ActionButton title="Incluir todos os membros" icon={<GroupsIcon />} variant="outlined"
                disabled={candidatos.length === 0}
                onClick={() => {
                  const ja = new Set(getValues('presencas').map((p) => p.fielId));
                  candidatos.filter((c) => !ja.has(c.id)).forEach((c) => append({ ...presencaVazia(), fielId: c.id }));
                }} />
            </Stack>
          </Stack>
        )}
      </CardContent>
    </Card>
  );
};

export default AtaPresencaCard;
