import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  TextField,
  Box,
} from '@mui/material';

interface EventCancelDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  isSubmitting?: boolean;
  eventTitle?: string;
}

const EventCancelDialog: React.FC<EventCancelDialogProps> = ({
  open,
  onClose,
  onConfirm,
  isSubmitting = false,
  eventTitle = 'este evento',
}) => {
  const [reason, setReason] = useState('');
  const [error, setError] = useState(false);

  const handleConfirm = () => {
    if (!reason.trim()) {
      setError(true);
      return;
    }
    onConfirm(reason);
  };

  const handleClose = () => {
    setReason('');
    setError(false);
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={handleClose}
      maxWidth="xs"
      fullWidth
      sx={{ '& .MuiDialog-paper': { borderRadius: '16px' } }}
    >
      <DialogTitle sx={{ fontWeight: 700, color: '#d32f2f' }}>
        Cancelar Evento
      </DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>
          Você tem certeza que deseja cancelar o evento <strong>{eventTitle}</strong>? Esta ação não pode ser desfeita.
        </DialogContentText>
        <Box sx={{ mt: 1 }}>
          <TextField
            autoFocus
            required
            label="Motivo do Cancelamento"
            fullWidth
            multiline
            rows={3}
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              if (e.target.value.trim()) setError(false);
            }}
            error={error}
            helperText={error ? 'O motivo é obrigatório para realizar o cancelamento.' : ''}
            disabled={isSubmitting}
            slotProps={{ htmlInput: { maxLength: 4000 } }}
          />
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2, borderTop: '1px solid #eee' }}>
        <Button onClick={handleClose} disabled={isSubmitting} color="secondary">
          Voltar
        </Button>
        <Button 
          onClick={handleConfirm} 
          variant="contained" 
          color="error" 
          disabled={isSubmitting}
          sx={{ fontWeight: 600, borderRadius: '8px' }}
        >
          {isSubmitting ? 'Cancelando...' : 'Confirmar Cancelamento'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EventCancelDialog;
