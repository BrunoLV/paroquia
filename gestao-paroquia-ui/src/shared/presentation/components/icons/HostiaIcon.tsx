import React from 'react';
import { SvgIcon, type SvgIconProps } from '@mui/material';

/** Hóstia consagrada — disco redondo com cruz inscrita, símbolo da Primeira Comunhão. */
const HostiaIcon: React.FC<SvgIconProps> = (props) => (
  <SvgIcon {...props} viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="1.5" />
    <path d="M12 7.5v9M8 12h8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
  </SvgIcon>
);

export default HostiaIcon;
