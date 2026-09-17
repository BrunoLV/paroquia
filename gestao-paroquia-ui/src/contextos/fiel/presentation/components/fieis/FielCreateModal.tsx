import React from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card,
  CardContent,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  Grid,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import BadgeIcon from '@mui/icons-material/Badge';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { CreateFielPayload, Organizacao } from '@/shared/domain/types/types';
import {
  formatCep,
  formatCpf,
  formatDocumentoEstrangeiro,
  formatPhone,
  formatRg,
} from '@/shared/utils/masks';
import { fielCreateSchema, FielCreateFormInput } from '@/contextos/fiel/presentation/schemas/fielSchema';
import { PAISES_ORIGEM } from './fielConstants';

interface FielCreateModalProps {
  open: boolean;
  unidades: Organizacao[];
  isPending?: boolean;
  onClose: () => void;
  onSave: (payload: CreateFielPayload) => void;
}

const defaultValues: FielCreateFormInput = {
  nome: '',
  sexo: 'MASCULINO',
  dataNascimento: '',
  paisOrigem: 'BRA',
  cpf: '',
  rg: '',
  tipoDocumentoEstrangeiro: undefined,
  numeroDocumentoEstrangeiro: '',
  email: '',
  celular: '',
  isWhatsapp: true,
  telefoneFixo: '',
  endereco: {},
  unidadeTerritorialId: '',
  isDizimista: false,
  permitirDivulgacaoAniversario: false,
};

/**
 * Modal de cadastro de novo fiel paroquial.
 * Contempla dados civis, identificação estrangeira (RNE/RNM/Passaporte), contato,
 * endereço e vinculação comunitária. Validação formal via Zod + React Hook Form,
 * espelhando `fielCreateSchema` (mesmas regras antes aplicadas manualmente).
 */
export const FielCreateModal: React.FC<FielCreateModalProps> = ({
  open,
  unidades,
  isPending,
  onClose,
  onSave,
}) => {
  const {
    control,
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isDirty },
  } = useForm<FielCreateFormInput>({
    resolver: zodResolver(fielCreateSchema),
    defaultValues,
  });

  const handleClose = () => confirmDiscard(isDirty, onClose);

  const paisOrigem = watch('paisOrigem');
  const tipoDocumentoEstrangeiro = watch('tipoDocumentoEstrangeiro');
  const estrangeiro = paisOrigem !== 'BRA';

  const onSubmit = (data: FielCreateFormInput) => {
    onSave(data as CreateFielPayload);
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogTitle>Novo fiel</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            {/* Dados Civis */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <BadgeIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Dados civis
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      required
                      fullWidth
                      size="small"
                      label="Nome completo"
                      error={Boolean(errors.nome)}
                      helperText={errors.nome?.message}
                      {...register('nome')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <Controller
                      name="sexo"
                      control={control}
                      render={({ field }) => (
                        <TextField select required fullWidth size="small" label="Sexo" {...field}>
                          <MenuItem value="MASCULINO">Masculino</MenuItem>
                          <MenuItem value="FEMININO">Feminino</MenuItem>
                          <MenuItem value="NAO_INFORMADO">Não informado</MenuItem>
                        </TextField>
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 3 }}>
                    <TextField
                      required
                      fullWidth
                      size="small"
                      type="date"
                      label="Nascimento"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataNascimento)}
                      helperText={errors.dataNascimento?.message}
                      {...register('dataNascimento')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Controller
                      name="paisOrigem"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          select
                          required
                          fullWidth
                          size="small"
                          label="País de origem"
                          {...field}
                          onChange={(e) => {
                            const novoPais = e.target.value;
                            field.onChange(novoPais);
                            if (novoPais === 'BRA') {
                              setValue('tipoDocumentoEstrangeiro', undefined);
                              setValue('numeroDocumentoEstrangeiro', '');
                            } else {
                              setValue('cpf', '');
                              setValue('rg', '');
                            }
                          }}
                        >
                          {PAISES_ORIGEM.map((pais) => (
                            <MenuItem key={pais.codigo} value={pais.codigo}>
                              {pais.nome}
                            </MenuItem>
                          ))}
                        </TextField>
                      )}
                    />
                  </Grid>
                  {estrangeiro ? (
                    <>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <Controller
                          name="tipoDocumentoEstrangeiro"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              select
                              required
                              fullWidth
                              size="small"
                              label="Documento estrangeiro"
                              error={Boolean(errors.tipoDocumentoEstrangeiro)}
                              helperText={errors.tipoDocumentoEstrangeiro?.message}
                              {...field}
                              value={field.value ?? ''}
                            >
                              <MenuItem value="RNE_RNM">RNE / RNM</MenuItem>
                              <MenuItem value="PASSAPORTE">Passaporte</MenuItem>
                            </TextField>
                          )}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <Controller
                          name="numeroDocumentoEstrangeiro"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              required
                              fullWidth
                              size="small"
                              label="Número do documento"
                              placeholder={
                                tipoDocumentoEstrangeiro === 'RNE_RNM'
                                  ? 'A000000-A'
                                  : 'Número do passaporte'
                              }
                              error={Boolean(errors.numeroDocumentoEstrangeiro)}
                              helperText={errors.numeroDocumentoEstrangeiro?.message}
                              {...field}
                              onChange={(e) =>
                                field.onChange(
                                  formatDocumentoEstrangeiro(tipoDocumentoEstrangeiro, e.target.value),
                                )
                              }
                            />
                          )}
                        />
                      </Grid>
                    </>
                  ) : (
                    <>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <Controller
                          name="cpf"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              required
                              fullWidth
                              size="small"
                              label="CPF"
                              placeholder="000.000.000-00"
                              helperText={errors.cpf?.message ?? 'Informe os 11 dígitos.'}
                              error={Boolean(errors.cpf)}
                              {...field}
                              onChange={(e) => field.onChange(formatCpf(e.target.value))}
                            />
                          )}
                        />
                      </Grid>
                      <Grid size={{ xs: 12, sm: 4 }}>
                        <Controller
                          name="rg"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              fullWidth
                              size="small"
                              label="RG"
                              placeholder="00.000.000-0"
                              {...field}
                              onChange={(e) => field.onChange(formatRg(e.target.value))}
                            />
                          )}
                        />
                      </Grid>
                    </>
                  )}
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
                      required
                      fullWidth
                      size="small"
                      type="email"
                      label="E-mail"
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
                          required
                          fullWidth
                          size="small"
                          label="Celular"
                          placeholder="(00) 00000-0000"
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
                            <Checkbox
                              size="small"
                              checked={field.value}
                              onChange={(e) => field.onChange(e.target.checked)}
                            />
                          }
                          label="Este celular possui WhatsApp"
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <Divider sx={{ my: 0.5 }} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      required
                      fullWidth
                      size="small"
                      label="Logradouro"
                      error={Boolean(errors.endereco?.logradouro)}
                      helperText={errors.endereco?.logradouro?.message}
                      {...register('endereco.logradouro')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 2 }}>
                    <TextField fullWidth size="small" label="Número" {...register('endereco.numero')} />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 4 }}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Complemento"
                      {...register('endereco.complemento')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      required
                      fullWidth
                      size="small"
                      label="Bairro"
                      error={Boolean(errors.endereco?.bairro)}
                      helperText={errors.endereco?.bairro?.message}
                      {...register('endereco.bairro')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      required
                      fullWidth
                      size="small"
                      label="Cidade"
                      error={Boolean(errors.endereco?.cidade)}
                      helperText={errors.endereco?.cidade?.message}
                      {...register('endereco.cidade')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 2 }}>
                    <TextField
                      required
                      fullWidth
                      size="small"
                      label="UF"
                      error={Boolean(errors.endereco?.estado)}
                      helperText={errors.endereco?.estado?.message}
                      {...register('endereco.estado')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6, sm: 2 }}>
                    <Controller
                      name="endereco.cep"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          required
                          fullWidth
                          size="small"
                          label="CEP"
                          placeholder="00000-000"
                          error={Boolean(errors.endereco?.cep)}
                          helperText={errors.endereco?.cep?.message}
                          {...field}
                          onChange={(e) => field.onChange(formatCep(e.target.value))}
                        />
                      )}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Vínculos Comunitários e Pastorais */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <ChurchIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Comunidade e vínculos pastorais
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <Controller
                      name="unidadeTerritorialId"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          select
                          required
                          fullWidth
                          size="small"
                          label="Igreja que frequenta"
                          error={Boolean(errors.unidadeTerritorialId)}
                          helperText={errors.unidadeTerritorialId?.message}
                          {...field}
                        >
                          {unidades
                            .filter((u) => u.tipo === 'MATRIZ' || u.tipo === 'COMUNIDADE')
                            .map((u) => (
                              <MenuItem key={u.id} value={u.id}>
                                {u.nome}
                              </MenuItem>
                            ))}
                        </TextField>
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                      <Controller
                        name="isDizimista"
                        control={control}
                        render={({ field }) => (
                          <FormControlLabel
                            control={
                              <Checkbox
                                size="small"
                                checked={field.value}
                                onChange={(e) => field.onChange(e.target.checked)}
                              />
                            }
                            label="Dizimista"
                          />
                        )}
                      />
                      <Controller
                        name="permitirDivulgacaoAniversario"
                        control={control}
                        render={({ field }) => (
                          <FormControlLabel
                            control={
                              <Checkbox
                                size="small"
                                checked={field.value}
                                onChange={(e) => field.onChange(e.target.checked)}
                              />
                            }
                            label="Autoriza divulgar somente dia e mês do aniversário"
                          />
                        )}
                      />
                    </Stack>
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
            title="Salvar"
            icon={<AddIcon />}
            color="primary"
            disabled={isPending}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};
