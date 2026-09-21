import React from 'react';
import type { SvgIconProps } from '@mui/material';
import VideocamIcon from '@mui/icons-material/Videocam';
import DevicesIcon from '@mui/icons-material/Devices';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { FormatoEvento, FORMATO_EVENTO_LABELS } from '@/shared/domain/types/types';

/**
 * Retorna o ícone representativo para o formato de realização do evento.
 */
export const getFormatoEventoIcon = (
  formato?: FormatoEvento | string | null,
  fontSize: 'inherit' | 'small' | 'medium' | 'large' = 'small'
): React.ReactElement<SvgIconProps> => {
  switch (formato) {
    case 'ONLINE':
      return <VideocamIcon fontSize={fontSize} titleAccess="Online / Remoto" />;
    case 'HIBRIDO':
      return <DevicesIcon fontSize={fontSize} titleAccess="Híbrido (Presencial + Online)" />;
    case 'PRESENCIAL':
    default:
      return <LocationOnIcon fontSize={fontSize} titleAccess="Presencial" />;
  }
};

/**
 * Retorna label formatada em português para o formato do evento.
 */
export const getFormatoEventoLabel = (formato?: FormatoEvento | string | null): string => {
  if (formato === 'ONLINE') return FORMATO_EVENTO_LABELS.ONLINE;
  if (formato === 'HIBRIDO') return FORMATO_EVENTO_LABELS.HIBRIDO;
  return FORMATO_EVENTO_LABELS.PRESENCIAL;
};
