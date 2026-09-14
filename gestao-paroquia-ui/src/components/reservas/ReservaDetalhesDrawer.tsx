import React from 'react';
import {
  Box,
  Divider,
  Drawer,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ActionButton from '@/components/ActionButton';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import BuildIcon from '@mui/icons-material/Build';
import EventIcon from '@mui/icons-material/Event';
import PersonIcon from '@mui/icons-material/Person';
import GroupsIcon from '@mui/icons-material/Groups';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import DescriptionIcon from '@mui/icons-material/Description';
import ScheduleIcon from '@mui/icons-material/Schedule';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { ReservaDetalhe } from '@/api/types';
import StatusIcon from '@/components/StatusIcon';
import { InfoField } from '@/components/fieis/fielConstants';
import { getReservaStatusMeta } from '@/utils/statusMeta/reservaStatus';

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
        paper: { sx: { width: { xs: '100%', sm: 480 }, p: 3 } },
      }}
    >
      <Stack spacing={2.5}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Detalhes da Reserva
          </Typography>
          <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
        </Stack>

        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
          {reserva.tipoItem === 'LOCAL' ? (
            <MeetingRoomIcon sx={{ fontSize: 32, color: 'primary.main' }} />
          ) : (
            <BuildIcon sx={{ fontSize: 32, color: 'secondary.main' }} />
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

        <Box>
          <StatusIcon {...statusMeta} size="medium" />
        </Box>

        <Divider />

        <Grid container spacing={2}>
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
                  p: 1.5,
                  borderRadius: 1,
                  bgcolor: 'action.hover',
                  borderLeft: '4px solid',
                  borderLeftColor: 'primary.main',
                }}
              >
                <Typography variant="caption" sx={{ fontWeight: 600, color: 'primary.main' }}>
                  Margem de Carência / Buffer Logístico: {reserva.bufferMinutos} minutos
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.8rem' }}>
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
