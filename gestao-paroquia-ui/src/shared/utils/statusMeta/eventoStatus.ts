import React from 'react';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlineOutlined';
import EditNoteIcon from '@mui/icons-material/EditNote';
import CancelIcon from '@mui/icons-material/Cancel';
import HelpOutlineIcon from '@mui/icons-material/HelpOutlineOutlined';
import type { ChipProps, SvgIconProps } from '@mui/material';

interface EventoStatusMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const DEFAULT_META: EventoStatusMeta = { label: 'Desconhecido', color: 'default', icon: React.createElement(HelpOutlineIcon), tooltip: 'Status não reconhecido' };

/** `status` do evento é string solta no contrato (types.ts) — sem enum enumerado, fallback neutro para valores desconhecidos. */
export const getEventoStatusMeta = (status: string): EventoStatusMeta => {
  switch (status.toUpperCase()) {
    case 'CONFIRMADO':
      return { label: 'Confirmado', color: 'success', icon: React.createElement(CheckCircleIcon), tooltip: 'Evento confirmado' };
    case 'ADICIONADO_EXTRA':
      return { label: 'Adicionado Extra', color: 'secondary', icon: React.createElement(AddCircleOutlineIcon), tooltip: 'Evento extra adicionado à agenda' };
    case 'RASCUNHO':
      return { label: 'Rascunho', color: 'warning', icon: React.createElement(EditNoteIcon), tooltip: 'Evento em rascunho' };
    case 'CANCELADO':
      return { label: 'Cancelado', color: 'error', icon: React.createElement(CancelIcon), tooltip: 'Evento cancelado' };
    default:
      return DEFAULT_META;
  }
};
