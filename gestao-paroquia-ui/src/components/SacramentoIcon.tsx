import React from 'react';
import { Tooltip, type SvgIconProps } from '@mui/material';

interface SacramentoIconProps {
  icon: React.ReactElement<SvgIconProps>;
  color: SvgIconProps['color'];
  tooltip: string;
}

/** Ícone colorido com tooltip, sem texto — usado para indicadores de sacramento. */
const SacramentoIcon: React.FC<SacramentoIconProps> = ({ icon, color, tooltip }) => (
  <Tooltip title={tooltip}>
    {React.cloneElement(icon, { color, fontSize: 'small' })}
  </Tooltip>
);

export default SacramentoIcon;
