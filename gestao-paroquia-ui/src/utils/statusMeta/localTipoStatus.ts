import React from 'react';
import ChurchIcon from '@mui/icons-material/Church';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import DoorFrontIcon from '@mui/icons-material/DoorFront';
import HelpOutlineIcon from '@mui/icons-material/HelpOutlineOutlined';
import type { ChipProps, SvgIconProps } from '@mui/material';

interface LocalTipoMeta {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
}

const LOCAL_TIPO_META: Record<string, LocalTipoMeta> = {
  IGREJA: { label: 'Igreja', color: 'primary', icon: React.createElement(ChurchIcon), tooltip: 'Igreja' },
  SALAO: { label: 'Salão', color: 'secondary', icon: React.createElement(MeetingRoomIcon), tooltip: 'Salão' },
  SALA: { label: 'Sala', color: 'info', icon: React.createElement(DoorFrontIcon), tooltip: 'Sala' },
  OUTRO: { label: 'Outro', color: 'default', icon: React.createElement(HelpOutlineIcon), tooltip: 'Outro tipo de local' },
};

const DEFAULT_META: LocalTipoMeta = LOCAL_TIPO_META.OUTRO;

export const getLocalTipoMeta = (tipo: string): LocalTipoMeta => LOCAL_TIPO_META[tipo] ?? DEFAULT_META;
