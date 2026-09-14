import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import BlockIcon from '@mui/icons-material/Block';
import ActionButton from '@/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/theme/dialogStyles';
import { confirmDiscard } from '@/utils/confirmDiscard';
import {
  cancelamentoReservaSchema,
  CancelamentoReservaFormData,
} from '@/schemas/reservaSchema';

interface ReservaCancelarModalProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (motivo: string) => void;
  itemNome?: string;
  isPendente?: boolean;
}

export const ReservaCancelarModal: React.FC<ReservaCancelarModalProps> = ({
  open,
  onClose,
  onConfirm,
  itemNome,
  isPendente,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty, isSubmitting },
  } = useForm<CancelamentoReservaFormData>({
    resolver: zodResolver(cancelamentoReservaSchema),
    defaultValues: { motivo: '' },
  });

  const onSubmit = (data: CancelamentoReservaFormData) => {
    onConfirm(data.motivo);
    reset();
  };

  const handleClose = () => confirmDiscard(isDirty, () => {
    reset();
    onClose();
  });

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle sx={{ fontWeight: 700 }}>
          {isPendente ? 'Rejeitar Solicitação de Reserva' : 'Cancelar Reserva'}
        </DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <DialogContentText sx={{ mb: 2 }}>
            Tem certeza que deseja {isPendente ? 'rejeitar a solicitação' : 'cancelar a reserva'} para o recurso{' '}
            <strong>{itemNome}</strong>? Esta operação liberará a agenda e registrará auditoria canônica.
          </DialogContentText>
          <TextField
            {...register('motivo')}
            autoFocus
            fullWidth
            size="small"
            multiline
            rows={3}
            label="Justificativa da Ação"
            placeholder="Informe o motivo formal do cancelamento/rejeição (mínimo de 5 caracteres)..."
            error={Boolean(errors.motivo)}
            helperText={errors.motivo?.message}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <ActionButton title="Voltar" icon={<CloseIcon />} color="secondary" onClick={handleClose} disabled={isSubmitting} />
          <ActionButton
            title={isPendente ? 'Rejeitar Solicitação' : 'Confirmar Cancelamento'}
            icon={<BlockIcon />}
            color="error"
            type="submit"
            disabled={isSubmitting}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};
