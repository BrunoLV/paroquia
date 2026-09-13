import { alpha } from '@mui/material';
import theme from '../theme';

/** Compara ano/mês/dia, ignorando hora — usado para agrupar eventos por dia no grid/agenda. */
export const isSameDay = (d1: Date, d2: Date): boolean => (
  d1.getFullYear() === d2.getFullYear() &&
  d1.getMonth() === d2.getMonth() &&
  d1.getDate() === d2.getDate()
);

export const MONTH_NAMES = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

/** Espelha as cores de {@link getCategoriaEventoMeta} (mesmo mapeamento categoria→cor), como triplet bg/text/border derivado do tema em vez de hex fixo. */
export const getCategoryColor = (category: string) => {
  const p = theme.palette;
  switch (category) {
    case 'PASTORAL':
      return { bg: alpha(p.success.main, 0.12), text: p.success.dark, border: p.success.main };
    case 'SOCIAL':
      return { bg: alpha(p.warning.main, 0.12), text: p.warning.dark, border: p.warning.main };
    case 'LITURGICO':
      return { bg: alpha(p.secondary.main, 0.12), text: p.secondary.dark, border: p.secondary.main };
    case 'ADMINISTRATIVO':
      return { bg: alpha(p.grey[700], 0.12), text: p.grey[800], border: p.grey[500] };
    case 'SACRAMENTAL':
      return { bg: alpha(p.info.main, 0.12), text: p.info.dark, border: p.info.main };
    case 'FORMATIVO':
      return { bg: alpha(p.primary.main, 0.12), text: p.primary.dark, border: p.primary.main };
    case 'ASSISTENCIAL':
      return { bg: alpha(p.info.main, 0.12), text: p.info.dark, border: p.info.light };
    default:
      return { bg: alpha(p.grey[500], 0.12), text: p.grey[700], border: p.grey[400] };
  }
};
