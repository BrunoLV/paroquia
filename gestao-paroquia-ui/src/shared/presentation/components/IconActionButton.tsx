import React from 'react';
import { IconButton, Tooltip, type IconButtonProps, type SxProps, type Theme } from '@mui/material';

interface IconActionButtonProps extends Pick<IconButtonProps, 'id' | 'aria-controls' | 'aria-expanded' | 'aria-haspopup' | 'onKeyDown'> {
  title: string;
  ariaLabel?: string;
  icon: React.ReactElement;
  onClick: IconButtonProps['onClick'];
  color?: IconButtonProps['color'];
  disabled?: boolean;
  size?: IconButtonProps['size'];
  sx?: SxProps<Theme>;
}

/** Controle compacto para navegação, alternância ou abertura de menus, sempre com nome acessível. */
const IconActionButton: React.FC<IconActionButtonProps> = ({
  title, ariaLabel, icon, onClick, color = 'primary', disabled, size = 'medium', sx, ...accessibilityProps
}) => (
  <Tooltip title={title} describeChild>
    <span>
      <IconButton {...accessibilityProps} aria-label={ariaLabel ?? title} color={color} disabled={disabled} size={size} sx={sx} onClick={onClick}>
        {icon}
      </IconButton>
    </span>
  </Tooltip>
);

export default IconActionButton;
