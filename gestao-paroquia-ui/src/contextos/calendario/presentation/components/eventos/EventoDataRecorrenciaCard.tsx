import React, { useMemo, useCallback } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  Collapse,
  FormControlLabel,
  Grid,
  InputAdornment,
  Stack,
  Switch,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import RepeatIcon from '@mui/icons-material/Repeat';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import BedtimeIcon from '@mui/icons-material/Bedtime';
import NightsStayIcon from '@mui/icons-material/NightsStay';
import FlareIcon from '@mui/icons-material/Flare';
import TodayIcon from '@mui/icons-material/Today';
import { Controller, useWatch, type UseFormReturn } from 'react-hook-form';
import FormularioRecorrencia from '@/contextos/calendario/presentation/components/FormularioRecorrencia';
import { EventoFormInput, TipoHorarioForm } from '@/contextos/calendario/presentation/schemas/eventoSchema';
import { METADADOS_TIPO_HORARIO } from '@/contextos/calendario/presentation/utils/horarioEventoUtils';
import { formatLocalDate } from '@/shared/utils/recurrenceUtils';

import { alpha } from '@mui/material/styles';

interface EventoDataRecorrenciaCardProps {
  form: UseFormReturn<EventoFormInput>;
  disabled: boolean;
  isRecurring: boolean;
  isEditing: boolean;
  isParoco: boolean;
  isSecretario?: boolean;
  isClosedYear?: boolean;
}

const PRESETS_DURACAO = [
  { label: '+30 min', horas: 0, minutos: 30 },
  { label: '+45 min', horas: 0, minutos: 45 },
  { label: '+1 hora', horas: 1, minutos: 0 },
  { label: '+1h 30 min', horas: 1, minutos: 30 },
  { label: '+2 horas', horas: 2, minutos: 0 },
  { label: '+3 horas', horas: 3, minutos: 0 },
  { label: '+4 horas', horas: 4, minutos: 0 },
];

const PERIODOS_DIA: { tipo: TipoHorarioForm; label: string; icon: React.ReactNode; info: string }[] = [
  { tipo: 'MADRUGADA', label: 'Madrugada', icon: <NightsStayIcon fontSize="small" />, info: '00:00 às 05:59' },
  { tipo: 'MANHA', label: 'Manhã', icon: <FlareIcon fontSize="small" />, info: '06:00 às 11:59' },
  { tipo: 'TARDE', label: 'Tarde', icon: <WbSunnyIcon fontSize="small" />, info: '12:00 às 17:59' },
  { tipo: 'NOITE', label: 'Noite', icon: <BedtimeIcon fontSize="small" />, info: '18:00 às 23:59' },
];

/** Seção "Data e recorrência": suporte a horários personalizados com assistente de duração, dia inteiro e períodos pré-definidos múltiplos. */
const EventoDataRecorrenciaCard: React.FC<EventoDataRecorrenciaCardProps> = ({
  form,
  disabled,
  isRecurring,
  isEditing,
  isParoco,
  isSecretario = false,
  isClosedYear = false,
}) => {
  const { register, control, setValue } = form;

  const [tipoHorario, periodos, startDate, endDate, endTime, eventDate, duracaoHoras, duracaoMinutos] = useWatch({
    control,
    name: ['tipoHorario', 'periodos', 'startDate', 'endDate', 'endTime', 'eventDate', 'duracaoHoras', 'duracaoMinutos'],
  });

  const periodosSelecionados: TipoHorarioForm[] = useMemo(() => {
    if (Array.isArray(periodos) && periodos.length > 0) {
      return periodos;
    }
    if (tipoHorario && ['MADRUGADA', 'MANHA', 'TARDE', 'NOITE'].includes(tipoHorario)) {
      return [tipoHorario as TipoHorarioForm];
    }
    return [];
  }, [periodos, tipoHorario]);

  const isModoPeriodo = tipoHorario !== 'PERSONALIZADO' && tipoHorario !== 'DIA_INTEIRO';

  // Aplica duração em horas e minutos calculando novo término com base no início
  const aplicarDuracao = useCallback(
    (horas: number, minutos: number) => {
      setValue('duracaoHoras', horas);
      setValue('duracaoMinutos', minutos);

      if (!startDate) return;
      const start = new Date(startDate);
      if (isNaN(start.getTime())) return;

      const end = new Date(start.getTime() + (horas * 60 + minutos) * 60000);

      if (isRecurring) {
        const horaStr = `${String(end.getHours()).padStart(2, '0')}:${String(end.getMinutes()).padStart(2, '0')}`;
        setValue('endTime', horaStr);
      } else {
        setValue('endDate', formatLocalDate(end));
      }
    },
    [startDate, isRecurring, setValue]
  );

  // Manipula a troca de modo de horário com migração coerente de datas
  const handleTrocaModoHorario = (novoModo: 'PERSONALIZADO' | 'DIA_INTEIRO' | 'PERIODO') => {
    if (novoModo === 'PERSONALIZADO') {
      setValue('tipoHorario', 'PERSONALIZADO');
      setValue('periodos', []);
      if (eventDate && !startDate) {
        setValue('startDate', `${eventDate}T09:00`);
        setValue('endDate', `${eventDate}T10:00`);
      }
    } else if (novoModo === 'DIA_INTEIRO') {
      setValue('tipoHorario', 'DIA_INTEIRO');
      setValue('periodos', []);
      if (startDate && !eventDate) {
        setValue('eventDate', startDate.split('T')[0]);
      }
    } else if (novoModo === 'PERIODO') {
      const turnosNovos: TipoHorarioForm[] = periodosSelecionados.length > 0 ? periodosSelecionados : ['MANHA'];
      setValue('tipoHorario', turnosNovos[0]);
      setValue('periodos', turnosNovos);
      if (startDate && !eventDate) {
        setValue('eventDate', startDate.split('T')[0]);
      }
    }
  };

  const handleTogglePeriodo = (tipo: TipoHorarioForm) => {
    let novos: TipoHorarioForm[];
    if (periodosSelecionados.includes(tipo)) {
      novos = periodosSelecionados.filter((p) => p !== tipo);
      // Evita deixar sem nenhum selecionado
      if (novos.length === 0) {
        novos = [tipo];
      }
    } else {
      novos = [...periodosSelecionados, tipo];
    }
    setValue('periodos', novos);
    setValue('tipoHorario', novos[0]);
  };

  // Resumo amigável do intervalo calculado no modo personalizado
  const resumoIntervaloPersonalizado = useMemo(() => {
    if (!startDate) return null;
    const start = new Date(startDate);
    if (isNaN(start.getTime())) return null;

    let end: Date | null = null;
    if (isRecurring && endTime) {
      const [h, m] = endTime.split(':').map(Number);
      end = new Date(start);
      end.setHours(h, m, 0, 0);
    } else if (endDate) {
      end = new Date(endDate);
    }

    if (!end || isNaN(end.getTime())) return null;

    const diffMinutos = Math.round((end.getTime() - start.getTime()) / 60000);
    if (diffMinutos <= 0) return null;

    const h = Math.floor(diffMinutos / 60);
    const m = diffMinutos % 60;
    const duracaoTexto = h > 0 ? (m > 0 ? `${h}h ${m}min` : `${h}h`) : `${m}min`;

    const startHora = start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    const endHora = end.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

    return `Horário: ${startHora} às ${endHora} (${duracaoTexto})`;
  }, [startDate, endDate, endTime, isRecurring]);

  return (
    <Card variant="outlined" sx={{ borderRadius: 2 }}>
      <CardContent sx={{ p: { xs: 2.5, sm: 3 }, '&:last-child': { pb: { xs: 2.5, sm: 3 } } }}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: 2.5 }}>
          <RepeatIcon color="primary" fontSize="small" />
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'primary.main' }}>
            Data e Horário do Evento
          </Typography>
        </Stack>

        {/* Seletor do Tipo de Horário */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary', display: 'block', mb: 1.2 }}>
            Formato do Horário
          </Typography>
          <ToggleButtonGroup
            value={isModoPeriodo ? 'PERIODO' : tipoHorario}
            exclusive
            onChange={(_, val) => val && handleTrocaModoHorario(val)}
            size="small"
            fullWidth
            disabled={disabled}
            sx={{ mb: isModoPeriodo ? 2 : 0 }}
          >
            <ToggleButton value="PERSONALIZADO" sx={{ textTransform: 'none', py: 1.2, fontWeight: 600 }}>
              <AccessTimeIcon fontSize="small" sx={{ mr: 1 }} />
              Personalizado
            </ToggleButton>
            <ToggleButton value="DIA_INTEIRO" sx={{ textTransform: 'none', py: 1.2, fontWeight: 600 }}>
              <TodayIcon fontSize="small" sx={{ mr: 1 }} />
              Dia Inteiro
            </ToggleButton>
            <ToggleButton value="PERIODO" sx={{ textTransform: 'none', py: 1.2, fontWeight: 600 }}>
              <WbSunnyIcon fontSize="small" sx={{ mr: 1 }} />
              Por Período
            </ToggleButton>
          </ToggleButtonGroup>

          {/* Sub-seleção múltipla quando o usuário escolhe "Por Período" */}
          {isModoPeriodo && (
            <Box>
              <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary', display: 'block', mb: 1 }}>
                Selecione um ou mais turnos desejados (múltipla escolha permitida):
              </Typography>
              <Grid container spacing={1.5}>
                {PERIODOS_DIA.map((p) => {
                  const isSelected = periodosSelecionados.includes(p.tipo);
                  return (
                    <Grid size={{ xs: 6, sm: 3 }} key={p.tipo}>
                      <Box
                        onClick={() => !disabled && handleTogglePeriodo(p.tipo)}
                        sx={{
                          p: 1.5,
                          cursor: disabled ? 'not-allowed' : 'pointer',
                          borderRadius: 2,
                          border: '2px solid',
                          borderColor: isSelected ? 'primary.main' : 'divider',
                          bgcolor: isSelected
                            ? (theme) => alpha(theme.palette.primary.main, 0.08)
                            : 'background.paper',
                          transition: 'all 0.2s ease-in-out',
                          display: 'flex',
                          alignItems: 'center',
                          gap: 1.2,
                          '&:hover': {
                            borderColor: isSelected ? 'primary.main' : 'primary.light',
                            bgcolor: isSelected
                              ? (theme) => alpha(theme.palette.primary.main, 0.12)
                              : 'action.hover',
                          },
                        }}
                      >
                        <Box sx={{ color: isSelected ? 'primary.main' : 'text.secondary' }}>
                          {p.icon}
                        </Box>
                        <Box sx={{ textAlign: 'left' }}>
                          <Typography
                            variant="body2"
                            sx={{
                              fontWeight: isSelected ? 700 : 600,
                              color: isSelected ? 'primary.main' : 'text.primary',
                              lineHeight: 1.2,
                            }}
                          >
                            {p.label}
                          </Typography>
                          <Typography variant="caption" sx={{ fontSize: '0.72rem', color: 'text.secondary' }}>
                            {p.info}
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>
                  );
                })}
              </Grid>
            </Box>
          )}
        </Box>

        <Grid container spacing={2.5}>
          {/* Se Dia Inteiro ou Período: Exibe apenas Data do Evento */}
          {tipoHorario !== 'PERSONALIZADO' && (
            <>
              <Grid size={{ xs: 12 }}>
                <TextField
                  label="Data do Evento"
                  type="date"
                  required
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  disabled={disabled}
                  size="small"
                  value={eventDate || (startDate ? startDate.split('T')[0] : '')}
                  onChange={(e) => {
                    setValue('eventDate', e.target.value);
                    setValue('startDate', `${e.target.value}T00:00`);
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Alert severity="info" variant="outlined" sx={{ py: 1.2, px: 2, alignItems: 'center' }}>
                  {tipoHorario === 'DIA_INTEIRO' ? (
                    <>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        Dia Inteiro (00:00 às 23:59)
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        O evento ocupará todas as horas do dia. Não é necessário informar horários de término.
                      </Typography>
                    </>
                  ) : periodosSelecionados.length > 1 ? (
                    <>
                      <Typography variant="body2" sx={{ fontWeight: 700 }}>
                        {periodosSelecionados.length} turnos selecionados:{' '}
                        {periodosSelecionados.map((p) => METADADOS_TIPO_HORARIO[p]?.label).join(' e ')}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                        O evento ocupará cada turno indicado ({periodosSelecionados
                          .map((p) => `${METADADOS_TIPO_HORARIO[p]?.label}: ${METADADOS_TIPO_HORARIO[p]?.descricao}`)
                          .join(' · ')}). Os turnos intermediários não selecionados permanecerão desimpedidos para outras atividades.
                      </Typography>
                    </>
                  ) : (
                    <>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {METADADOS_TIPO_HORARIO[periodosSelecionados[0] || 'MANHA']?.label}:{' '}
                        {METADADOS_TIPO_HORARIO[periodosSelecionados[0] || 'MANHA']?.descricao}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Horário fixo do período selecionado. Não é necessário informar horário de término.
                      </Typography>
                    </>
                  )}
                </Alert>
              </Grid>
            </>
          )}

          {/* Se Personalizado: Início e Fim + Assistente de Duração */}
          {tipoHorario === 'PERSONALIZADO' && (
            <>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Data e Hora de Início"
                  type="datetime-local"
                  required
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                  disabled={disabled}
                  size="small"
                  {...register('startDate')}
                  onChange={(e) => {
                    register('startDate').onChange(e);
                    if (duracaoHoras !== undefined || duracaoMinutos !== undefined) {
                      const h = duracaoHoras || 1;
                      const m = duracaoMinutos || 0;
                      const start = new Date(e.target.value);
                      if (!isNaN(start.getTime())) {
                        const end = new Date(start.getTime() + (h * 60 + m) * 60000);
                        if (isRecurring) {
                          setValue(
                            'endTime',
                            `${String(end.getHours()).padStart(2, '0')}:${String(end.getMinutes()).padStart(2, '0')}`
                          );
                        } else {
                          setValue('endDate', formatLocalDate(end));
                        }
                      }
                    }
                  }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                {isRecurring ? (
                  <TextField
                    label="Hora de Término"
                    type="time"
                    required
                    slotProps={{ inputLabel: { shrink: true } }}
                    fullWidth
                    disabled={disabled}
                    size="small"
                    {...register('endTime')}
                  />
                ) : (
                  <TextField
                    label="Data e Hora de Término"
                    type="datetime-local"
                    required
                    slotProps={{ inputLabel: { shrink: true } }}
                    fullWidth
                    disabled={disabled}
                    size="small"
                    {...register('endDate')}
                  />
                )}
              </Grid>

              {/* Assistente de Duração */}
              <Grid size={{ xs: 12 }}>
                <Box
                  sx={{
                    p: { xs: 2, sm: 2.5 },
                    bgcolor: (theme) => alpha(theme.palette.primary.main, 0.02),
                    borderRadius: 2,
                    border: '1px solid',
                    borderColor: 'divider',
                  }}
                >
                  {/* Cabeçalho do Assistente com Chip de Resumo */}
                  <Stack
                    direction={{ xs: 'column', sm: 'row' }}
                    spacing={1.5}
                    sx={{ alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between', mb: 2 }}
                  >
                    <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                      <AccessTimeIcon color="primary" fontSize="small" />
                      <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'text.primary' }}>
                        Assistente de Duração Rápida
                      </Typography>
                    </Stack>
                    {resumoIntervaloPersonalizado && (
                      <Chip
                        icon={<AccessTimeIcon fontSize="small" />}
                        label={resumoIntervaloPersonalizado}
                        size="small"
                        color="primary"
                        variant="outlined"
                        sx={{
                          fontWeight: 600,
                          bgcolor: (theme) => alpha(theme.palette.primary.main, 0.06),
                          borderColor: (theme) => alpha(theme.palette.primary.main, 0.3),
                        }}
                      />
                    )}
                  </Stack>

                  {/* Grid Estruturado: Durações Sugeridas (Presets) + Ajuste Fino */}
                  <Grid container spacing={2.5} sx={{ alignItems: 'flex-start' }}>
                    {/* Coluna 1: Presets Rápidos */}
                    <Grid size={{ xs: 12, md: 7.5 }}>
                      <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary', display: 'block', mb: 1 }}>
                        Durações sugeridas:
                      </Typography>
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                        {PRESETS_DURACAO.map((p) => {
                          const isSelected = duracaoHoras === p.horas && duracaoMinutos === p.minutos;
                          return (
                            <Chip
                              key={p.label}
                              label={p.label}
                              size="small"
                              clickable={!disabled}
                              variant={isSelected ? 'filled' : 'outlined'}
                              color={isSelected ? 'primary' : 'default'}
                              onClick={() => aplicarDuracao(p.horas, p.minutos)}
                              sx={{
                                fontWeight: isSelected ? 700 : 500,
                                transition: 'all 0.2s ease',
                                borderColor: isSelected ? 'primary.main' : 'divider',
                                bgcolor: isSelected ? 'primary.main' : 'background.paper',
                                '&:hover': {
                                  bgcolor: isSelected ? 'primary.dark' : 'action.hover',
                                },
                              }}
                            />
                          );
                        })}
                      </Box>
                    </Grid>

                    {/* Coluna 2: Ajuste Fino */}
                    <Grid size={{ xs: 12, md: 4.5 }}>
                      <Typography variant="caption" sx={{ fontWeight: 600, color: 'text.secondary', display: 'block', mb: 1 }}>
                        Ajuste fino:
                      </Typography>
                      <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                        <TextField
                          label="Horas"
                          type="number"
                          size="small"
                          disabled={disabled}
                          value={duracaoHoras ?? 1}
                          onChange={(e) => aplicarDuracao(Math.max(0, parseInt(e.target.value, 10) || 0), duracaoMinutos || 0)}
                          slotProps={{
                            input: {
                              endAdornment: <InputAdornment position="end">h</InputAdornment>,
                            },
                            htmlInput: { min: 0, max: 24 },
                          }}
                          fullWidth
                        />
                        <TextField
                          label="Minutos"
                          type="number"
                          size="small"
                          disabled={disabled}
                          value={duracaoMinutos ?? 0}
                          onChange={(e) => aplicarDuracao(duracaoHoras || 0, Math.max(0, parseInt(e.target.value, 10) || 0))}
                          slotProps={{
                            input: {
                              endAdornment: <InputAdornment position="end">min</InputAdornment>,
                            },
                            htmlInput: { min: 0, max: 59, step: 5 },
                          }}
                          fullWidth
                        />
                      </Stack>
                    </Grid>
                  </Grid>
                </Box>
              </Grid>
            </>
          )}

          {!isEditing && (
            <>
              <Grid size={{ xs: 12 }}>
                <Controller
                  name="isRecurring"
                  control={control}
                  render={({ field }) => (
                    <>
                      <FormControlLabel
                        control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} disabled={disabled} />}
                        label="Evento Recorrente"
                        sx={{ mb: 0.5 }}
                      />
                      <Collapse in={field.value}>
                        <Box sx={{ mb: 1.5, mt: 1.5 }}>
                          <Controller
                            name="recurrenceRule"
                            control={control}
                            render={({ field: ruleField }) => (
                              <FormularioRecorrencia rule={ruleField.value} onChange={ruleField.onChange} disabled={disabled} />
                            )}
                          />
                        </Box>
                      </Collapse>
                    </>
                  )}
                />
              </Grid>

              {isParoco && (
                <Grid size={{ xs: 12 }}>
                  <Box sx={{ pt: 2, pb: 1, borderTop: '1px dashed', borderColor: 'divider' }}>
                    <Controller
                      name="liberarConflitoAgenda"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} color="warning" disabled={disabled} />}
                          label="Agendar Imediatamente por Decisão Pastoral"
                        />
                      )}
                    />
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', ml: 1, mt: 0.5 }}>
                      Aplica a prerrogativa do Pároco para agendamento direto. Se desmarcado, qualquer sobreposição identificada seguirá o fluxo de aprovação do Conselho Paroquial.
                    </Typography>
                  </Box>
                </Grid>
              )}
            </>
          )}

          <Grid size={{ xs: 12 }}>
            <TextField
              label={
                isSecretario
                  ? 'Justificativa da Solicitação (Obrigatória para Secretário)'
                  : isClosedYear
                  ? 'Justificativa (Obrigatória para Ano Fechado)'
                  : 'Justificativa Extra (Opcional)'
              }
              placeholder={
                isSecretario
                  ? 'Descreva a motivação detalhada para a criação/edição deste evento'
                  : 'Preencha caso seja um agendamento extraordinário'
              }
              required={isSecretario || isClosedYear}
              fullWidth
              disabled={disabled}
              size="small"
              slotProps={{ htmlInput: { maxLength: 4000 } }}
              {...register('justification')}
            />
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default EventoDataRecorrenciaCard;
