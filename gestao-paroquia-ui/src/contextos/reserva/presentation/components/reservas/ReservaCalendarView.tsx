import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Stack,
  Tooltip,
  Typography,
  Dialog,
} from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import BuildIcon from '@mui/icons-material/Build';
import IconActionButton from '@/shared/presentation/components/IconActionButton';
import { ReservaDetalhe } from '@/shared/domain/types/types';
import { getReservaStatusMeta } from '@/shared/utils/statusMeta/reservaStatus';

interface ReservaCalendarViewProps {
  reservas: ReservaDetalhe[];
  onSelectReserva: (reserva: ReservaDetalhe) => void;
}

export const ReservaCalendarView: React.FC<ReservaCalendarViewProps> = ({
  reservas,
  onSelectReserva,
}) => {
  const [dataBase, setDataBase] = useState(() => new Date());

  const ano = dataBase.getFullYear();
  const mes = dataBase.getMonth();

  const primeiroDiaSemana = new Date(ano, mes, 1).getDay();
  const totalDiasMes = new Date(ano, mes + 1, 0).getDate();

  const dias: (number | null)[] = [];
  for (let i = 0; i < primeiroDiaSemana; i++) {
    dias.push(null);
  }
  for (let d = 1; d <= totalDiasMes; d++) {
    dias.push(d);
  }

  const mesNome = dataBase.toLocaleString('pt-BR', { month: 'long', year: 'numeric' });

  const getReservasDoDia = (dia: number) => {
    return reservas.filter((r) => {
      const d = new Date(r.dataHoraInicio);
      return d.getFullYear() === ano && d.getMonth() === mes && d.getDate() === dia;
    });
  };

  const handleMesAnterior = () => {
    setDataBase(new Date(ano, mes - 1, 1));
  };

  const handleMesProximo = () => {
    setDataBase(new Date(ano, mes + 1, 1));
  };

  return (
    <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
      <CardContent sx={{ p: 2 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 600, textTransform: 'capitalize' }}>
            {mesNome}
          </Typography>
          <Stack direction="row" spacing={1}>
            <IconActionButton title="Mês anterior" icon={<ChevronLeftIcon />} size="small" onClick={handleMesAnterior} />
            <IconActionButton title="Próximo mês" icon={<ChevronRightIcon />} size="small" onClick={handleMesProximo} />
          </Stack>
        </Stack>

        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
            gap: 1,
            textAlign: 'center',
          }}
        >
          {['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'].map((diaSemana) => (
            <Typography
              key={diaSemana}
              variant="caption"
              color="text.secondary"
              sx={{ fontWeight: 700, py: 0.5 }}
            >
              {diaSemana}
            </Typography>
          ))}

          {dias.map((dia, idx) => {
            if (dia === null) {
              return <Box key={`empty-${idx}`} sx={{ minHeight: 90, bgcolor: 'action.hover', opacity: 0.2, borderRadius: 1 }} />;
            }

            const reservasDia = getReservasDoDia(dia);
            const isHoje =
              new Date().getFullYear() === ano &&
              new Date().getMonth() === mes &&
              new Date().getDate() === dia;

            return (
              <Box
                key={`day-${dia}`}
                sx={{
                  minHeight: 100,
                  p: 0.5,
                  border: '1px solid',
                  borderColor: isHoje ? 'primary.main' : 'divider',
                  borderRadius: 1,
                  bgcolor: isHoje ? 'primary.50' : 'background.paper',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 0.5,
                  textAlign: 'left',
                  overflow: 'hidden',
                }}
              >
                <Typography
                  variant="caption"
                  color={isHoje ? 'primary.main' : 'text.primary'}
                  sx={{ fontWeight: isHoje ? 700 : 500, px: 0.5 }}
                >
                  {dia}
                </Typography>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, overflowY: 'auto' }}>
                  {reservasDia.map((r) => {
                    const meta = getReservaStatusMeta(r.status);
                    const horaInicio = new Date(r.dataHoraInicio).toLocaleTimeString('pt-BR', {
                      hour: '2-digit',
                      minute: '2-digit',
                    });

                    return (
                      <Tooltip
                        key={r.id}
                        title={`${horaInicio} - ${r.itemNome} (${r.descricao}) [Buffer: ${r.bufferMinutos}m]`}
                      >
                        <Box
                          onClick={() => onSelectReserva(r)}
                          sx={{
                            p: 0.4,
                            borderRadius: 0.5,
                            bgcolor: r.tipoItem === 'LOCAL' ? 'primary.lighter' : 'secondary.lighter',
                            borderLeft: '3px solid',
                            borderLeftColor: r.tipoItem === 'LOCAL' ? 'primary.main' : 'secondary.main',
                            cursor: 'pointer',
                            '&:hover': { filter: 'brightness(0.95)' },
                          }}
                        >
                          <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center' }}>
                            {r.tipoItem === 'LOCAL' ? (
                              <MeetingRoomIcon sx={{ fontSize: 12, color: 'primary.main' }} />
                            ) : (
                              <BuildIcon sx={{ fontSize: 12, color: 'secondary.main' }} />
                            )}
                            <Typography variant="caption" noWrap sx={{ fontSize: '0.7rem', fontWeight: 600 }}>
                              {horaInicio} {r.itemNome}
                            </Typography>
                          </Stack>
                        </Box>
                      </Tooltip>
                    );
                  })}
                </Box>
              </Box>
            );
          })}
        </Box>
      </CardContent>
    </Card>
  );
};
