import React, { useEffect } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card, CardContent, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, FormControlLabel,
  Grid, InputLabel, MenuItem, Select, Stack, TextField, Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import EditNoteIcon from '@mui/icons-material/EditNote';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import {
  AverbacaoPayload, TIPO_EVENTO_AVERBACAO_LABEL, TipoEventoAverbacao,
} from '@/contextos/sacramento/domain/sacramento';
import { averbacaoSchema, AverbacaoFormInput } from '@/contextos/sacramento/presentation/schemas/assentoAcoesSchema';

interface AverbacaoDialogProps {
  open: boolean;
  isPending: boolean;
  onClose: () => void;
  onConfirm: (payload: AverbacaoPayload) => void;
}

const vazio: AverbacaoFormInput = {
  tipoEvento: 'CONFIRMACAO', dataAverbacao: '', numeroDocumento: '', teorNota: '', reservada: false,
};

/** Nota marginal do Batismo (Cân. 535 §2): a secretaria digita, o Pároco homologa; nunca se edita nem remove. */
export const AverbacaoDialog: React.FC<AverbacaoDialogProps> = ({ open, isPending, onClose, onConfirm }) => {
  const { control, register, handleSubmit, reset, formState: { errors } } = useForm<AverbacaoFormInput>({
    resolver: zodResolver(averbacaoSchema),
    defaultValues: vazio,
  });
  useEffect(() => {
    if (open) reset(vazio);
  }, [open, reset]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form
        onSubmit={handleSubmit((d) => onConfirm({ ...d, numeroDocumento: d.numeroDocumento || undefined }))}
        noValidate
      >
        <DialogTitle>Nova averbação marginal</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <EditNoteIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Evento averbado
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Controller
                      name="tipoEvento"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small">
                          <InputLabel id="averbacao-tipo">Evento</InputLabel>
                          <Select {...field} labelId="averbacao-tipo" label="Evento">
                            {(Object.keys(TIPO_EVENTO_AVERBACAO_LABEL) as TipoEventoAverbacao[]).map((t) => (
                              <MenuItem key={t} value={t}>{TIPO_EVENTO_AVERBACAO_LABEL[t]}</MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      )}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      label="Data da averbação"
                      type="date"
                      fullWidth
                      size="small"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataAverbacao)}
                      helperText={errors.dataAverbacao?.message}
                      {...register('dataAverbacao')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField label="Nº do documento ou notificação comprobatória" fullWidth size="small" {...register('numeroDocumento')} />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      label="Teor da nota marginal"
                      fullWidth
                      multiline
                      minRows={3}
                      size="small"
                      error={Boolean(errors.teorNota)}
                      helperText={errors.teorNota?.message}
                      {...register('teorNota')}
                    />
                  </Grid>
                  <Grid size={{ xs: 12 }}>
                    <Controller
                      name="reservada"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Checkbox checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                          label="Nota reservada: omitir de certidões simples"
                        />
                      )}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
          <ActionButton title="Digitar averbação" icon={<SaveIcon />} color="primary" type="submit" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default AverbacaoDialog;
