import React from 'react';
import { Controller, useFieldArray, useFormContext, useWatch } from 'react-hook-form';
import { Checkbox, FormControl, FormControlLabel, Grid, MenuItem, Paper, Stack, TextField } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import GavelIcon from '@mui/icons-material/Gavel';
import ActionButton from '@/shared/presentation/components/ActionButton';
import IconActionButton from '@/shared/presentation/components/IconActionButton';
import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import { RESULTADO_LABEL } from '@/contextos/conselho/domain/ata';
import { AtaFormInput } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { Candidato } from '@/contextos/conselho/presentation/components/ataFormTipos';

interface DeliberacaoFieldsProps {
  item: number;
  indice: number;
  permiteVotacao: boolean;
  candidatos: Candidato[];
  onRemover: () => void;
}

/**
 * Uma deliberação do item de pauta: texto, resultado (parecer ao Pároco, pois o voto dos conselhos é
 * consultivo), votação onde o tipo vota e declarações de conflito de interesses.
 */
export const DeliberacaoFields: React.FC<DeliberacaoFieldsProps> = ({ item, indice, permiteVotacao, candidatos, onRemover }) => {
  const { control, register, formState: { errors } } = useFormContext<AtaFormInput>();
  const base = `itens.${item}.deliberacoes.${indice}` as const;
  const { fields: conflitos, append, remove } = useFieldArray({ control, name: `${base}.conflitos` });
  const comVotacao = useWatch({ control, name: `${base}.comVotacao` });
  const erros = errors.itens?.[item]?.deliberacoes?.[indice];

  return (
    <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 1.5 }}>
      <Stack spacing={2}>
        <TextField label={`Deliberação ${item + 1}.${indice + 1}`} fullWidth multiline minRows={2} size="small" required
          error={Boolean(erros?.texto)} helperText={erros?.texto?.message} {...register(`${base}.texto`)} />
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller name={`${base}.resultado`} control={control} render={({ field }) => (
              <FormControl fullWidth size="small">
                <SearchableSelect {...field} labelId={`resultado-${item}-${indice}`} label="Resultado">
                  {Object.entries(RESULTADO_LABEL).map(([v, l]) => <MenuItem key={v} value={v}>{l}</MenuItem>)}
                </SearchableSelect>
              </FormControl>
            )} />
          </Grid>
          {permiteVotacao && (
            <Grid size={{ xs: 12, sm: 6 }}>
              <Controller name={`${base}.comVotacao`} control={control} render={({ field }) => (
                <FormControlLabel label="Registrar votação"
                  control={<Checkbox checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />} />
              )} />
            </Grid>
          )}
          {permiteVotacao && comVotacao && (['favor', 'contra', 'abstencoes'] as const).map((campo) => (
            <Grid key={campo} size={{ xs: 4 }}>
              <TextField label={campo === 'abstencoes' ? 'Abstenções' : campo === 'favor' ? 'A favor' : 'Contra'}
                type="number" fullWidth size="small" slotProps={{ htmlInput: { min: 0 } }}
                error={Boolean(erros?.[campo])} helperText={erros?.[campo]?.message}
                {...register(`${base}.${campo}`, { valueAsNumber: true })} />
            </Grid>
          ))}
        </Grid>
        {conflitos.map((c, k) => (
          <Grid container spacing={2} key={c.id} sx={{ alignItems: 'flex-start' }}>
            <Grid size={{ xs: 12, sm: 4 }}>
              <Controller name={`${base}.conflitos.${k}.declaranteFielId`} control={control} render={({ field }) => (
                <FormControl fullWidth size="small">
                  <SearchableSelect {...field} labelId={`conflito-${item}-${indice}-${k}`} label="Quem declara" required
                    error={Boolean(erros?.conflitos?.[k]?.declaranteFielId)}
                    helperText={erros?.conflitos?.[k]?.declaranteFielId?.message}>
                    {candidatos.map((p) => <MenuItem key={p.id} value={p.id}>{p.nome}</MenuItem>)}
                  </SearchableSelect>
                </FormControl>
              )} />
            </Grid>
            <Grid size={{ xs: 9, sm: 7 }}>
              <TextField label="Conflito de interesses" fullWidth size="small" required
                error={Boolean(erros?.conflitos?.[k]?.descricao)} helperText={erros?.conflitos?.[k]?.descricao?.message}
                {...register(`${base}.conflitos.${k}.descricao`)} />
            </Grid>
            <Grid size={{ xs: 3, sm: 1 }} sx={{ display: 'flex', justifyContent: 'center' }}>
              <IconActionButton title="Remover conflito" icon={<DeleteIcon />} color="error" onClick={() => remove(k)} />
            </Grid>
          </Grid>
        ))}
        <Stack direction="row" spacing={1}>
          <ActionButton title="Declarar conflito de interesses" icon={<GavelIcon />} variant="outlined"
            onClick={() => append({ declaranteFielId: '', descricao: '' })} />
          <ActionButton title="Remover deliberação" icon={<DeleteIcon />} color="error" variant="outlined" onClick={onRemover} />
        </Stack>
      </Stack>
    </Paper>
  );
};

export default DeliberacaoFields;
