import React from 'react';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import OutboxIcon from '@mui/icons-material/Outbox';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import CancelIcon from '@mui/icons-material/Cancel';
import AccessTimeFilledIcon from '@mui/icons-material/AccessTimeFilled';
import type { ChipProps, SvgIconProps } from '@mui/material';
import { StatusReserva } from '@/shared/domain/types/types';

export interface ReservaStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

export const getReservaStatusMeta = (status: StatusReserva): ReservaStatusMeta => {
  switch (status) {
    case 'PENDENTE':
      return {
        label: 'Pendente',
        color: 'warning',
        icon: React.createElement(HourglassEmptyIcon),
        tooltip: 'Aguardando aprovação de coordenador ou administrador',
      };
    case 'CONFIRMADA':
      return {
        label: 'Confirmada',
        color: 'success',
        icon: React.createElement(CheckCircleIcon),
        tooltip: 'Reserva deferida e agenda garantida',
      };
    case 'RETIRADO':
      return {
        label: 'Em Custódia',
        color: 'info',
        icon: React.createElement(OutboxIcon),
        tooltip: 'Bem retirado fisicamente para uso',
      };
    case 'FINALIZADA':
      return {
        label: 'Finalizada',
        color: 'default',
        icon: React.createElement(TaskAltIcon),
        tooltip: 'Uso e devolução concluídos',
      };
    case 'CANCELADA':
      return {
        label: 'Cancelada',
        color: 'error',
        icon: React.createElement(CancelIcon),
        tooltip: 'Reserva desfeita ou rejeitada',
      };
    case 'EXPIRADA':
      return {
        label: 'Expirada',
        color: 'error',
        icon: React.createElement(AccessTimeFilledIcon),
        tooltip: 'Expirada por ausência de aprovação em tempo hábil',
      };
    default:
      return {
        label: status,
        color: 'default',
        icon: React.createElement(HourglassEmptyIcon),
        tooltip: status,
      };
  }
};
