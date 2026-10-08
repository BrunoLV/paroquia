import React from 'react';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BlockIcon from '@mui/icons-material/Block';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import EventBusyIcon from '@mui/icons-material/EventBusy';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { StatusMandato } from '@/shared/domain/types/types';

interface MandatoStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const MANDATO_STATUS_META: Record<StatusMandato, MandatoStatusMeta> = {
  PROPOSTO: { label: 'Aguardando homologação', color: 'warning', icon: React.createElement(HourglassEmptyIcon), tooltip: 'Proposto; aguarda a confirmação do Pároco' },
  HOMOLOGADO: { label: 'Homologado', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Confirmado pelo Pároco' },
  VETADO: { label: 'Vetado', color: 'error', icon: React.createElement(BlockIcon), tooltip: 'Vetado pelo Pároco' },
  ENCERRADO: { label: 'Encerrado', color: 'default', icon: React.createElement(EventBusyIcon), tooltip: 'Mandato encerrado' },
};

/** Situação canônica de um mandato de coordenação. */
export const getMandatoStatusMeta = (status: StatusMandato): MandatoStatusMeta => MANDATO_STATUS_META[status];
