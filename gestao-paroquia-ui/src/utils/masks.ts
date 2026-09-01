/**
 * Utilitários para formatação e mascaramento de dados (LGPD / UX).
 */

export const formatRg = (value: string | null | undefined): string => {
  if (!value) return '';
  const digits = value.replace(/[^\w]/g, '').toUpperCase().slice(0, 9);
  if (digits.length <= 2) return digits;
  if (digits.length <= 5) return `${digits.slice(0, 2)}.${digits.slice(2)}`;
  if (digits.length <= 8) return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5)}`;
  return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}-${digits.slice(8)}`;
};

export const maskRgPartial = (value: string | null | undefined): string => {
  if (!value) return '';
  const formatted = formatRg(value);
  if (formatted.length < 12) return formatted;
  return `**.${formatted.slice(3, 9)}-*`;
};

export const formatPhone = (value: string | null | undefined): string => {
  if (!value) return '';
  const digits = value.replace(/\D/g, '').slice(0, 11);
  if (digits.length === 0) return '';
  if (digits.length <= 2) return `(${digits}`;
  if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
  if (digits.length <= 10) return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7, 11)}`;
};

export const formatCep = (value: string | null | undefined): string => {
  if (!value) return '';
  const digits = value.replace(/\D/g, '').slice(0, 8);
  if (digits.length <= 5) return digits;
  return `${digits.slice(0, 5)}-${digits.slice(5)}`;
};

export const formatDateBr = (value: string | null | undefined): string => {
  if (!value) return '';
  const parts = value.split('-');
  if (parts.length === 3) {
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }
  return value;
};
