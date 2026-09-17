import React from 'react';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import BuildIcon from '@mui/icons-material/Build';
import HelpOutlineIcon from '@mui/icons-material/HelpOutlineOutlined';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { AtivoStatus } from '@/shared/domain/types/types';

export type { AtivoStatus };

interface AtivoStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const ATIVO_STATUS_META: Record<AtivoStatus, AtivoStatusMeta> = {
  DISPONIVEL: {
    label: 'Disponível',
    color: 'success',
    icon: React.createElement(CheckCircleIcon),
    tooltip: 'Bem livre para uso imediato ou agendamento',
  },
  RESERVADO: {
    label: 'Reservado',
    color: 'info',
    icon: React.createElement(EventAvailableIcon),
    tooltip: 'Reserva em vigor cobrindo a data corrente',
  },
  EM_MANUTENCAO: {
    label: 'Em Manutenção',
    color: 'warning',
    icon: React.createElement(BuildIcon),
    tooltip: 'Bem fisicamente indisponível por conserto/revisão',
  },
};

const DEFAULT_META: AtivoStatusMeta = {
  label: 'Desconhecido',
  color: 'default',
  icon: React.createElement(HelpOutlineIcon),
  tooltip: 'Status não classificado',
};

export const getAtivoStatusMeta = (status: string): AtivoStatusMeta =>
  ATIVO_STATUS_META[status as AtivoStatus] ?? DEFAULT_META;
