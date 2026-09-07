import React from 'react';
import { Box, Typography, type ChipProps, type SvgIconProps, type SxProps, type Theme } from '@mui/material';

interface StatusIconProps {
  label: string;
  color: ChipProps['color'];
  icon: React.ReactElement<SvgIconProps>;
  tooltip: string;
  size?: 'small' | 'medium';
  variant?: ChipProps['variant'];
  sx?: SxProps<Theme>;
}

/** Indicador textual sem aparência de botão; tooltip apenas complementa a informação. */
const StatusIcon: React.FC<StatusIconProps> = ({ color, icon, label, size = 'small', sx }) => (
  <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.5, color: color === 'default' ? 'text.secondary' : `${color}.main` }}>
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', verticalAlign: 'middle', lineHeight: 1, ...sx }}>
      {React.cloneElement(icon, { color: 'inherit', fontSize: size === 'small' ? 'small' : 'medium' })}
      <Typography component="span" variant="caption" sx={{ display: 'block', ml: 0.5, fontWeight: 600, lineHeight: 1 }}>
        {label}
      </Typography>
    </Box>
  </Box>
);

export default StatusIcon;
