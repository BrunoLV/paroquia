import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Stack, TextField, Typography,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CloseIcon from '@mui/icons-material/Close';
import GavelIcon from '@mui/icons-material/Gavel';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import {
  cancelamentoSchema, CancelamentoFormInput, retificacaoSchema, RetificacaoFormInput,
} from '@/contextos/sacramento/presentation/schemas/assentoAcoesSchema';
import { RetificacaoPayload } from '@/contextos/sacramento/domain/sacramento';

interface DialogBaseProps {
  open: boolean;
  isPending: boolean;
  onClose: () => void;
}

/** Cancelamento de termo: ato do Pároco; o motivo formal é obrigatório e fica na trilha do assento. */
export const CancelamentoDialog: React.FC<DialogBaseProps & { onConfirm: (motivo: string) => void }> = ({
  open, isPending, onClose, onConfirm,
}) => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<CancelamentoFormInput>({
    resolver: zodResolver(cancelamentoSchema),
    defaultValues: { motivo: '' },
  });
  useEffect(() => {
    if (open) reset({ motivo: '' });
  }, [open, reset]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit((d) => onConfirm(d.motivo))} noValidate>
        <DialogTitle>Cancelar assento</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <BlockIcon color="error" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Motivo do cancelamento
                  </Typography>
                </Stack>
                <TextField
                  label="Motivo (duplicidade ou erro formal grave)"
                  fullWidth
                  multiline
                  minRows={3}
                  size="small"
                  error={Boolean(errors.motivo)}
                  helperText={errors.motivo?.message ?? 'O termo permanece no livro, marcado como cancelado.'}
                  {...register('motivo')}
                />
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" label="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
          <ActionButton title="Cancelar assento" icon={<BlockIcon />} color="error" type="submit" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

/** Retificação material: registra uma ressalva atestada pelo Pároco, sem sobrescrever o teor lavrado. */
export const RetificacaoDialog: React.FC<DialogBaseProps & { onConfirm: (p: RetificacaoPayload) => void }> = ({
  open, isPending, onClose, onConfirm,
}) => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<RetificacaoFormInput>({
    resolver: zodResolver(retificacaoSchema),
    defaultValues: { campo: '', valorOriginal: '', valorCorrigido: '', justificativa: '' },
  });
  useEffect(() => {
    if (open) reset({ campo: '', valorOriginal: '', valorCorrigido: '', justificativa: '' });
  }, [open, reset]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form
        onSubmit={handleSubmit((d) =>
          onConfirm({ ...d, valorOriginal: d.valorOriginal || undefined }))}
        noValidate
      >
        <DialogTitle>Retificação material</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <GavelIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Ressalva atestada pelo Pároco
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <TextField label="Campo retificado" fullWidth size="small" error={Boolean(errors.campo)} helperText={errors.campo?.message} {...register('campo')} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Valor lavrado" fullWidth size="small" {...register('valorOriginal')} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Valor correto" fullWidth size="small" error={Boolean(errors.valorCorrigido)} helperText={errors.valorCorrigido?.message} {...register('valorCorrigido')} />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      label="Justificativa"
                      fullWidth
                      multiline
                      minRows={3}
                      size="small"
                      error={Boolean(errors.justificativa)}
                      helperText={errors.justificativa?.message ?? 'O texto original do termo não é alterado.'}
                      {...register('justificativa')}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
          <ActionButton title="Registrar ressalva" icon={<GavelIcon />} color="primary" type="submit" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};
