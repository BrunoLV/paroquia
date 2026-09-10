import React from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import DeleteIcon from '@mui/icons-material/Delete';
import ActionButton from '@/components/ActionButton';
import { AtivoPatrimonial } from '@/api/types';

interface AtivoExcluirConfirmDialogProps {
  open: boolean;
  ativo: AtivoPatrimonial | null;
  isPending?: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

/** Confirmação de exclusão lógica (soft delete) de um ativo patrimonial. */
export const AtivoExcluirConfirmDialog: React.FC<AtivoExcluirConfirmDialogProps> = ({
  open,
  ativo,
  isPending,
  onClose,
  onConfirm,
}) => (
  <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
    <DialogTitle>Excluir Ativo</DialogTitle>
    <DialogContent>
      <Typography sx={{ pt: 1 }}>
        Tem certeza que deseja excluir o ativo <strong>{ativo?.nome}</strong>?
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
        O registro será desincorporado do patrimônio (exclusão lógica) e sairá das listagens.
        Ativos com manutenção em aberto ou reservas futuras confirmadas não podem ser excluídos.
      </Typography>
    </DialogContent>
    <DialogActions sx={{ px: 3, py: 2 }}>
      <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
      <ActionButton
        title="Excluir"
        icon={<DeleteIcon />}
        color="error"
        onClick={onConfirm}
        disabled={isPending}
      />
    </DialogActions>
  </Dialog>
);

export default AtivoExcluirConfirmDialog;
