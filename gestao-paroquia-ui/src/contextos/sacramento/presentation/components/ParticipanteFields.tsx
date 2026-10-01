import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React from 'react';
import { Control, Controller, FieldErrors, UseFormRegister, useWatch } from 'react-hook-form';
import { Checkbox, FormControl, FormControlLabel, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { PAPEIS_CLERICAIS, PAPEL_LABEL, PapelParticipante } from '@/contextos/sacramento/domain/sacramento';
import { AssentoFormInput } from '@/contextos/sacramento/presentation/schemas/assentoSchema';

interface ParticipanteFieldsProps {
  index: number;
  papeis: PapelParticipante[];
  control: Control<AssentoFormInput>;
  register: UseFormRegister<AssentoFormInput>;
  errors: FieldErrors<AssentoFormInput>;
  onRemove: () => void;
}

/**
 * Linha de participante: dados em texto exatamente como atestados na celebração (snapshot).
 * Condição canônica só aparece para ministros; católico/crismado alimentam a elegibilidade de padrinhos
 * (Cân. 874), validada pelo Pároco na homologação.
 */
export const ParticipanteFields: React.FC<ParticipanteFieldsProps> = ({
  index, papeis, control, register, errors, onRemove,
}) => {
  const papel = useWatch({ control, name: `participantes.${index}.papel` });
  const erro = errors.participantes?.[index];
  const clerical = PAPEIS_CLERICAIS.includes(papel);
  const padrinho = papel === 'PADRINHO' || papel === 'MADRINHA';

  return (
    <Stack spacing={1}>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Controller
            name={`participantes.${index}.papel`}
            control={control}
            render={({ field }) => (
              <FormControl fullWidth size="small">

                <SearchableSelect {...field} labelId={`papel-${index}`} label="Papel">
                  {papeis.map((p) => (
                    <MenuItem key={p} value={p}>{PAPEL_LABEL[p]}</MenuItem>
                  ))}
                </SearchableSelect>
              </FormControl>
            )}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 8 }}>
          <TextField
            label="Nome como atestado"
            fullWidth
            size="small"
            error={Boolean(erro?.nomeHistorico)}
            helperText={erro?.nomeHistorico?.message}
            {...register(`participantes.${index}.nomeHistorico`)}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <TextField label="Documento" fullWidth size="small" {...register(`participantes.${index}.documento`)} />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <TextField
            label="Local de nascimento (cidade/UF/país)"
            fullWidth
            size="small"
            {...register(`participantes.${index}.localNascimento`)}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <TextField
            label="Idade na celebração"
            fullWidth
            size="small"
            error={Boolean(erro?.idadeNaCelebracao)}
            helperText={erro?.idadeNaCelebracao?.message}
            {...register(`participantes.${index}.idadeNaCelebracao`)}
          />
        </Grid>
        {clerical && (
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name={`participantes.${index}.condicaoCanonica`}
              control={control}
              render={({ field }) => (
                <FormControl fullWidth size="small">

                  <SearchableSelect {...field} labelId={`condicao-${index}`} label="Condição canônica">
                    <MenuItem value="">—</MenuItem>
                    <MenuItem value="BISPO">Bispo</MenuItem>
                    <MenuItem value="PRESBITERO">Presbítero</MenuItem>
                    <MenuItem value="DIACONO">Diácono</MenuItem>
                  </SearchableSelect>
                </FormControl>
              )}
            />
          </Grid>
        )}
        {padrinho && (
          <Grid size={{ xs: 12, sm: 8 }}>
            <Stack direction="row" spacing={2}>
              {(['catolico', 'crismado'] as const).map((campo) => (
                <Controller
                  key={campo}
                  name={`participantes.${index}.${campo}`}
                  control={control}
                  render={({ field }) => (
                    <FormControlLabel
                      control={<Checkbox checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                      label={campo === 'catolico' ? 'Católico' : 'Crismado'}
                    />
                  )}
                />
              ))}
            </Stack>
          </Grid>
        )}
      </Grid>
      <Stack direction="row" sx={{ justifyContent: 'flex-end' }}>
        <ActionButton title="Remover participante" label="Remover" icon={<DeleteIcon />} color="error" variant="outlined" onClick={onRemove} />
      </Stack>
      <Typography variant="caption" color="text.secondary">
        Vínculo com cadastro de fiel ou clérigo é opcional e pode ser feito depois, sem alterar este texto.
      </Typography>
    </Stack>
  );
};

export default ParticipanteFields;
