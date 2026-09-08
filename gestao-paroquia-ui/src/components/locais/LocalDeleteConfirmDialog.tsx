import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CloseIcon from '@mui/icons-material/Close';
import ActionButton from '@/components/ActionButton';
import { Local } from '@/api/types';

interface LocalDeleteConfirmDialogProps {
  local: Local | null;
  open: boolean;
  isPending: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

/**
 * Diálogo de confirmação para exclusão lógica (soft delete) de um Local.
 * Exibe advertência sobre a integridade referencial com eventos futuros.
 */
export const LocalDeleteConfirmDialog: React.FC<LocalDeleteConfirmDialogProps> = ({
  local,
  open,
  isPending,
  onClose,
  onConfirm,
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Excluir Local</DialogTitle>
      <DialogContent>
        <Typography sx={{ pt: 1 }}>
          Tem certeza que deseja excluir o local <strong>{local?.nome}</strong>?
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          O registro será desativado e removido das listagens operacionais. Locais com eventos futuros agendados não podem ser excluídos.
        </Typography>
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton
          title="Cancelar"
          icon={<CloseIcon />}
          onClick={onClose}
          color="secondary"
        />
        <ActionButton
          color="error"
          title="Excluir"
          icon={<DeleteIcon />}
          onClick={onConfirm}
          disabled={isPending}
        />
      </DialogActions>
    </Dialog>
  );
};
