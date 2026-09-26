import React, { useEffect } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormHelperText,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import ChurchIcon from '@mui/icons-material/Church';
import HistoryEduIcon from '@mui/icons-material/HistoryEdu';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import { Diocese, GovernoEclesiastico, Clerigo } from '@/shared/domain/types/types';
import ActionButton from '@/shared/presentation/components/ActionButton';
import {
  governoEclesiasticoSchema,
  GovernoEclesiasticoFormSchema,
} from '@/contextos/clero/presentation/schemas/governoEclesiasticoSchema';

interface GovernoEclesiasticoModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: GovernoEclesiasticoFormSchema) => void;
  governo?: GovernoEclesiastico | null;
  dioceses?: Diocese[];
  clerigos?: Clerigo[];
  isPending: boolean;
}

const defaultValues: GovernoEclesiasticoFormSchema = {
  dioceseId: '',
  parocoId: null,
  dataPosseParoco: '',
  decretoErecaoParoquia: '',
  dataInstalacaoParoquia: '',
  livroTomboNumero: '',
};

/**
 * Modal de configuração do Governo Eclesiástico Paroquial.
 *
 * REGRA CANÔNICA (Cân. 515 a 524 CDC):
 * A paróquia é uma determinada comunidade de fiéis constituída de maneira estável
 * na Igreja particular (Diocese), cujo cuidado pastoral é confiado ao Pároco como a seu
 * pastor próprio, sob a autoridade do Bispo diocesano. A posse e a ereção canônica
 * constituem a validade jurídica de todos os atos e sacramentos.
 *
 * PADRÃO VISUAL & UX (DESIGN_SYSTEM.md):
 * - Dialog compacto maxWidth="md" com DialogTitle sem Typography aninhada.
 * - Form com noValidate delegando integridade ao resolver Zod (governoEclesiasticoSchema).
 * - DialogContent com dividers e DIALOG_CONTENT_SCROLL_SX.
 * - Decomposição semântica em dois Cards coesos:
 *   1. Vinculação Canônica e Provisão do Pároco (Diocese, Pároco e Posse).
 *   2. Registro Histórico e Ereção Canônica (Instalação, Decreto e Livro Tombo).
 * - Todos os inputs (FormControl, Select, TextField) com size="small".
 * - Botões canônicos ActionButton no DialogActions padrão (Cancelar / Salvar).
 */
export const GovernoEclesiasticoModal: React.FC<GovernoEclesiasticoModalProps> = ({
  open,
  onClose,
  onSave,
  governo,
  dioceses = [],
  clerigos = [],
  isPending,
}) => {
  const {
    control,
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<GovernoEclesiasticoFormSchema>({
    resolver: zodResolver(governoEclesiasticoSchema),
    defaultValues,
  });

  useEffect(() => {
    if (governo) {
      reset({
        dioceseId: governo.dioceseId || '',
        parocoId: governo.parocoId || null,
        dataPosseParoco: governo.dataPosseParoco || '',
        decretoErecaoParoquia: governo.decretoErecaoParoquia || '',
        dataInstalacaoParoquia: governo.dataInstalacaoParoquia || '',
        livroTomboNumero: governo.livroTomboNumero || '',
      });
    } else {
      reset(defaultValues);
    }
  }, [governo, open, reset]);

  const handleClose = () => confirmDiscard(isDirty, onClose);

  const onSubmit = (data: GovernoEclesiasticoFormSchema) => {
    onSave(data);
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Configurar governo eclesiástico
      </DialogTitle>

      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            {/* Seção 1: Vinculação Canônica & Provisão do Pároco */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <ChurchIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Vinculação Canônica e Provisão do Pároco
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <Controller
                      name="dioceseId"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small" error={Boolean(errors.dioceseId)}>
                          <InputLabel id="diocese-select-label">Diocese Circunscricional *</InputLabel>
                          <Select {...field} labelId="diocese-select-label" label="Diocese Circunscricional *">
                            {dioceses.map((d) => (
                              <MenuItem key={d.id} value={d.id}>
                                {d.nome} - {d.bispoDiocesano} ({d.uf || 'BR'})
                              </MenuItem>
                            ))}
                          </Select>
                          {errors.dioceseId && <FormHelperText>{errors.dioceseId.message}</FormHelperText>}
                        </FormControl>
                      )}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 8 }}>
                    <Controller
                      name="parocoId"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small">
                          <InputLabel id="paroco-select-label">Pároco (Administrador Canônico)</InputLabel>
                          <Select
                            {...field}
                            value={field.value || ''}
                            onChange={(e) => field.onChange(e.target.value || null)}
                            labelId="paroco-select-label"
                            label="Pároco (Administrador Canônico)"
                          >
                            <MenuItem value="">Nenhum designado</MenuItem>
                            {clerigos.map((c) => (
                              <MenuItem key={c.clerigoId} value={c.clerigoId}>
                                {c.nomeEclesiastico} ({c.grauOrdem})
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      )}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      {...register('dataPosseParoco')}
                      label="Data da Posse do Pároco"
                      type="date"
                      fullWidth
                      size="small"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataPosseParoco)}
                      helperText={errors.dataPosseParoco?.message}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Seção 2: Registro Histórico & Ereção Canônica */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <HistoryEduIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Registro Histórico e Ereção Canônica
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      {...register('dataInstalacaoParoquia')}
                      label="Data de Instalação da Paróquia"
                      type="date"
                      fullWidth
                      size="small"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataInstalacaoParoquia)}
                      helperText={errors.dataInstalacaoParoquia?.message}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      {...register('decretoErecaoParoquia')}
                      label="Decreto Canônico de Ereção"
                      placeholder="Ex: Decreto Diocesano nº 14/1950"
                      fullWidth
                      size="small"
                      error={Boolean(errors.decretoErecaoParoquia)}
                      helperText={errors.decretoErecaoParoquia?.message}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      {...register('livroTomboNumero')}
                      label="Nº do Livro Tombo / Registro"
                      placeholder="Ex: Livro 01, fl. 05"
                      fullWidth
                      size="small"
                      error={Boolean(errors.livroTomboNumero)}
                      helperText={errors.livroTomboNumero?.message}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>

        <DialogActions>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={handleClose}
            color="secondary"
            disabled={isPending}
          />
          <ActionButton
            type="submit"
            title="Salvar"
            icon={<SaveIcon />}
            color="primary"
            disabled={isPending}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default GovernoEclesiasticoModal;
