import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CloseIcon from '@mui/icons-material/Close';
import ActionButton from '@/shared/presentation/components/ActionButton';
import type { Clerigo, ClerigoInstitucional } from '@/shared/domain/types/types';

interface ClerigoInativarDialogProps {
  clerigo: Clerigo | ClerigoInstitucional | null;
  open: boolean;
  isPending: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

/**
 * Diálogo de confirmação destrutiva para inativação de clérigo no corpo ministerial.
 */
export const ClerigoInativarDialog: React.FC<ClerigoInativarDialogProps> = ({
  clerigo,
  open,
  isPending,
  onClose,
  onConfirm,
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Inativar clérigo</DialogTitle>
      <DialogContent>
        <Typography sx={{ pt: 1 }}>
          Tem certeza que deseja inativar o clérigo{' '}
          <strong>{clerigo?.nomeEclesiastico}</strong>?
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
