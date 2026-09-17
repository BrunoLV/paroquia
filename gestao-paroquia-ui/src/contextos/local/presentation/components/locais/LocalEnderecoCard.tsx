import React, { useState } from 'react';
import { Control, Controller, FieldErrors, UseFormSetValue } from 'react-hook-form';
import {
  Card,
  CardContent,
  CardHeader,
  CircularProgress,
  Grid,
  InputAdornment,
  TextField,
} from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { LocalFormInput } from '@/contextos/local/presentation/schemas/localSchema';
import { fetchAddressByCep } from '@/shared/infrastructure/external/viaCep';
import { formatCep } from '@/shared/utils/masks';

interface LocalEnderecoCardProps {
  control: Control<LocalFormInput>;
  setValue: UseFormSetValue<LocalFormInput>;
  errors: FieldErrors<LocalFormInput>;
  numeroInputRef: React.RefObject<HTMLInputElement | null>;
}

export const LocalEnderecoCard: React.FC<LocalEnderecoCardProps> = ({
  control,
  setValue,
  errors,
  numeroInputRef,
}) => {
  const [loadingCep, setLoadingCep] = useState(false);

  const handleCepLookup = async (rawCep: string) => {
    const cleanDigits = rawCep.replace(/\D/g, '');
    if (cleanDigits.length === 8) {
      setLoadingCep(true);
      try {
        const address = await fetchAddressByCep(cleanDigits);
        if (address) {
          setValue('logradouro', address.logradouro, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
          setValue('bairro', address.bairro, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
          setValue('cidade', address.cidade, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
          setValue('uf', address.uf, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
          numeroInputRef.current?.focus();
        }
      } finally {
        setLoadingCep(false);
      }
    }
  };

  return (
    <Card variant="outlined">
      <CardHeader
        avatar={<LocationOnIcon color="primary" />}
        title="Endereço"
        titleTypographyProps={{ variant: 'subtitle1', fontWeight: 600 }}
      />
      <CardContent>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 4 }}>
            <Controller
              name="cep"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="CEP"
                  placeholder="00000-000"
                  fullWidth
                  size="small"
                  error={Boolean(errors.cep)}
                  helperText={errors.cep?.message}
                  onChange={(e) => {
                    const formatted = formatCep(e.target.value);
                    field.onChange(formatted);
                    handleCepLookup(formatted);
                  }}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                    input: {
                      endAdornment: loadingCep ? (
                        <InputAdornment position="end">
                          <CircularProgress size={20} />
                        </InputAdornment>
                      ) : null,
                    },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 8 }}>
            <Controller
              name="logradouro"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="Logradouro"
                  placeholder="Rua, Avenida, Praça..."
                  fullWidth
                  size="small"
                  error={Boolean(errors.logradouro)}
                  helperText={errors.logradouro?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 3 }}>
            <Controller
              name="numero"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  inputRef={(element) => {
                    field.ref(element);
                    (numeroInputRef as React.MutableRefObject<HTMLInputElement | null>).current = element;
                  }}
                  label="Número"
                  placeholder="123"
                  fullWidth
                  size="small"
                  error={Boolean(errors.numero)}
                  helperText={errors.numero?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 9 }}>
            <Controller
              name="complemento"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="Complemento"
                  placeholder="Bloco, sala, fundos..."
                  fullWidth
                  size="small"
                  error={Boolean(errors.complemento)}
                  helperText={errors.complemento?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 5 }}>
            <Controller
              name="bairro"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="Bairro"
                  fullWidth
                  size="small"
                  error={Boolean(errors.bairro)}
                  helperText={errors.bairro?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 5 }}>
            <Controller
              name="cidade"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="Cidade"
                  fullWidth
                  size="small"
                  error={Boolean(errors.cidade)}
                  helperText={errors.cidade?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                  }}
                />
              )}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 2 }}>
            <Controller
              name="uf"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value ?? ''}
                  label="UF"
                  placeholder="SP"
                  fullWidth
                  size="small"
                  error={Boolean(errors.uf)}
                  helperText={errors.uf?.message}
                  slotProps={{
                    inputLabel: { shrink: Boolean(field.value) || undefined },
                    htmlInput: { maxLength: 2 },
                  }}
                />
              )}
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
