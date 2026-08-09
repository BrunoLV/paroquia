import React from 'react';
import { Button, IconButton, Tooltip, type ButtonProps, type SxProps, type Theme } from '@mui/material';

interface ActionButtonProps {
  title: string;
  icon: React.ReactElement;
  label?: string;
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void;
  type?: 'button' | 'submit';
  color?: ButtonProps['color'];
  variant?: ButtonProps['variant'];
  size?: 'small' | 'medium';
  disabled?: boolean;
  fullWidth?: boolean;
  component?: React.ElementType;
  sx?: SxProps<Theme>;
  children?: React.ReactNode;
  /** Sobrescreve o aria-label (default: `title`) — usar só quando o código já expõe um aria-label distinto do texto do tooltip. */
  ariaLabel?: string;
}

/** Botão com ícone + tooltip obrigatórios — padrão único de ação no app. */
const ActionButton: React.FC<ActionButtonProps> = ({
  title, icon, label, onClick, type = 'button', color = 'primary', variant = 'outlined',
  size = 'medium', disabled, fullWidth, component, sx, children, ariaLabel,
}) => {
  const buttonEl = label ? (
    <Button
      type={type} color={color} variant={variant} size={size} onClick={onClick}
      startIcon={icon} disabled={disabled} fullWidth={fullWidth}
      sx={sx} aria-label={ariaLabel ?? title} {...(component ? { component } : {})}
    >
      {label}
    </Button>
  ) : (
    <IconButton
      type={type} color={color} size={size} onClick={onClick} disabled={disabled}
      sx={sx} aria-label={ariaLabel ?? title} {...(component ? { component } : {})}
    >
      {icon}
      {children}
    </IconButton>
  );

  return (
    <Tooltip title={title}>
      {disabled ? <span>{buttonEl}</span> : buttonEl}
    </Tooltip>
  );
};

export default ActionButton;
