import React, { useEffect, useRef, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  Grid,
  InputAdornment,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { fetchAddressByCep } from '@/shared/infrastructure/external/viaCep';
import BadgeIcon from '@mui/icons-material/Badge';
import BlockIcon from '@mui/icons-material/Block';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import Diversity3Icon from '@mui/icons-material/Diversity3';
import EditIcon from '@mui/icons-material/Edit';
import FamilyRestroomIcon from '@mui/icons-material/FamilyRestroom';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { Fiel, Organizacao, UpdateFielPayload } from '@/shared/domain/types/types';
import {
  RelacionamentoFamiliar,
  TipoRelacionamentoFamiliar,
} from '@/contextos/fiel/infrastructure/http/familiares';
import {
  formatCep,
  formatCpf,
  formatDocumentoEstrangeiro,
  formatPhone,
  formatRg,
} from '@/shared/utils/masks';
import { fielEditSchema, FielEditFormInput } from '@/contextos/fiel/presentation/schemas/fielSchema';
import {
  PAISES_ORIGEM,
  TIPO_RELACAO_LABELS,
  cadastroVazio,
  paraCadastro,
} from './fielConstants';

interface FielEditModalProps {
  open: boolean;
  fiel: Fiel | null;
  unidades: Organizacao[];
  familiares: RelacionamentoFamiliar[];
  fieisDisponiveis: Fiel[];
  isPending?: boolean;
  isVinculoPending?: boolean;
  onClose: () => void;
  onSave: (payload: UpdateFielPayload) => void;
  onVincularFamiliar: (destinoId: string, tipo: TipoRelacionamentoFamiliar) => void;
  onRemoverFamiliar: (id: string) => void;
}

/** `UpdateFielPayload` possui `endereco` opcional; o formulário sempre trabalha com o objeto preenchido. */
const paraFormInput = (fiel: Fiel): FielEditFormInput => {
  const cadastro = paraCadastro(fiel);
  const endereco = typeof cadastro.endereco === 'object' && cadastro.endereco ? cadastro.endereco : {};
  return { ...cadastro, endereco } as FielEditFormInput;
};

const formInputVazio = (): FielEditFormInput => {
  const cadastro = cadastroVazio();
  return { ...cadastro, endereco: {} } as FielEditFormInput;
};

/**
 * Modal de edição cadastral do Fiel e gestão de vínculos familiares.
 * Cadastro validado via Zod + React Hook Form (`fielEditSchema`); o
 * mini-formulário de vínculo familiar (destino/tipo) permanece em
 * `useState` local por ser independente do payload de edição do fiel.
 */
export const FielEditModal: React.FC<FielEditModalProps> = ({
  open,
  fiel,
  unidades,
  familiares,
  fieisDisponiveis,
  isPending,
  isVinculoPending,
  onClose,
  onSave,
  onVincularFamiliar,
  onRemoverFamiliar,
}) => {
  const [destino, setDestino] = useState('');
  const [tipo, setTipo] = useState<TipoRelacionamentoFamiliar>('CONJUGE');

  const {
    control,
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors, isDirty },
  } = useForm<FielEditFormInput>({
    resolver: zodResolver(fielEditSchema),
    defaultValues: formInputVazio(),
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
          setValue('endereco.estado', address.uf, {
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
      reset(fiel ? paraFormInput(fiel) : formInputVazio());
    }
  }, [fiel, open, reset]);

  if (!fiel) return null;

  const paisOrigem = watch('paisOrigem');
  const tipoDocumentoEstrangeiro = watch('tipoDocumentoEstrangeiro');
  const estrangeiro = paisOrigem !== 'BRA';

  const handleVincular = () => {
    if (!destino) return;
    onVincularFamiliar(destino, tipo);
    setDestino('');
  };

  const fielId = fiel.fielId ?? fiel.id;

  const onSubmit = (data: FielEditFormInput) => {
    onSave(data as UpdateFielPayload);
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogTitle>
          <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography variant="h6" component="div">
              Editar fiel
            </Typography>
            <Chip
              size="small"
              label={(fiel.isAtivo ?? fiel.ativo) ? 'Ativo' : 'Inativo'}
              color={(fiel.isAtivo ?? fiel.ativo) ? 'success' : 'default'}
              variant="outlined"
            />
          </Stack>
        </DialogTitle>
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
                              error={Boolean(errors.cpf)}
                              helperText={errors.cpf?.message}
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
                              checked={field.value ?? true}
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
                          required
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
                          required
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
                          required
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
                          required
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
                          required
                          fullWidth
                          size="small"
                          error={Boolean(errors.endereco?.uf || errors.endereco?.estado)}
                          helperText={errors.endereco?.uf?.message || errors.endereco?.estado?.message}
                          onChange={(e) => {
                            const val = e.target.value.toUpperCase();
                            field.onChange(val);
                            setValue('endereco.estado', val);
                          }}
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
                                checked={field.value ?? false}
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
                                checked={field.value ?? false}
                                onChange={(e) => field.onChange(e.target.checked)}
                              />
                            }
                            label="Autoriza divulgar dia e mês do aniversário"
                          />
                        )}
                      />
                    </Stack>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Vínculos Familiares */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1.5 }}>
                  <FamilyRestroomIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Família
                  </Typography>
                </Stack>

                {familiares.length === 0 ? (
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Nenhum vínculo familiar registrado.
                  </Typography>
                ) : (
                  <Grid container spacing={2} sx={{ mb: 2 }}>
                    {familiares.map((r) => (
                      <Grid size={{ xs: 12, sm: 6 }} key={r.id}>
                        <Paper
                          variant="outlined"
                          sx={{
                            p: 1.5,
                            borderRadius: 1.5,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                          }}
                        >
                          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                            <Diversity3Icon fontSize="small" color="action" />
                            <Typography variant="body2" sx={{ fontWeight: 600 }}>
                              {r.nome}
                            </Typography>
                          </Stack>
                          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                            <Chip
                              size="small"
                              label={TIPO_RELACAO_LABELS[r.relacao] ?? r.relacao}
                              variant="outlined"
                            />
                            <ActionButton
                              size="small"
                              color="error"
                              title="Inativar vínculo"
                              icon={<BlockIcon />}
                              onClick={() => onRemoverFamiliar(r.id)}
                            />
                          </Stack>
                        </Paper>
                      </Grid>
                    ))}
                  </Grid>
                )}

                <Divider sx={{ my: 1.5 }} />

                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ mb: 1, display: 'block', fontWeight: 600 }}
                >
                  Adicionar vínculo familiar
                </Typography>
                <Grid container spacing={2} sx={{ alignItems: 'center' }}>
                  <Grid size={{ xs: 12, sm: 5 }}>
                    <TextField
                      select
                      fullWidth
                      size="small"
                      label="Fiel relacionado"
                      value={destino}
                      onChange={(e) => setDestino(e.target.value)}
                    >
                      {fieisDisponiveis
                        .filter((f) => (f.fielId ?? f.id) !== fielId)
                        .map((f) => (
                          <MenuItem key={f.fielId ?? f.id} value={f.fielId ?? f.id}>
                            {f.nome}
                          </MenuItem>
                        ))}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      select
                      fullWidth
                      size="small"
                      label="Relação"
                      value={tipo}
                      onChange={(e) => setTipo(e.target.value as TipoRelacionamentoFamiliar)}
                    >
                      <MenuItem value="CONJUGE">Cônjuge</MenuItem>
                      <MenuItem value="GENITOR">Genitor(a)</MenuItem>
                      <MenuItem value="FILHO">Filho(a)</MenuItem>
                      <MenuItem value="IRMAO">Irmão(ã)</MenuItem>
                      <MenuItem value="OUTRO">Outro</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 3 }}>
                    <ActionButton
                      fullWidth
                      color="primary"
                      title="Vincular"
                      icon={<AddIcon />}
                      disabled={!destino || isVinculoPending}
                      onClick={handleVincular}
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
            color="primary"
            title="Salvar alterações"
            icon={<EditIcon />}
            disabled={isPending}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};
