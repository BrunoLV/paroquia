import React from 'react';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import CancelIcon from '@mui/icons-material/Cancel';
import EventIcon from '@mui/icons-material/Event';
import BuildIcon from '@mui/icons-material/Build';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { AtivoAlocacaoStatus, AtivoAlocacaoTipo } from '../../api/types';

export type { AtivoAlocacaoStatus, AtivoAlocacaoTipo };

interface AtivoAlocacaoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const ATIVO_ALOCACAO_STATUS_META: Record<AtivoAlocacaoStatus, AtivoAlocacaoMeta> = {
  PENDENTE_APROVACAO: { label: 'Pendente de Aprovação', color: 'warning', icon: React.createElement(HourglassEmptyIcon), tooltip: 'Alocação aguardando aprovação' },
  CONFIRMADA: { label: 'Confirmada', color: 'info', icon: React.createElement(CheckCircleIcon), tooltip: 'Alocação confirmada' },
  ATRASADA: { label: 'Atrasada', color: 'error', icon: React.createElement(WarningIcon), tooltip: 'Devolução atrasada' },
  DEVOLVIDA: { label: 'Devolvida', color: 'default', icon: React.createElement(AssignmentReturnIcon), tooltip: 'Ativo devolvido' },
  CANCELADA: { label: 'Cancelada', color: 'default', icon: React.createElement(CancelIcon), tooltip: 'Alocação cancelada' },
};

const ATIVO_ALOCACAO_TIPO_META: Record<AtivoAlocacaoTipo, AtivoAlocacaoMeta> = {
  ALOCACAO_EVENTO: { label: 'Alocação para Evento', color: 'info', icon: React.createElement(EventIcon), tooltip: 'Reserva vinculada a um evento' },
  MANUTENCAO: { label: 'Manutenção', color: 'warning', icon: React.createElement(BuildIcon), tooltip: 'Período de manutenção' },
};

export const getAtivoAlocacaoStatusMeta = (status: AtivoAlocacaoStatus): AtivoAlocacaoMeta => ATIVO_ALOCACAO_STATUS_META[status];

export const getAtivoAlocacaoTipoMeta = (tipo: AtivoAlocacaoTipo): AtivoAlocacaoMeta => ATIVO_ALOCACAO_TIPO_META[tipo];
