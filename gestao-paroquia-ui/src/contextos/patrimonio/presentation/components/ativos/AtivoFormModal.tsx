import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React, { useEffect } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Box, Card, CardContent, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, FormControlLabel, FormHelperText, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { getCategoriaAtivoMeta } from '@/shared/utils/statusMeta/categoriaAtivoStatus';
import { ativoSchema, AtivoFormInput } from '@/contextos/patrimonio/presentation/schemas/ativoSchema';
import { AtivoCategoria, CadastrarAtivoPayload, Organizacao } from '@/shared/domain/types/types';

interface AtivoFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (payload: CadastrarAtivoPayload) => void;
  organizacoes: Organizacao[];
  categorias: AtivoCategoria[];
  defaultOrgId?: string;
  isPending: boolean;
}

const emptyForm: AtivoFormInput = {
  nome: '',
  descricao: '',
  categoria: 'ELETRONICO',
  organizacaoId: '',
  isReservavel: false,
  dataAquisicao: '',
  valorAquisicao: 0,
};

/** Modal de cadastro de Ativo Patrimonial — react-hook-form + zod, padrão de Locais. */
export const AtivoFormModal: React.FC<AtivoFormModalProps> = ({
  open,
  onClose,
  onSave,
  organizacoes,
  categorias,
  defaultOrgId,
  isPending,
}) => {
  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<AtivoFormInput>({
    resolver: zodResolver(ativoSchema),
    defaultValues: emptyForm,
  });

  useEffect(() => {
    if (open) {
      reset({ ...emptyForm, organizacaoId: defaultOrgId ?? '' });
    }
  }, [open, defaultOrgId, reset]);

  const submit = (data: AtivoFormInput) => {
    onSave({
      nome: data.nome,
      descricao: data.descricao || null,
      categoria: data.categoria,
      organizacaoId: data.organizacaoId,
      isReservavel: data.isReservavel,
      dataAquisicao: data.dataAquisicao,
      valorAquisicao: Number(data.valorAquisicao),
    });
  };

  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(submit)} noValidate>
        <DialogTitle>Novo Ativo Patrimonial</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <AccountBalanceIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Identificação do ativo
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Controller
                      name="organizacaoId"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small" required error={Boolean(errors.organizacaoId)}>

                          <SearchableSelect {...field} labelId="create-organizacao-select-label" label="Organização Dona">
                            {organizacoes.map((org) => (
                              <MenuItem key={org.id} value={org.id}>
                                {org.nome}
                              </MenuItem>
                            ))}
                          </SearchableSelect>
                          {errors.organizacaoId && (
                            <FormHelperText>{errors.organizacaoId.message}</FormHelperText>
                          )}
                        </FormControl>
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      label="Nome"
                      fullWidth
                      required
                      size="small"
                      error={Boolean(errors.nome)}
                      helperText={errors.nome?.message}
                      {...register('nome')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Controller
                      name="categoria"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small">

                          <SearchableSelect {...field} labelId="categoria-select-label" label="Tipo">
                            {categorias.map((categoria) => (
                              <MenuItem key={categoria} value={categoria}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                  <StatusIcon {...getCategoriaAtivoMeta(categoria)} />
                                </Box>
                              </MenuItem>
                            ))}
                          </SearchableSelect>
                        </FormControl>
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }} sx={{ display: 'flex', alignItems: 'center' }}>
                    <Controller
                      name="isReservavel"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Checkbox checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                          label="Reservável"
                        />
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      label="Data de Aquisição"
                      type="date"
                      fullWidth
                      required
                      size="small"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataAquisicao)}
                      helperText={errors.dataAquisicao?.message}
                      {...register('dataAquisicao')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      label="Valor de Aquisição"
                      type="number"
                      fullWidth
                      required
                      size="small"
                      error={Boolean(errors.valorAquisicao)}
                      helperText={errors.valorAquisicao?.message}
                      {...register('valorAquisicao', { valueAsNumber: true })}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      label="Descrição"
                      fullWidth
                      multiline
                      minRows={2}
                      size="small"
                      error={Boolean(errors.descricao)}
                      helperText={errors.descricao?.message ?? 'Especificações técnicas, estado físico (opcional).'}
                      {...register('descricao')}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={handleClose} />
          <ActionButton title="Salvar Ativo" icon={<SaveIcon />} color="primary" type="submit" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default AtivoFormModal;
