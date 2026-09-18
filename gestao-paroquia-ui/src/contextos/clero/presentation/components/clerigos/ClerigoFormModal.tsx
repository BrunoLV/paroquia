import React, { useEffect, useRef, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControl,
  FormControlLabel,
  Grid,
  InputAdornment,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import BadgeIcon from '@mui/icons-material/Badge';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CloseIcon from '@mui/icons-material/Close';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/shared/presentation/components/ActionButton';
import type { Clerigo, ClerigoPayload } from '@/shared/domain/types/types';
import { formatCep, formatCpf, formatPhone, formatRg } from '@/shared/utils/masks';
import { clerigoSchema, ClerigoFormInput } from '@/contextos/clero/presentation/schemas/clerigoSchema';
import { fetchAddressByCep } from '@/shared/infrastructure/external/viaCep';
import {
  FUNCOES_ECLESIASTICAS,
  GRAUS_ORDEM,
  clerigoParaPayload,
  estadoInicialForm,
} from './clerigoConstants';

interface ClerigoFormModalProps {
  open: boolean;
  isPending: boolean;
  initialData?: Clerigo | null;
  onClose: () => void;
  onSubmit: (payload: ClerigoPayload) => void;
}

/**
 * Modal formal de cadastro e edição de Clérigo.
 * Validação via Zod + React Hook Form (`clerigoSchema`), máscaras automáticas
 * nos campos civis (CPF/RG/telefone).
 */
export const ClerigoFormModal: React.FC<ClerigoFormModalProps> = ({
  open,
  isPending,
  initialData,
  onClose,
  onSubmit,
}) => {
  const isEditing = Boolean(initialData);

  const {
    control,
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isDirty },
  } = useForm<ClerigoFormInput>({
    resolver: zodResolver(clerigoSchema),
    defaultValues: estadoInicialForm() as ClerigoFormInput,
  });

  const [loadingCep, setLoadingCep] = useState(false);
  const numeroInputRef = useRef<HTMLInputElement | null>(null);

  const handleCepLookup = async (rawCep: string) => {
    const cleanDigits = rawCep.replace(/\D/g, '');
    if (cleanDigits.length === 8) {
      setLoadingCep(true);
      try {
        const address = await fetchAddressByCep(cleanDigits);
        if (address) {
          setValue('endereco.logradouro', address.logradouro, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
          setValue('endereco.bairro', address.bairro, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
          setValue('endereco.cidade', address.cidade, {
            shouldValidate: true,
            shouldDirty: true,
            shouldTouch: true,
          });
          setValue('endereco.uf', address.uf, {
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

  const handleClose = () => confirmDiscard(isDirty, onClose);

  useEffect(() => {
    if (open) {
      reset(
        (initialData ? clerigoParaPayload(initialData) : estadoInicialForm()) as ClerigoFormInput,
      );
    }
  }, [initialData, open, reset]);

  const submit = (data: ClerigoFormInput) => {
    onSubmit(data as ClerigoPayload);
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(submit)} noValidate>
        <DialogTitle>{isEditing ? 'Editar clérigo' : 'Novo clérigo'}</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            {/* Identificação e Dados Canônicos */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <BadgeIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Identificação e dados canônicos
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Nome eclesiástico"
                      required
                      error={Boolean(errors.nomeEclesiastico)}
                      helperText={errors.nomeEclesiastico?.message}
                      {...register('nomeEclesiastico')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Nome civil"
                      {...register('nomeCivil')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Controller
                      name="grauOrdem"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small" required error={Boolean(errors.grauOrdem)}>
                          <InputLabel>Grau da ordem</InputLabel>
                          <Select {...field} label="Grau da ordem">
                            {GRAUS_ORDEM.map((item) => (
                              <MenuItem key={item.value} value={item.value}>
                                {item.label}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Controller
                      name="funcaoAtual"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small" required error={Boolean(errors.funcaoAtual)}>
                          <InputLabel>Encargo atual</InputLabel>
                          <Select {...field} label="Encargo atual">
                            {FUNCOES_ECLESIASTICAS.map((item) => (
                              <MenuItem key={item.value} value={item.value}>
                                {item.label}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Diocese de incardinação"
                      required
                      error={Boolean(errors.dioceseIncardinacao)}
                      helperText={errors.dioceseIncardinacao?.message}
                      {...register('dioceseIncardinacao')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <Controller
                      name="cpf"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          fullWidth
                          size="small"
                          label="CPF"
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(formatCpf(e.target.value))}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <Controller
                      name="rg"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          fullWidth
                          size="small"
                          label="RG"
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(formatRg(e.target.value))}
                        />
                      )}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Datas Canônicas e Nascimento */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <CalendarMonthIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Datas canônicas e nascimento
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <TextField
                      fullWidth
                      size="small"
                      type="date"
                      label="Data de nascimento"
                      slotProps={{ inputLabel: { shrink: true } }}
                      {...register('dataNascimento')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <Controller
                      name="dataOrdenacaoDiaconal"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          fullWidth
                          size="small"
                          type="date"
                          label="Ordenação diaconal"
                          slotProps={{ inputLabel: { shrink: true } }}
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(e.target.value || null)}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <Controller
                      name="dataOrdenacaoPresbiteral"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          fullWidth
                          size="small"
                          type="date"
                          label="Ordenação presbiteral"
                          slotProps={{ inputLabel: { shrink: true } }}
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(e.target.value || null)}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <Controller
                      name="dataOrdenacaoEpiscopal"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          fullWidth
                          size="small"
                          type="date"
                          label="Ordenação episcopal"
                          slotProps={{ inputLabel: { shrink: true } }}
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(e.target.value || null)}
                        />
                      )}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Contato e Endereço */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <ContactMailIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Contato e endereço
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="E-mail"
                      type="email"
                      required
                      error={Boolean(errors.email)}
                      helperText={errors.email?.message}
                      {...register('email')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Controller
                      name="celular"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          fullWidth
                          size="small"
                          label="Celular"
                          placeholder="(00) 00000-0000"
                          required
                          error={Boolean(errors.celular)}
                          helperText={errors.celular?.message}
                          {...field}
                          onChange={(e) => field.onChange(formatPhone(e.target.value))}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Controller
                      name="telefoneFixo"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          fullWidth
                          size="small"
                          label="Telefone fixo"
                          placeholder="(00) 0000-0000"
                          {...field}
                          value={field.value ?? ''}
                          onChange={(e) => field.onChange(formatPhone(e.target.value))}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <Controller
                      name="isWhatsapp"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={
                            <Switch
                              checked={field.value}
                              onChange={(e) => field.onChange(e.target.checked)}
                            />
                          }
                          label="Celular é WhatsApp"
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <Divider sx={{ my: 0.5 }} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Controller
                      name="endereco.cep"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          value={field.value ?? ''}
                          label="CEP"
                          placeholder="00000-000"
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.cep)}
                          helperText={errors.endereco?.cep?.message}
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
                      name="endereco.logradouro"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          value={field.value ?? ''}
                          label="Logradouro"
                          placeholder="Rua, Avenida, Praça..."
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.logradouro)}
                          helperText={errors.endereco?.logradouro?.message}
                          slotProps={{
                            inputLabel: { shrink: Boolean(field.value) || undefined },
                          }}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <Controller
                      name="endereco.numero"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          value={field.value ?? ''}
                          inputRef={(element) => {
                            field.ref(element);
                            numeroInputRef.current = element;
                          }}
                          label="Número"
                          placeholder="123"
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.numero)}
                          helperText={errors.endereco?.numero?.message}
                          slotProps={{
                            inputLabel: { shrink: Boolean(field.value) || undefined },
                          }}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 9 }}>
                    <Controller
                      name="endereco.complemento"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          value={field.value ?? ''}
                          label="Complemento"
                          placeholder="Bloco, sala, fundos..."
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.complemento)}
                          helperText={errors.endereco?.complemento?.message}
                          slotProps={{
                            inputLabel: { shrink: Boolean(field.value) || undefined },
                          }}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 5 }}>
                    <Controller
                      name="endereco.bairro"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          value={field.value ?? ''}
                          label="Bairro"
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.bairro)}
                          helperText={errors.endereco?.bairro?.message}
                          slotProps={{
                            inputLabel: { shrink: Boolean(field.value) || undefined },
                          }}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 5 }}>
                    <Controller
                      name="endereco.cidade"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          value={field.value ?? ''}
                          label="Cidade"
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.cidade)}
                          helperText={errors.endereco?.cidade?.message}
                          slotProps={{
                            inputLabel: { shrink: Boolean(field.value) || undefined },
                          }}
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 2 }}>
                    <Controller
                      name="endereco.uf"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          value={field.value ?? ''}
                          label="UF"
                          placeholder="SP"
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.uf)}
                          helperText={errors.endereco?.uf?.message}
                          onChange={(e) => field.onChange(e.target.value.toUpperCase())}
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
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={handleClose}
            color="secondary"
          />
          <ActionButton
            type="submit"
            title={isEditing ? 'Salvar alterações' : 'Salvar clérigo'}
            icon={<SaveIcon />}
            color="primary"
            disabled={isPending}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};
