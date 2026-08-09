import React from 'react';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import InventoryIcon from '@mui/icons-material/Inventory';
import BuildIcon from '@mui/icons-material/Build';
import BlockIcon from '@mui/icons-material/Block';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { AtivoStatus } from '../../api/types';

export type { AtivoStatus };

interface AtivoStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const ATIVO_STATUS_META: Record<AtivoStatus, AtivoStatusMeta> = {
  DISPONIVEL: { label: 'Disponível', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Ativo disponível para alocação' },
  ALOCADO: { label: 'Alocado', color: 'info', icon: React.createElement(InventoryIcon), tooltip: 'Ativo atualmente alocado' },
  EM_MANUTENCAO: { label: 'Em Manutenção', color: 'warning', icon: React.createElement(BuildIcon), tooltip: 'Ativo em manutenção' },
  BAIXADO: { label: 'Baixado', color: 'default', icon: React.createElement(BlockIcon), tooltip: 'Ativo baixado, fora de uso' },
};

export const getAtivoStatusMeta = (status: AtivoStatus): AtivoStatusMeta => ATIVO_STATUS_META[status];
