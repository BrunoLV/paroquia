import React, { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Chip,
  CircularProgress,
  Divider,
  Drawer,
  FormControl,
  FormControlLabel,
  FormHelperText,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import { reservaSchema, ReservaFormData } from '@/contextos/reserva/presentation/schemas/reservaSchema';
import { TipoItemReserva } from '@/shared/domain/types/types';

interface ReservaFormDrawerProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: ReservaFormData) => void;
  isLoading?: boolean;
  locais: Array<{ id: string; nome: string; bufferPadraoMinutos?: number }>;
  ativos: Array<{ id: string; nome: string; codigoTombamento?: string }>;
  pastorais: Array<{ id: string; nome: string }>;
  eventos?: Array<{ id: string; titulo: string }>;
  preselectedTipo?: TipoItemReserva;
  preselectedItemId?: string;
}

export const ReservaFormDrawer: React.FC<ReservaFormDrawerProps> = ({
  open,
  onClose,
  onSubmit,
  isLoading,
  locais,
  ativos,
  pastorais,
  eventos = [],
  preselectedTipo,
  preselectedItemId,
}) => {
  const {
    control,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isDirty },
  } = useForm<ReservaFormData>({
    resolver: zodResolver(reservaSchema),
    defaultValues: {
      tipoItem: preselectedTipo ?? 'LOCAL',
      itemId: preselectedItemId ?? '',
      dataHoraInicio: '',
      dataHoraFim: '',
      bufferMinutos: 0,
      descricao: '',
      pastoralMovimentoId: '',
      eventoId: null,
      isRecorrente: false,
      tipoRecorrencia: 'SEMANAL',
      quantidadeOcorrencias: 4,
    },
  });

  const selectedTipo = watch('tipoItem');
  const selectedItemId = watch('itemId');
  const isRecorrente = watch('isRecorrente');

  useEffect(() => {
    if (open) {
      if (preselectedTipo) setValue('tipoItem', preselectedTipo, { shouldDirty: false });
      if (preselectedItemId) setValue('itemId', preselectedItemId, { shouldDirty: false });
    } else {
      reset();
    }
  }, [open, preselectedTipo, preselectedItemId, setValue, reset]);

  // Se o item selecionado for um local com buffer padrao, auto-preenche o buffer
  useEffect(() => {
    if (selectedTipo === 'LOCAL' && selectedItemId) {
      const loc = locais.find((l) => l.id === selectedItemId);
      if (loc && loc.bufferPadraoMinutos) {
        setValue('bufferMinutos', loc.bufferPadraoMinutos, { shouldDirty: false });
      }
    }
  }, [selectedTipo, selectedItemId, locais, setValue]);

  const aplicarTurno = (turno: 'MANHA' | 'TARDE' | 'NOITE' | 'MADRUGADA' | 'DIA_INTEIRO') => {
    const inicioAtual = watch('dataHoraInicio');
    const baseDate = inicioAtual && inicioAtual.includes('T')
      ? inicioAtual.slice(0, 10)
      : new Date().toISOString().slice(0, 10);

    switch (turno) {
      case 'MANHA':
        setValue('dataHoraInicio', `${baseDate}T06:00`, { shouldDirty: true, shouldValidate: true });
        setValue('dataHoraFim', `${baseDate}T12:00`, { shouldDirty: true, shouldValidate: true });
        break;
      case 'TARDE':
        setValue('dataHoraInicio', `${baseDate}T12:00`, { shouldDirty: true, shouldValidate: true });
        setValue('dataHoraFim', `${baseDate}T18:00`, { shouldDirty: true, shouldValidate: true });
        break;
      case 'NOITE':
        setValue('dataHoraInicio', `${baseDate}T18:00`, { shouldDirty: true, shouldValidate: true });
        setValue('dataHoraFim', `${baseDate}T23:59`, { shouldDirty: true, shouldValidate: true });
        break;
      case 'MADRUGADA':
        setValue('dataHoraInicio', `${baseDate}T00:00`, { shouldDirty: true, shouldValidate: true });
        setValue('dataHoraFim', `${baseDate}T06:00`, { shouldDirty: true, shouldValidate: true });
        break;
      case 'DIA_INTEIRO':
        setValue('dataHoraInicio', `${baseDate}T06:00`, { shouldDirty: true, shouldValidate: true });
        setValue('dataHoraFim', `${baseDate}T23:59`, { shouldDirty: true, shouldValidate: true });
        break;
    }
  };

  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={handleClose}
      slotProps={{
        paper: { sx: { width: { xs: '100%', sm: 560, md: 640 }, p: { xs: 2.5, sm: 3.5 } } },
      }}
    >
      <Stack spacing={2} sx={{ height: '100%', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
            Nova Reserva de Recurso
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Aloque espaços físicos ou ativos patrimoniais para suas atividades pastorais ou eventos.
          </Typography>

          <form id="reserva-form" onSubmit={handleSubmit(onSubmit)}>
            <Stack spacing={2.5}>
              <Controller
                name="tipoItem"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={Boolean(errors.tipoItem)}>
                    <InputLabel>Tipo de Recurso</InputLabel>
                    <Select
                      {...field}
                      label="Tipo de Recurso"
                      onChange={(e) => {
                        field.onChange(e);
                        setValue('itemId', '');
                      }}
                    >
                      <MenuItem value="LOCAL">Espaço Físico / Local</MenuItem>
                      <MenuItem value="ATIVO">Ativo Patrimonial Móvel</MenuItem>
                    </Select>
                    {errors.tipoItem && <FormHelperText>{errors.tipoItem.message}</FormHelperText>}
                  </FormControl>
                )}
              />

              <Controller
                name="itemId"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={Boolean(errors.itemId)}>
                    <InputLabel>{selectedTipo === 'LOCAL' ? 'Selecionar Espaço' : 'Selecionar Ativo'}</InputLabel>
                    <Select {...field} label={selectedTipo === 'LOCAL' ? 'Selecionar Espaço' : 'Selecionar Ativo'}>
                      {selectedTipo === 'LOCAL'
                        ? locais.map((l) => (
                            <MenuItem key={l.id} value={l.id}>
                              {l.nome}
                            </MenuItem>
                          ))
                        : ativos.map((a) => (
                            <MenuItem key={a.id} value={a.id}>
                              {a.nome} {a.codigoTombamento ? `(Nº ${a.codigoTombamento})` : ''}
                            </MenuItem>
                          ))}
                    </Select>
                    {errors.itemId && <FormHelperText>{errors.itemId.message}</FormHelperText>}
                  </FormControl>
                )}
              />

              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600, display: 'block', mb: 1 }}>
                  Definição Rápida de Turno / Período:
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  <Chip label="Manhã (06h–12h)" size="small" variant="outlined" clickable onClick={() => aplicarTurno('MANHA')} />
                  <Chip label="Tarde (12h–18h)" size="small" variant="outlined" clickable onClick={() => aplicarTurno('TARDE')} />
                  <Chip label="Noite (18h–23h59)" size="small" variant="outlined" clickable onClick={() => aplicarTurno('NOITE')} />
                  <Chip label="Madrugada (00h–06h)" size="small" variant="outlined" clickable onClick={() => aplicarTurno('MADRUGADA')} />
                  <Chip label="Dia Inteiro (06h–23h59)" size="small" variant="outlined" clickable onClick={() => aplicarTurno('DIA_INTEIRO')} />
                </Box>
              </Box>

              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <Controller
                  name="dataHoraInicio"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      size="small"
                      type="datetime-local"
                      label="Início"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataHoraInicio)}
                      helperText={errors.dataHoraInicio?.message}
                    />
                  )}
                />

                <Controller
                  name="dataHoraFim"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      size="small"
                      type="datetime-local"
                      label="Término"
                      slotProps={{ inputLabel: { shrink: true } }}
                      error={Boolean(errors.dataHoraFim)}
                      helperText={errors.dataHoraFim?.message}
                    />
                  )}
                />
              </Stack>

              <Controller
                name="bufferMinutos"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    size="small"
                    type="number"
                    label="Margem de Carência (Buffer em Minutos)"
                    helperText={
                      errors.bufferMinutos?.message ||
                      'Tempo de limpeza, preparação ou deslocamento logístico.'
                    }
                    error={Boolean(errors.bufferMinutos)}
                  />
                )}
              />

              <Controller
                name="pastoralMovimentoId"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={Boolean(errors.pastoralMovimentoId)}>
                    <InputLabel>Pastoral / Movimento</InputLabel>
                    <Select {...field} label="Pastoral / Movimento">
                      {pastorais.map((p) => (
                        <MenuItem key={p.id} value={p.id}>
                          {p.nome}
                        </MenuItem>
                      ))}
                    </Select>
                    {errors.pastoralMovimentoId && (
                      <FormHelperText>{errors.pastoralMovimentoId.message}</FormHelperText>
                    )}
                  </FormControl>
                )}
              />

              <Controller
                name="eventoId"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth size="small">
                    <InputLabel>Vincular a Evento do Calendário (Opcional)</InputLabel>
                    <Select
                      {...field}
                      value={field.value ?? ''}
                      onChange={(e) => field.onChange(e.target.value || null)}
                      label="Vincular a Evento do Calendário (Opcional)"
                    >
                      <MenuItem value="">Nenhum (Reserva Avulsa)</MenuItem>
                      {eventos.map((ev) => (
                        <MenuItem key={ev.id} value={ev.id}>
                          {ev.titulo}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
              />

              <Controller
                name="descricao"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    size="small"
                    multiline
                    rows={3}
                    label="Finalidade / Justificativa"
                    placeholder="Ex: Ensaio geral dos coroinhas para o Tríduo Pascal."
                    error={Boolean(errors.descricao)}
                    helperText={errors.descricao?.message}
                  />
                )}
              />

              <Divider />

              <Controller
                name="isRecorrente"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch {...field} checked={field.value} />}
                    label="Repetir em lote (Agendamento Recorrente)"
                  />
                )}
              />

              {isRecorrente && (
                <>
                  <Stack direction="row" spacing={2}>
                  <Controller
                    name="tipoRecorrencia"
                    control={control}
                    render={({ field }) => (
                      <FormControl fullWidth size="small">
                        <InputLabel>Frequência</InputLabel>
                        <Select {...field} label="Frequência">
                          <MenuItem value="SEMANAL">Semanal</MenuItem>
                          <MenuItem value="QUINZENAL">Quinzenal</MenuItem>
                          <MenuItem value="MENSAL">Mensal</MenuItem>
                        </Select>
                      </FormControl>
                    )}
                  />

                  <Controller
                    name="quantidadeOcorrencias"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        fullWidth
                        size="small"
                        type="number"
                        label="Ocorrências"
                        error={Boolean(errors.quantidadeOcorrencias)}
                        helperText={errors.quantidadeOcorrencias?.message}
                      />
                    )}
                  />
                </Stack>
                <Alert severity="info" sx={{ borderRadius: 2 }}>
                  Esta reserva será programada {watch('tipoRecorrencia') === 'SEMANAL' ? 'a cada semana' : watch('tipoRecorrencia') === 'QUINZENAL' ? 'a cada 15 dias' : 'a cada mês'}, gerando {watch('quantidadeOcorrencias') || 1} reservas sequenciais na agenda.
                </Alert>
              </>
            )}
            </Stack>
          </form>
        </Box>

        <Box sx={{ pt: 2, borderTop: '1px solid', borderColor: 'divider' }}>
          <Stack direction="row" spacing={2} sx={{ justifyContent: 'flex-end' }}>
            <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={handleClose} disabled={isLoading} />
            <ActionButton
              title="Confirmar Solicitação"
              icon={isLoading ? <CircularProgress size={16} /> : <SaveIcon />}
              color="primary"
              type="submit"
              form="reserva-form"
              disabled={isLoading}
            />
          </Stack>
        </Box>
      </Stack>
    </Drawer>
  );
};
