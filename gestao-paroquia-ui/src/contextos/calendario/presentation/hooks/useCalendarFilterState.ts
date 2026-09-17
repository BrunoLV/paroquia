import { useState } from 'react';

export const CALENDAR_CATEGORIES = [
  'PASTORAL',
  'SOCIAL',
  'LITURGICO',
  'ADMINISTRATIVO',
  'SACRAMENTAL',
  'FORMATIVO',
  'ASSISTENCIAL',
];

/** Estado bruto dos filtros de organização/categoria do calendário — a lista de organizações
 * permitidas para o usuário atual é derivada em {@link useCalendarData}, que já detém os dados
 * de organizações. */
export const useCalendarFilterState = () => {
  const [selectedOrgFilter, setSelectedOrgFilter] = useState<string>('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('');

  return { selectedOrgFilter, setSelectedOrgFilter, selectedCategoryFilter, setSelectedCategoryFilter };
};
