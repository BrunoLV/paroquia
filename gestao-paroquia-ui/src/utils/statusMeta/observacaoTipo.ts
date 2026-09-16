import React from 'react';
import NotesIcon from '@mui/icons-material/Notes';
import DescriptionIcon from '@mui/icons-material/Description';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import BlockIcon from '@mui/icons-material/Block';
import ScheduleIcon from '@mui/icons-material/Schedule';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { TipoObservacao } from '@/api/types';

export type { TipoObservacao };

interface ObservacaoTipoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const OBSERVACAO_TIPO_META: Record<TipoObservacao, ObservacaoTipoMeta> = {
  NOTA: { label: 'Nota', color: 'default', icon: React.createElement(NotesIcon), tooltip: 'Nota geral sobre o evento' },
  JUSTIFICATIVA: { label: 'Justificativa', color: 'info', icon: React.createElement(DescriptionIcon), tooltip: 'Justificativa de uma ação' },
  APROVACAO: { label: 'Aprovação', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Registro de aprovação' },
  REPROVACAO: { label: 'Reprovação', color: 'error', icon: React.createElement(CancelIcon), tooltip: 'Registro de reprovação' },
  CANCELAMENTO: { label: 'Cancelamento', color: 'error', icon: React.createElement(BlockIcon), tooltip: 'Registro de cancelamento' },
  AJUSTE_HORARIO: { label: 'Ajuste de Horário', color: 'warning', icon: React.createElement(ScheduleIcon), tooltip: 'Registro de ajuste de horário' },
};

export const getObservacaoTipoMeta = (tipo: TipoObservacao): ObservacaoTipoMeta => OBSERVACAO_TIPO_META[tipo];
