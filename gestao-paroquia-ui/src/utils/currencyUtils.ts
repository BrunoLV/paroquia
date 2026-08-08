const formatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatCurrencyBRL = (value: number | null | undefined): string =>
  value == null ? '—' : formatter.format(value);

