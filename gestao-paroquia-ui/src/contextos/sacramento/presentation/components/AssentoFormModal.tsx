import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React, { useEffect } from 'react';
import { Controller, useFieldArray, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card, CardContent, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, Divider, FormControl, FormControlLabel, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import GroupsIcon from '@mui/icons-material/Groups';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import {
  CAMPOS_ESPECIFICOS,
  PAPEIS_POR_TIPO,
  TIPO_SACRAMENTO_LABEL,
  TipoSacramento,
} from '@/contextos/sacramento/domain/sacramento';
import {
  AssentoFormInput,
  assentoSchema,
  participanteVazio,
} from '@/contextos/sacramento/presentation/schemas/assentoSchema';
import ParticipanteFields from '@/contextos/sacramento/presentation/components/ParticipanteFields';

interface AssentoFormModalProps {
  open: boolean;
  tipo: TipoSacramento;
  /** Valores iniciais: formulário vazio para nova lavratura ou o rascunho em edição. */
  inicial: AssentoFormInput;
  edicao: boolean;
  podeLivroReservado: boolean;
  isPending: boolean;
  onClose: () => void;
  onSubmit: (dados: AssentoFormInput) => void;
}

/**
 * Lavratura/edição de rascunho de assento. Tudo é opcional até a homologação: a secretaria prepara
 * o termo com os dados em mãos e o Pároco confere os requisitos canônicos ao selar.
 */
export const AssentoFormModal: React.FC<AssentoFormModalProps> = ({
  open, tipo, inicial, edicao, podeLivroReservado, isPending, onClose, onSubmit,
}) => {
  const {
    control, register, handleSubmit, reset, formState: { errors, isDirty },
  } = useForm<AssentoFormInput>({ resolver: zodResolver(assentoSchema), defaultValues: inicial });
  const { fields, append, remove } = useFieldArray({ control, name: 'participantes' });

  useEffect(() => {
    if (open) reset(inicial);
  }, [open, inicial, reset]);

  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogTitle>{edicao ? 'Editar rascunho' : `Novo assento de ${TIPO_SACRAMENTO_LABEL[tipo]}`}</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <MenuBookIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Termo e celebração
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  {(['livro', 'folha', 'termo'] as const).map((campo) => (
                    <Grid key={campo} size={{ xs: 12, sm: 4 }}>
                      <TextField label={campo.charAt(0).toUpperCase() + campo.slice(1)} fullWidth size="small" {...register(campo)} />
                    </Grid>
                  ))}
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Paróquia custodiante" fullWidth size="small" {...register('paroquiaCustodiante')} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField label="Local da celebração" fullWidth size="small" {...register('localCelebracao')} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      label="Data e hora da celebração"
                      type="datetime-local"
                      fullWidth
                      size="small"
                      slotProps={{ inputLabel: { shrink: true } }}
                      {...register('dataCelebracao')}
                    />
                  </Grid>
                  {CAMPOS_ESPECIFICOS[tipo].map((campo) => (
                    <Grid key={campo.chave} size={{ xs: 12, sm: 6 }}>
                      {campo.opcoes ? (
                        <Controller
                          name={`dadosEspecificos.${campo.chave}`}
                          control={control}
                          defaultValue=""
                          render={({ field }) => (
                            <FormControl fullWidth size="small">

                              <SearchableSelect {...field} labelId={`esp-${campo.chave}`} label={campo.label}>
                                <MenuItem value="">—</MenuItem>
                                {campo.opcoes?.map((o) => (
                                  <MenuItem key={o.valor} value={o.valor}>{o.label}</MenuItem>
                                ))}
                              </SearchableSelect>
                            </FormControl>
                          )}
                        />
                      ) : (
                        <TextField label={campo.label} fullWidth size="small" {...register(`dadosEspecificos.${campo.chave}`)} />
                      )}
                    </Grid>
                  ))}
                  <Grid size={{ xs: 12 }}>
                    <Controller
                      name="dadoSensivel"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Checkbox checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                          label="Contém dado sensível (filiação adotiva): omitir de certidões simples"
                        />
                      )}
                    />
                  </Grid>
                  {podeLivroReservado && !edicao && (
                    <Grid size={{ xs: 12 }}>
                      <Controller
                        name="visibilidade"
                        control={control}
                        render={({ field }) => (
                          <FormControlLabel
                            control={
                              <Checkbox
                                checked={field.value === 'RESERVADO'}
                                onChange={(e) => field.onChange(e.target.checked ? 'RESERVADO' : 'COMUM')}
                              />
                            }
                            label="Livro reservado (invisível à secretaria; definido apenas na lavratura)"
                          />
                        )}
                      />
                    </Grid>
                  )}
                </Grid>
              </CardContent>
            </Card>
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                  <GroupsIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    Participantes
                  </Typography>
                </Stack>
                <Stack spacing={2} divider={<Divider flexItem />}>
                  {fields.map((f, index) => (
                    <ParticipanteFields
                      key={f.id}
                      index={index}
                      papeis={PAPEIS_POR_TIPO[tipo]}
                      control={control}
                      register={register}
                      errors={errors}
                      onRemove={() => remove(index)}
                    />
                  ))}
                  {errors.participantes?.root?.message && (
                    <Typography variant="caption" color="error">{errors.participantes.root.message}</Typography>
                  )}
                  {errors.participantes?.message && (
                    <Typography variant="caption" color="error">{errors.participantes.message}</Typography>
                  )}
                  <ActionButton
                    title="Adicionar participante"
                    icon={<AddIcon />}
                    color="primary"
                    variant="outlined"
                    onClick={() => append(participanteVazio(PAPEIS_POR_TIPO[tipo][0]))}
                  />
                </Stack>
              </CardContent>
            </Card>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={handleClose} />
          <ActionButton title="Salvar rascunho" icon={<SaveIcon />} color="primary" type="submit" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default AssentoFormModal;
