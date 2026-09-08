import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
} from '@mui/material';
import {
  devolucaoAtivoSchema,
  DevolucaoAtivoFormData,
} from '@/schemas/reservaSchema';

interface ReservaDevolucaoModalProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (checkAvarias: string) => void;
  itemNome?: string;
}

export const ReservaDevolucaoModal: React.FC<ReservaDevolucaoModalProps> = ({
  open,
  onClose,
  onConfirm,
  itemNome,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<DevolucaoAtivoFormData>({
    resolver: zodResolver(devolucaoAtivoSchema),
    defaultValues: { checkAvarias: 'Item devolvido em perfeito estado de funcionamento.' },
  });

  const onSubmit = (data: DevolucaoAtivoFormData) => {
    onConfirm(data.checkAvarias);
    reset();
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="xs" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle sx={{ fontWeight: 700 }}>
          Registro de Devolução de Ativo
        </DialogTitle>
        <DialogContent>
          <DialogContentText sx={{ mb: 2 }}>
            Inspeção e devolução física do bem <strong>{itemNome}</strong>.
            Descreva detalhadamente as condições de integridade física e operacional do equipamento.
          </DialogContentText>
          <TextField
            {...register('checkAvarias')}
            autoFocus
            fullWidth
            size="small"
            multiline
            rows={3}
            label="Relatório de Inspeção (Check de Avarias)"
            placeholder="Ex: Cabos enrolados, aparelho testado, sem avarias..."
            error={Boolean(errors.checkAvarias)}
            helperText={errors.checkAvarias?.message}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleClose} disabled={isSubmitting}>
            Voltar
          </Button>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={isSubmitting}
          >
            Registrar Devolução
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
