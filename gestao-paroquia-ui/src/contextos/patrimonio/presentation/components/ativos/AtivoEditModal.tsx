import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import EditIcon from '@mui/icons-material/Edit';
import LockIcon from '@mui/icons-material/Lock';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import { getCategoriaAtivoMeta } from '@/shared/utils/statusMeta/categoriaAtivoStatus';
import { descricaoAtivoSchema, DescricaoAtivoFormInput } from '@/contextos/patrimonio/presentation/schemas/ativoSchema';
import { AtivoPatrimonial } from '@/shared/domain/types/types';
import { formatCurrencyBRL } from '@/shared/utils/currencyUtils';
import { formatDateBr } from '@/shared/utils/masks';

interface AtivoEditModalProps {
  open: boolean;
  ativo: AtivoPatrimonial | null;
  isPending: boolean;
  onClose: () => void;
  onSave: (descricao: string | null) => void;
}

const lockAdornment = {
  input: {
    endAdornment: (
      <InputAdornment position="end">
        <LockIcon fontSize="small" color="disabled" />
      </InputAdornment>
    ),
  },
};

/**
 * Edição de ativo — imutabilidade estrutural: todos os campos ficam travados (cadeado), exceto
 * a descrição, que tem botão de ação rápida "Salvar Descrição".
 */
export const AtivoEditModal: React.FC<AtivoEditModalProps> = ({ open, ativo, isPending, onClose, onSave }) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<DescricaoAtivoFormInput>({
    resolver: zodResolver(descricaoAtivoSchema),
    defaultValues: { descricao: '' },
  });

  useEffect(() => {
    if (open && ativo) {
      reset({ descricao: ativo.descricao ?? '' });
    }
  }, [open, ativo, reset]);

  if (!ativo) return null;

  const submit = (data: DescricaoAtivoFormInput) => onSave(data.descricao || null);
  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(submit)} noValidate>
        <DialogTitle>Editar Ativo — {ativo.nome}</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <LockIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Campos imutáveis
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Nome" value={ativo.nome} fullWidth size="small" disabled slotProps={lockAdornment} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Código de Tombamento" value={ativo.codigoTombamento} fullWidth size="small" disabled slotProps={lockAdornment} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Tipo" value={getCategoriaAtivoMeta(ativo.categoria).label} fullWidth size="small" disabled slotProps={lockAdornment} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Reservável" value={ativo.isReservavel ? 'Sim' : 'Não'} fullWidth size="small" disabled slotProps={lockAdornment} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Data de Aquisição" value={formatDateBr(ativo.dataAquisicao)} fullWidth size="small" disabled slotProps={lockAdornment} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Valor de Aquisição" value={formatCurrencyBRL(ativo.valorAquisicao)} fullWidth size="small" disabled slotProps={lockAdornment} />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <EditIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Descrição (editável)
                  </Typography>
                </Stack>
                <TextField
                  fullWidth
                  multiline
                  minRows={3}
                  size="small"
                  autoFocus
                  error={Boolean(errors.descricao)}
                  helperText={errors.descricao?.message}
                  {...register('descricao')}
                />
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={handleClose} />
          <ActionButton title="Salvar Descrição" icon={<SaveIcon />} color="primary" type="submit" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default AtivoEditModal;
