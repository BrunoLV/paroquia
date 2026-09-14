import React from 'react';
import { Button, type ButtonProps, type SxProps, type Theme } from '@mui/material';

interface ActionButtonProps {
  title: string;
  icon: React.ReactElement;
  /** Texto perceptível do botão. `title` mantém compatibilidade durante a migração. */
  label?: string;
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void;
  type?: 'button' | 'submit';
  /** ID de um `<form>` externo, para submissão a partir de um botão fora do form (ex.: rodapé de Drawer). */
  form?: string;
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

/** Botão de ação sempre textual; controles compactos usam IconActionButton. */
const ActionButton: React.FC<ActionButtonProps> = ({
  title, icon, label, onClick, type = 'button', form, color = 'primary', variant = 'contained',
  size = 'small', disabled, fullWidth, component, sx, children, ariaLabel,
}) => {
  const buttonEl = (
    <Button
      type={type} form={form} color={color} variant={variant} size={size} onClick={onClick}
      startIcon={icon} disabled={disabled} fullWidth={fullWidth}
      sx={sx} aria-label={ariaLabel ?? title} {...(component ? { component } : {})}
    >
      {label ?? title}
      {children}
    </Button>
  );

  return buttonEl;
};

export default ActionButton;
