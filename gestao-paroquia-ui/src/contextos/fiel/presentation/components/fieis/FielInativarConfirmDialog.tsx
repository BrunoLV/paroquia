import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CloseIcon from '@mui/icons-material/Close';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { Fiel } from '@/shared/domain/types/types';

interface FielInativarConfirmDialogProps {
  fiel: Fiel | null;
  open: boolean;
  isPending: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

/**
 * Diálogo de confirmação com salvaguarda destrutiva para inativação de fiel,
 * garantindo rastreabilidade e prevenção contra inativação acidental.
 */
export const FielInativarConfirmDialog: React.FC<FielInativarConfirmDialogProps> = ({
  fiel,
  open,
  isPending,
  onClose,
  onConfirm,
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Inativar fiel</DialogTitle>
      <DialogContent>
        <Typography sx={{ pt: 1 }}>
          Tem certeza que deseja inativar o cadastro de <strong>{fiel?.nome}</strong>?
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
          title="Inativar"
          icon={<BlockIcon />}
          onClick={onConfirm}
          disabled={isPending}
        />
      </DialogActions>
    </Dialog>
  );
};
