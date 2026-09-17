import React from 'react';
import PlayCircleOutlineIcon from '@mui/icons-material/PlayCircleOutlineOutlined';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import CancelIcon from '@mui/icons-material/Cancel';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import type { ChipProps, SvgIconProps } from '@mui/material';
import type { Projeto } from '@/shared/domain/types/types';

export type ProjetoStatus = Projeto['status'];

interface ProjetoStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const PROJETO_STATUS_META: Record<ProjetoStatus, ProjetoStatusMeta> = {
  ABERTO: { label: 'Aberto', color: 'info', icon: React.createElement(PlayCircleOutlineIcon), tooltip: 'Projeto aberto, ainda não iniciado' },
  EM_ANDAMENTO: { label: 'Em Andamento', color: 'success', icon: React.createElement(AutorenewIcon), tooltip: 'Projeto em execução' },
  CANCELADO: { label: 'Cancelado', color: 'error', icon: React.createElement(CancelIcon), tooltip: 'Projeto cancelado' },
  CONCLUIDO: { label: 'Concluído', color: 'default', icon: React.createElement(CheckCircleIcon), tooltip: 'Projeto concluído' },
};

export const getProjetoStatusMeta = (status: ProjetoStatus): ProjetoStatusMeta => PROJETO_STATUS_META[status];
