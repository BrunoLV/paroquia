import React from 'react';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BlockIcon from '@mui/icons-material/Block';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { StatusHomologacao } from '@/shared/domain/types/types';

export type { StatusHomologacao };

interface HomologacaoStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const HOMOLOGACAO_STATUS_META: Record<StatusHomologacao, HomologacaoStatusMeta> = {
  PENDENTE_HOMOLOGACAO: { label: 'Pendente de Homologação', color: 'warning', icon: React.createElement(HourglassEmptyIcon), tooltip: 'Aguardando homologação' },
  HOMOLOGADO: { label: 'Homologado', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Decisão homologada' },
  VETADO: { label: 'Vetado', color: 'error', icon: React.createElement(BlockIcon), tooltip: 'Decisão vetada' },
};

export const getHomologacaoStatusMeta = (status: StatusHomologacao): HomologacaoStatusMeta => HOMOLOGACAO_STATUS_META[status];
