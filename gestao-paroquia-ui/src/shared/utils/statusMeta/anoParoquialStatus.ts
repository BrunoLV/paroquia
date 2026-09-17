import React from 'react';
import LockOpenIcon from '@mui/icons-material/LockOpen';
import LockIcon from '@mui/icons-material/Lock';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { AnoParoquialStatus } from '@/shared/domain/types/types';

export type { AnoParoquialStatus };

interface AnoParoquialStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const ANO_PAROQUIAL_STATUS_META: Record<AnoParoquialStatus, AnoParoquialStatusMeta> = {
  EM_PLANEJAMENTO: { label: 'Planejamento Aberto', color: 'info', icon: React.createElement(LockOpenIcon), tooltip: 'Ano paroquial em planejamento' },
  FECHADO: { label: 'Planejamento Fechado (Trava)', color: 'warning', icon: React.createElement(LockIcon), tooltip: 'Ano paroquial fechado' },
  CONCLUIDO: { label: 'Concluído', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Ano paroquial concluído' },
};

export const getAnoParoquialStatusMeta = (status: AnoParoquialStatus): AnoParoquialStatusMeta => ANO_PAROQUIAL_STATUS_META[status];
