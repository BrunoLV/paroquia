import React from 'react';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BlockIcon from '@mui/icons-material/Block';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import type { ChipProps, SvgIconProps } from '@mui/material';

interface SituacaoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

/** Ativo/Inativo — usado em Locais, Pastorais/Movimentos e vínculos de membro. */
export const getSituacaoAtivoMeta = (ativo: boolean): SituacaoMeta =>
  ativo
    ? { label: 'Ativo', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Ativo' }
    : { label: 'Inativo', color: 'error', icon: React.createElement(BlockIcon), tooltip: 'Inativo' };

/** Ativo/Pendente — usado para usuários aguardando aprovação de cadastro. */
export const getUsuarioSituacaoMeta = (enabled: boolean): SituacaoMeta =>
  enabled
    ? { label: 'Ativo', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Cadastro ativo' }
    : { label: 'Pendente', color: 'warning', icon: React.createElement(PendingActionsIcon), tooltip: 'Aguardando aprovação de cadastro' };
