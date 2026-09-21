import React from 'react';
import {
  Alert,
  Box,
  Chip,
  Divider,
  Drawer,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ActionButton from '@/shared/presentation/components/ActionButton';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import BuildIcon from '@mui/icons-material/Build';
import EventIcon from '@mui/icons-material/Event';
import PersonIcon from '@mui/icons-material/Person';
import GroupsIcon from '@mui/icons-material/Groups';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import DescriptionIcon from '@mui/icons-material/Description';
import ScheduleIcon from '@mui/icons-material/Schedule';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import RepeatIcon from '@mui/icons-material/Repeat';
import { ReservaDetalhe } from '@/shared/domain/types/types';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { InfoField } from '@/contextos/fiel/presentation/components/fieis/fielConstants';
import { getReservaStatusMeta } from '@/shared/utils/statusMeta/reservaStatus';

interface ReservaDetalhesDrawerProps {
  open: boolean;
  onClose: () => void;
  reserva: ReservaDetalhe | null;
}

export const ReservaDetalhesDrawer: React.FC<ReservaDetalhesDrawerProps> = ({
  open,
  onClose,
  reserva,
}) => {
  if (!reserva) return null;

  const statusMeta = getReservaStatusMeta(reserva.status);

  const identificarTurno = (inicioIso?: string, fimIso?: string, descricao?: string): string | null => {
    if (descricao) {
      if (descricao.includes('(Manhã)')) return 'Manhã';
      if (descricao.includes('(Tarde)')) return 'Tarde';
      if (descricao.includes('(Noite)')) return 'Noite';
      if (descricao.includes('(Madrugada)')) return 'Madrugada';
      if (descricao.includes('(Dia Inteiro)')) return 'Dia Inteiro';
    }
    if (!inicioIso || !fimIso) return null;
    try {
      const dInicio = new Date(inicioIso);
      const dFim = new Date(fimIso);
      const horaInicio = dInicio.getHours();
      const horaFim = dFim.getHours();
      const minFim = dFim.getMinutes();

      if (horaInicio === 6 && (horaFim === 11 || (horaFim === 12 && minFim === 0))) return 'Manhã';
      if (horaInicio === 12 && (horaFim === 17 || (horaFim === 18 && minFim === 0))) return 'Tarde';
      if (horaInicio === 18 && (horaFim === 23 || (horaFim === 0 && minFim === 0))) return 'Noite';
      if (horaInicio === 0 && (horaFim === 5 || (horaFim === 6 && minFim === 0))) return 'Madrugada';
      if (horaInicio === 6 && horaFim === 23 && minFim >= 59) return 'Dia Inteiro';
      return null;
    } catch {
      return null;
    }
  };

  const turno = identificarTurno(reserva.dataHoraInicio, reserva.dataHoraFim, reserva.descricao);

  const formatarData = (iso?: string | null) => {
    if (!iso) return '-';
    try {
      return new Date(iso).toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return iso;
    }
  };

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      slotProps={{
        paper: { sx: { width: { xs: '100%', sm: 560, md: 640 }, p: { xs: 2.5, sm: 3.5 } } },
      }}
    >
      <Stack spacing={3}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Detalhes da Reserva
          </Typography>
          <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
        </Stack>

        {reserva.grupoRecorrenciaId && (
          <Alert severity="info" icon={<RepeatIcon />} sx={{ borderRadius: 2 }}>
            Esta reserva faz parte de uma série programada recorrente.
          </Alert>
        )}

        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          {reserva.tipoItem === 'LOCAL' ? (
            <MeetingRoomIcon sx={{ fontSize: 36, color: 'primary.main' }} />
          ) : (
            <BuildIcon sx={{ fontSize: 36, color: 'secondary.main' }} />
          )}
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              {reserva.itemNome}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {reserva.tipoItem === 'LOCAL' ? 'Espaço Físico Paroquial' : 'Ativo Patrimonial Móvel'}
            </Typography>
          </Box>
        </Stack>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
          <StatusIcon {...statusMeta} size="medium" />
          {turno && (
            <Chip
              label={`Turno: ${turno}`}
              color="primary"
              variant="outlined"
              size="small"
              sx={{ fontWeight: 600 }}
            />
          )}
        </Box>

        <Divider />

        <Grid container spacing={2.5}>
          <Grid size={{ xs: 12 }}>
            <InfoField label="Finalidade / Descrição" icon={<DescriptionIcon fontSize="inherit" />} value={reserva.descricao} />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <InfoField label="Início do Uso" icon={<ScheduleIcon fontSize="inherit" />} value={formatarData(reserva.dataHoraInicio)} />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <InfoField label="Término do Uso" icon={<ScheduleIcon fontSize="inherit" />} value={formatarData(reserva.dataHoraFim)} />
          </Grid>

          {reserva.bufferMinutos > 0 && (
            <Grid size={{ xs: 12 }}>
              <Box
                sx={{
                  p: 2,
                  borderRadius: 2,
                  bgcolor: 'action.hover',
                  borderLeft: '4px solid',
                  borderLeftColor: 'primary.main',
                }}
              >
                <Typography variant="caption" sx={{ fontWeight: 600, color: 'primary.main' }}>
                  Margem de Carência / Buffer Logístico: {reserva.bufferMinutos} minutos
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.85rem', mt: 0.5 }}>
                  Recurso bloqueado até {formatarData(reserva.fimComBuffer)} para higienização e organização.
                </Typography>
              </Box>
            </Grid>
          )}

          <Grid size={{ xs: 12 }}>
            <Divider />
          </Grid>

          <Grid size={{ xs: 6 }}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <PersonIcon fontSize="small" color="action" />
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Solicitante
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 500 }}>
                  {reserva.solicitanteNome}
                </Typography>
              </Box>
            </Stack>
          </Grid>

          <Grid size={{ xs: 6 }}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <GroupsIcon fontSize="small" color="action" />
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Pastoral / Movimento
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 500 }}>
                  {reserva.pastoralMovimentoNome}
                </Typography>
              </Box>
            </Stack>
          </Grid>

          {reserva.eventoTitulo && (
            <Grid size={{ xs: 12 }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                <EventIcon fontSize="small" color="primary" />
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Vinculado ao Evento
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600, color: 'primary.main' }}>
                    {reserva.eventoTitulo}
                  </Typography>
                </Box>
              </Stack>
            </Grid>
          )}

          {reserva.aprovadorNome && (
            <Grid size={{ xs: 12 }}>
              <InfoField
                label="Aprovado por"
                icon={<CheckCircleIcon fontSize="inherit" />}
                value={`${reserva.aprovadorNome} em ${formatarData(reserva.aprovadoEm)}`}
              />
            </Grid>
          )}

          {reserva.motivoCancelamento && (
            <Grid size={{ xs: 12 }}>
              <Box
                sx={{
                  p: 1.5,
                  borderRadius: 1,
                  bgcolor: 'error.50',
                  border: '1px solid',
                  borderColor: 'error.main',
                }}
              >
                <Typography variant="caption" sx={{ fontWeight: 700, color: 'error.main' }}>
                  Cancelamento / Rejeição
                </Typography>
                <Typography variant="body2" color="text.primary">
                  {reserva.motivoCancelamento}
                </Typography>
                {reserva.canceladoPorNome && (
                  <Typography variant="caption" color="text.secondary">
                    Registrado por {reserva.canceladoPorNome}
                  </Typography>
                )}
              </Box>
            </Grid>
          )}

          {reserva.checkAvarias && (
            <Grid size={{ xs: 12 }}>
              <Box
                sx={{
                  p: 1.5,
                  borderRadius: 1,
                  bgcolor: 'info.50',
                  border: '1px solid',
                  borderColor: 'info.main',
                }}
              >
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                  <ReportProblemIcon fontSize="small" color="info" />
                  <Typography variant="caption" sx={{ fontWeight: 700, color: 'info.main' }}>
                    Relatório de Avarias / Devolução Física
                  </Typography>
                </Stack>
                <Typography variant="body2" color="text.primary" sx={{ mt: 0.5 }}>
                  {reserva.checkAvarias}
                </Typography>
              </Box>
            </Grid>
          )}
        </Grid>
      </Stack>
    </Drawer>
  );
};
