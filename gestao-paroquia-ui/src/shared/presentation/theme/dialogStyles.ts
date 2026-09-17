import type { SxProps, Theme } from '@mui/material';

/** Limita a altura do conteúdo do Dialog e habilita rolagem interna em telas baixas (padrão §5.5 do design system). */
export const DIALOG_CONTENT_SCROLL_SX: SxProps<Theme> = {
  maxHeight: 'calc(100vh - 200px)',
  overflowY: 'auto',
};
