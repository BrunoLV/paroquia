import React from 'react';
import { Tooltip, type ChipProps, type SvgIconProps, type SxProps, type Theme } from '@mui/material';

interface StatusIconProps {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
  size?: 'small' | 'medium';
  variant?: ChipProps['variant'];
  sx?: SxProps<Theme>;
}

const toSvgColor = (color: ChipProps['color']): SvgIconProps['color'] => (color === 'default' ? undefined : color);

/** Ícone colorido com tooltip, sem texto — padrão visual único p/ status/sacramentos/tags. */
const StatusIcon: React.FC<StatusIconProps> = ({ color, icon, tooltip, sx }) => (
  <Tooltip title={tooltip}>
    {React.cloneElement(icon, { color: toSvgColor(color), fontSize: 'small', sx })}
  </Tooltip>
);

export default StatusIcon;
