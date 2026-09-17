import React from 'react';
import { Box, Typography, type ChipProps, type SvgIconProps } from '@mui/material';

interface SacramentoIconProps {
  icon: React.ReactElement<SvgIconProps>;
  color: ChipProps['color'];
  tooltip: string;
}

/** Indicador de sacramento com ícone e rótulo visível, sem aparência de botão. */
const SacramentoIcon: React.FC<SacramentoIconProps> = ({ icon, color, tooltip }) => (
  <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', verticalAlign: 'middle', color: color === 'default' ? 'text.secondary' : `${color}.main` }}>
    {React.cloneElement(icon, { color: 'inherit', fontSize: 'small' })}
    <Typography component="span" variant="caption" sx={{ display: 'block', ml: 0.5, fontWeight: 600, lineHeight: 1 }}>
      {tooltip}
    </Typography>
  </Box>
);

export default SacramentoIcon;
