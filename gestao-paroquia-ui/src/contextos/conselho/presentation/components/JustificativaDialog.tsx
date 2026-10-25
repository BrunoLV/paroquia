import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import type { SvgIconProps } from '@mui/material';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';

const justificativaSchema = z.object({
  texto: z.string().trim().min(1, 'Este campo é obrigatório.').max(2000, 'Máximo de 2000 caracteres.'),
});

type JustificativaForm = z.infer<typeof justificativaSchema>;

interface JustificativaDialogProps {
  open: boolean;
  isPending: boolean;
  /** Título do diálogo e do botão de confirmação, por exemplo "Vetar ata". */
  titulo: string;
  /** Rótulo do campo, por exemplo "Motivo do veto". */
  rotulo: string;
  ajuda: string;
  icone: React.ReactElement<SvgIconProps>;
  cor: 'primary' | 'error';
  onClose: () => void;
  onConfirm: (texto: string) => void;
}

/**
 * Diálogo de justificativa obrigatória, usado onde a regra exige motivo registrado na trilha: o veto do Pároco
 * e a abertura de ata retificadora.
 */
export const JustificativaDialog: React.FC<JustificativaDialogProps> = ({
  open, isPending, titulo, rotulo, ajuda, icone, cor, onClose, onConfirm,
}) => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<JustificativaForm>({
    resolver: zodResolver(justificativaSchema),
    defaultValues: { texto: '' },
  });
  useEffect(() => {
    if (open) reset({ texto: '' });
  }, [open, reset]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit((d) => onConfirm(d.texto))} noValidate>
        <DialogTitle>{titulo}</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  {React.cloneElement(icone, { color: cor, fontSize: 'small' })}
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>{rotulo}</Typography>
                </Stack>
                <TextField
                  label={rotulo}
                  fullWidth
                  multiline
                  minRows={3}
                  size="small"
                  error={Boolean(errors.texto)}
                  helperText={errors.texto?.message ?? ajuda}
                  {...register('texto')}
                />
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
          <ActionButton title={titulo} icon={icone} color={cor} type="submit" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default JustificativaDialog;
