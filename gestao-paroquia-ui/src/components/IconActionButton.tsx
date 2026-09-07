import React from 'react';
import { IconButton, Tooltip, type IconButtonProps, type SxProps, type Theme } from '@mui/material';

interface IconActionButtonProps {
  title: string;
  ariaLabel?: string;
  icon: React.ReactElement;
  onClick: IconButtonProps['onClick'];
  color?: IconButtonProps['color'];
  disabled?: boolean;
  size?: IconButtonProps['size'];
  sx?: SxProps<Theme>;
}

/** Controle compacto para navegação ou alternância, nunca para uma ação de negócio. */
const IconActionButton: React.FC<IconActionButtonProps> = ({
  title, ariaLabel, icon, onClick, color = 'primary', disabled, size = 'medium', sx,
}) => (
  <Tooltip title={title}>
    <span>
      <IconButton aria-label={ariaLabel ?? title} color={color} disabled={disabled} size={size} sx={sx} onClick={onClick}>
        {icon}
      </IconButton>
    </span>
  </Tooltip>
);

export default IconActionButton;
