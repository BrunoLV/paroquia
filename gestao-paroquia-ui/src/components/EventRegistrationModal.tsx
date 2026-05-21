import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';

interface EventRegistrationModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (e: React.FormEvent) => void;
  isSubmitting?: boolean;
  organizations?: any[];
}

const EventRegistrationModal: React.FC<EventRegistrationModalProps> = ({
  open,
  onClose,
  onSubmit,
  isSubmitting = false,
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={onSubmit}>
        <DialogTitle>Criar Novo Evento</DialogTitle>
        <DialogContent>
          {/* Form fields will go here */}
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSubmitting} color="secondary">
            Cancelar
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={isSubmitting}>
            Criar Evento
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default EventRegistrationModal;
