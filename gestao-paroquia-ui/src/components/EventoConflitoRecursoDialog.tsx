import React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Stack,
  Typography,
  Box,
} from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import EventBusyIcon from '@mui/icons-material/EventBusy';
import { ReservaConflitoDetalhe } from '@/api/types';

interface EventoConflitoRecursoDialogProps {
  open: boolean;
  onClose: () => void;
  detalhes: ReservaConflitoDetalhe[];
  mensagem?: string;
}

export const EventoConflitoRecursoDialog: React.FC<EventoConflitoRecursoDialogProps> = ({
  open,
  onClose,
  detalhes,
  mensagem,
}) => {
  const formatarData = (iso: string) => {
    try {
      return new Date(iso).toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1.5, color: 'error.main' }}>
        <WarningAmberIcon color="error" />
        Bloqueio de Conflito de Recursos (409)
      </DialogTitle>
      <DialogContent dividers>
        <Typography variant="body1" sx={{ mb: 2 }}>
          {mensagem ||
            'Não foi possível salvar o evento pois o local ou recurso selecionado já possui reserva ativa no mesmo intervalo, incluindo a margem de buffer de segurança.'}
        </Typography>

        <Stack spacing={2}>
          {detalhes.map((c, idx) => (
            <Box
              key={idx}
              sx={{
                p: 2,
                borderRadius: 1.5,
                bgcolor: 'action.hover',
                border: '1px solid',
                borderColor: 'error.light',
              }}
            >
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
                <EventBusyIcon fontSize="small" color="error" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                  {c.itemNome} ({c.tipoItem === 'LOCAL' ? 'Espaço Físico' : 'Ativo Patrimonial'})
                </Typography>
              </Stack>

              <Typography variant="body2" color="text.secondary">
                <strong>Ocupação Conflitante:</strong> {formatarData(c.dataHoraInicio)} até {formatarData(c.fimComBuffer)} (com buffer)
              </Typography>

              {c.motivo && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  <strong>Finalidade da reserva concorrente:</strong> {c.motivo}
                </Typography>
              )}
            </Box>
          ))}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} variant="contained" color="primary">
          Ajustar Horário ou Espaço
        </Button>
      </DialogActions>
    </Dialog>
  );
};
