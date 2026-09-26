import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import { formatCnpj } from '@/shared/utils/masks';
import { Diocese } from '@/shared/domain/types/types';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { dioceseSchema, DioceseFormSchema } from '@/contextos/clero/presentation/schemas/governoEclesiasticoSchema';

interface DioceseFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: DioceseFormSchema) => void;
  diocese?: Diocese | null;
  isPending: boolean;
}

const defaultValues: DioceseFormSchema = {
  nome: '',
  bispoDiocesano: '',
  cnpjMitra: '',
  cidade: '',
  uf: '',
  contato: '',
  ativo: true,
};

/**
 * Modal para criação e edição de Diocese (Circunscrição Diocesana).
 *
 * REGRA CANÔNICA (Cân. 369 CDC):
 * A Diocese é a porção do povo de Deus confiada ao Bispo diocesano. A paróquia
 * é canonicamente vinculada à sua respectiva Diocese sede.
 *
 * PADRÃO VISUAL & UX (DESIGN_SYSTEM.md):
 * - Dialog compacto maxWidth="sm" com DialogTitle sem Typography aninhada.
 * - Form com noValidate delegando integridade ao resolver Zod (dioceseSchema).
 * - DialogContent com dividers e DIALOG_CONTENT_SCROLL_SX.
 * - Campos agrupados em Card delineado com cabeçalho semântico e inputs size="small".
 * - Máscara automática no CNPJ da Mitra e conversão de UF em maiúsculas.
 * - Botões canônicos ActionButton no DialogActions padrão (Cancelar / Salvar).
 */
export const DioceseFormModal: React.FC<DioceseFormModalProps> = ({
  open,
  onClose,
  onSave,
  diocese,
  isPending,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isDirty },
  } = useForm<DioceseFormSchema>({
    resolver: zodResolver(dioceseSchema),
    defaultValues,
  });

  useEffect(() => {
    if (diocese) {
      reset({
        nome: diocese.nome,
        bispoDiocesano: diocese.bispoDiocesano,
        cnpjMitra: diocese.cnpjMitra ? formatCnpj(diocese.cnpjMitra) : '',
        cidade: diocese.cidade || '',
        uf: diocese.uf || '',
        contato: diocese.contato || '',
        ativo: diocese.ativo,
      });
    } else {
      reset(defaultValues);
    }
  }, [diocese, open, reset]);

  const handleClose = () => confirmDiscard(isDirty, onClose);

  const onSubmit = (data: DioceseFormSchema) => {
    onSave(data);
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {diocese ? 'Editar diocese' : 'Nova diocese'}
      </DialogTitle>

      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <AccountBalanceIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Dados da Circunscrição Diocesana
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <TextField
                      {...register('nome')}
                      label="Nome da Diocese"
                      placeholder="Ex: Diocese de Santos"
                      fullWidth
                      size="small"
                      required
                      error={Boolean(errors.nome)}
                      helperText={errors.nome?.message}
                    />
                  </Grid>

                  <Grid size={{ xs: 12 }}>
                    <TextField
                      {...register('bispoDiocesano')}
                      label="Bispo Diocesano"
                      placeholder="Ex: Dom Tarcísio Scaramussa"
                      fullWidth
                      size="small"
                      required
                      error={Boolean(errors.bispoDiocesano)}
                      helperText={errors.bispoDiocesano?.message}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      {...register('cnpjMitra', {
                        onChange: (e) => {
                          const masked = formatCnpj(e.target.value);
                          setValue('cnpjMitra', masked, { shouldDirty: true, shouldValidate: true });
                        },
                      })}
                      label="CNPJ da Mitra Diocesana"
                      placeholder="00.000.000/0000-00"
                      fullWidth
                      size="small"
                      error={Boolean(errors.cnpjMitra)}
                      helperText={errors.cnpjMitra?.message}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 4 }}>
                    <TextField
                      {...register('cidade')}
                      label="Cidade Sede"
                      fullWidth
                      size="small"
                      error={Boolean(errors.cidade)}
                      helperText={errors.cidade?.message}
                    />
                  </Grid>

                  <Grid size={{ xs: 12, sm: 2 }}>
                    <TextField
                      {...register('uf', {
                        onChange: (e) => {
                          setValue('uf', e.target.value.toUpperCase(), { shouldDirty: true, shouldValidate: true });
                        },
                      })}
                      label="UF"
                      fullWidth
                      size="small"
                      slotProps={{ htmlInput: { maxLength: 2 } }}
                      error={Boolean(errors.uf)}
                      helperText={errors.uf?.message}
                    />
                  </Grid>

                  <Grid size={{ xs: 12 }}>
                    <TextField
                      {...register('contato')}
                      label="Contato / Cúria Diocesana"
                      placeholder="E-mail ou telefone"
                      fullWidth
                      size="small"
                      error={Boolean(errors.contato)}
                      helperText={errors.contato?.message}
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

export default DioceseFormModal;
