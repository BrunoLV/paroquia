import React, {useState} from 'react';
import {
    Box,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    TextField,
} from '@mui/material';
import {ArrowBack as ArrowBackIcon, Block as BlockIcon} from '@mui/icons-material';
import {useNotification} from '../context/NotificationContext';
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
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
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
          </Box>
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
