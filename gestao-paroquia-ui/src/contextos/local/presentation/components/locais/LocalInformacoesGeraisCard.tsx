import React from 'react';
import { Control, Controller, FieldErrors } from 'react-hook-form';
import {
  Card,
  CardContent,
  CardHeader,
  FormControl,
  FormControlLabel,
  FormHelperText,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Switch,
  TextField,
} from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import { LocalFormInput } from '@/contextos/local/presentation/schemas/localSchema';
import { Organizacao } from '@/shared/domain/types/types';

interface LocalInformacoesGeraisCardProps {
  control: Control<LocalFormInput>;
  errors: FieldErrors<LocalFormInput>;
  igrejas: Organizacao[];
}

export const LocalInformacoesGeraisCard: React.FC<LocalInformacoesGeraisCardProps> = ({
  control,
  errors,
  igrejas,
}) => {
  return (
    <Card variant="outlined">
      <CardHeader
        avatar={<InfoOutlinedIcon color="primary" />}
        title="Informações Gerais"
        titleTypographyProps={{ variant: 'subtitle1', fontWeight: 600 }}
      />
      <CardContent>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 8 }}>
            <Controller
              name="nome"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="Nome do Local"
                  placeholder="Ex.: Salão Paroquial São José"
                  fullWidth
                  size="small"
                  error={Boolean(errors.nome)}
                  helperText={errors.nome?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 4 }}>
            <FormControl fullWidth size="small" error={Boolean(errors.tipo)}>
              <InputLabel id="tipo-local-label">Tipo</InputLabel>
              <Controller
                name="tipo"
                control={control}
                render={({ field }) => (
                  <Select {...field} labelId="tipo-local-label" label="Tipo">
                    <MenuItem value="SALAO_PAROQUIAL">Salão Paroquial</MenuItem>
                    <MenuItem value="IGREJA">Igreja / Matriz</MenuItem>
                    <MenuItem value="CAPELA">Capela</MenuItem>
                    <MenuItem value="SALA_REUNIAO">Sala de Reunião</MenuItem>
                    <MenuItem value="AUDITORIO">Auditório</MenuItem>
                    <MenuItem value="QUADRA">Quadra</MenuItem>
                    <MenuItem value="SECRETARIA">Secretaria</MenuItem>
                    <MenuItem value="OUTRO">Outro</MenuItem>
                  </Select>
                )}
              />
              {errors.tipo && <FormHelperText>{errors.tipo.message}</FormHelperText>}
            </FormControl>
          </Grid>

          <Grid size={{ xs: 12, sm: 6 }}>
            <FormControl fullWidth size="small" error={Boolean(errors.igrejaId)}>
              <InputLabel id="igreja-local-label">Igreja / Comunidade</InputLabel>
              <Controller
                name="igrejaId"
                control={control}
                render={({ field }) => (
                  <Select {...field} labelId="igreja-local-label" label="Igreja / Comunidade">
                    {igrejas.map((org) => (
                      <MenuItem key={org.id} value={org.id}>
                        {org.nome} ({org.tipo})
                      </MenuItem>
                    ))}
                  </Select>
                )}
              />
              {errors.igrejaId && <FormHelperText>{errors.igrejaId.message}</FormHelperText>}
            </FormControl>
          </Grid>

          <Grid size={{ xs: 12, sm: 6 }}>
            <Controller
              name="capacidadeMaxima"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  onChange={(e) => {
                    const val = e.target.value === '' ? '' : Number(e.target.value);
                    field.onChange(val);
                  }}
                  label="Capacidade Máxima (Pessoas)"
                  type="number"
                  fullWidth
                  size="small"
                  error={Boolean(errors.capacidadeMaxima)}
                  helperText={errors.capacidadeMaxima?.message}
                  slotProps={{
                    inputLabel: { shrink: field.value !== undefined && field.value !== null && !Number.isNaN(field.value) ? true : undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12 }}>
            <Controller
              name="descricao"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="Descrição / Finalidades de Uso e Equipamentos"
                  placeholder="Descreva recursos disponíveis (ar condicionado, projetor, mesas) ou restrições..."
                  multiline
                  rows={2.5}
                  fullWidth
                  size="small"
                  error={Boolean(errors.descricao)}
                  helperText={errors.descricao?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12 }}>
            <Controller
              name="ativo"
              control={control}
              render={({ field }) => (
                <FormControlLabel
                  control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} color="primary" />}
                  label="Local ativo para uso e agendamentos"
                />
              )}
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
