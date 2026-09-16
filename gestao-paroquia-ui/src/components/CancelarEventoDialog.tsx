import React, {useState} from 'react';
import {
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Stack,
    TextField,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import BlockIcon from '@mui/icons-material/Block';
import {useNotification} from '@/context/NotificationContext';
import {DIALOG_CONTENT_SCROLL_SX} from '@/theme/dialogStyles';
import ActionButton from './ActionButton';

interface CancelarEventoDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  isSubmitting?: boolean;
  eventTitle?: string;
}

const CancelarEventoDialog: React.FC<CancelarEventoDialogProps> = ({
  open,
  onClose,
  onConfirm,
  isSubmitting = false,
  eventTitle,
}) => {
  const [reason, setReason] = useState('');
  const { showError } = useNotification();

  const handleClose = () => {
    setReason('');
    onClose();
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) {
      showError('A justificativa de cancelamento é obrigatória.');
      return;
    }
    if (reason.trim().length < 5) {
      showError('A justificativa deve conter pelo menos 5 caracteres.');
      return;
    }
    onConfirm(reason.trim());
    setReason('');
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>Cancelar Evento</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <DialogContentText color="text.secondary">
              Você está prestes a cancelar o evento{eventTitle ? ` "${eventTitle}"` : ''}.
              Por favor, informe a justificativa abaixo. Esta ação é irreversível e notificará os organizadores.
            </DialogContentText>
            <TextField
              label="Justificativa de Cancelamento"
              multiline
              rows={3}
              fullWidth
              required
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              disabled={isSubmitting}
              slotProps={{ htmlInput: { maxLength: 4000 } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <ActionButton
            title="Voltar"
            icon={<ArrowBackIcon />}
            onClick={handleClose}
            disabled={isSubmitting}
            color="secondary"
          />
          <ActionButton
            title="Confirmar Cancelamento"
            icon={<BlockIcon />}
            type="submit"
            color="error"
            disabled={isSubmitting}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default CancelarEventoDialog;
