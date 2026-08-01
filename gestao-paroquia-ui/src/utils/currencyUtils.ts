const formatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatCurrencyBRL = (value: number | null | undefined): string => {
  if (value === null || value === undefined) {
    return '—';
  }
  return formatter.format(value);
};
