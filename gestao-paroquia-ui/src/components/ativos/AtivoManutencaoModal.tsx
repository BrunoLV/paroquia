import React, { useEffect, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  AlertTitle,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  List,
  ListItem,
  ListItemText,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import BuildIcon from '@mui/icons-material/Build';
import CloseIcon from '@mui/icons-material/Close';
import ActionButton from '@/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/theme/dialogStyles';
import { confirmDiscard } from '@/utils/confirmDiscard';
import { manutencaoAtivoSchema, ManutencaoAtivoFormInput } from '@/schemas/ativoSchema';
import { verificarConflitoManutencao } from '@/api/ativos';
import { AtivoPatrimonial, ReservaImpeditiva } from '@/api/types';
import { formatDateBr } from '@/utils/masks';

interface AtivoManutencaoModalProps {
  open: boolean;
  ativo: AtivoPatrimonial | null;
  isPending: boolean;
  onClose: () => void;
  onSubmit: (payload: { descricaoReparo: string; dataInicio: string; dataFimPrevista: string }) => void;
}

const toIso = (local: string): string => (local ? new Date(local).toISOString() : '');

/**
 * Envio de ativo para manutenção. Datas via `datetime-local`; ao definir o período, faz
 * pré-checagem de reservas conflitantes e bloqueia o envio se houver.
 */
export const AtivoManutencaoModal: React.FC<AtivoManutencaoModalProps> = ({
  open,
  ativo,
  isPending,
  onClose,
  onSubmit,
}) => {
  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ManutencaoAtivoFormInput>({
    resolver: zodResolver(manutencaoAtivoSchema),
    defaultValues: { descricaoReparo: '', dataInicio: '', dataFimPrevista: '' },
  });

  const [conflitos, setConflitos] = useState<ReservaImpeditiva[]>([]);
  const [checando, setChecando] = useState(false);

  const dataInicio = useWatch({ control, name: 'dataInicio' });
  const dataFimPrevista = useWatch({ control, name: 'dataFimPrevista' });

  useEffect(() => {
    if (open) {
      reset({ descricaoReparo: '', dataInicio: '', dataFimPrevista: '' });
      setConflitos([]);
    }
  }, [open, reset]);

  useEffect(() => {
    if (!open || !ativo || !dataInicio || !dataFimPrevista) {
      setConflitos([]);
      return;
    }
    const controller = new AbortController();
    const timer = setTimeout(async () => {
      setChecando(true);
      try {
        const result = await verificarConflitoManutencao(ativo.id, {
          dataInicio: toIso(dataInicio),
          dataFimPrevista: toIso(dataFimPrevista),
        });
        if (!controller.signal.aborted) setConflitos(result);
      } catch {
        if (!controller.signal.aborted) setConflitos([]);
      } finally {
        if (!controller.signal.aborted) setChecando(false);
      }
    }, 400);
    return () => {
      controller.abort();
      clearTimeout(timer);
    };
  }, [open, ativo, dataInicio, dataFimPrevista]);

  if (!ativo) return null;

  const temConflito = conflitos.length > 0;

  const submit = (data: ManutencaoAtivoFormInput) => {
    onSubmit({
      descricaoReparo: data.descricaoReparo,
      dataInicio: toIso(data.dataInicio),
      dataFimPrevista: toIso(data.dataFimPrevista),
    });
  };

  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(submit)} noValidate>
        <DialogTitle>Enviar para Manutenção — {ativo.nome}</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <BuildIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Dados da manutenção
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      label="Início"
                      type="datetime-local"
                      fullWidth
                      size="small"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataInicio)}
                      helperText={errors.dataInicio?.message}
                      {...register('dataInicio')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      label="Término previsto"
                      type="datetime-local"
                      fullWidth
                      size="small"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataFimPrevista)}
                      helperText={errors.dataFimPrevista?.message}
                      {...register('dataFimPrevista')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      label="Descrição do reparo"
                      fullWidth
                      multiline
                      minRows={3}
                      size="small"
                      error={Boolean(errors.descricaoReparo)}
                      helperText={errors.descricaoReparo?.message}
                      {...register('descricaoReparo')}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {temConflito && (
              <Alert severity="error">
                <AlertTitle>Reservas impeditivas no período</AlertTitle>
                Cancele as reservas abaixo antes de enviar o ativo para manutenção:
                <List dense disablePadding>
                  {conflitos.map((c) => (
                    <ListItem key={c.reservaId} disablePadding>
                      <ListItemText
                        primary={c.descricao}
                        secondary={`${formatDateBr(c.inicio)} — ${formatDateBr(c.fim)} · ${c.status} · ${c.solicitanteNome}`}
                      />
                    </ListItem>
                  ))}
                </List>
              </Alert>
            )}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={handleClose} />
          <ActionButton
            title="Enviar para Manutenção"
            icon={<BuildIcon />}
            color="primary"
            type="submit"
            disabled={isPending || temConflito || checando}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default AtivoManutencaoModal;
