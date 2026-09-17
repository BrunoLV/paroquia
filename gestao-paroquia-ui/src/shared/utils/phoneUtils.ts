/**
 * Formata um número de telefone com a máscara brasileira (XX) XXXXX-XXXX ou (XX) XXXX-XXXX.
 *
 * @param phone String de telefone com ou sem formatação original
 * @returns String formatada ou '-' se for nula/vazia
 */
export function formatPhone(phone?: string | null): string {
  if (!phone || !phone.trim()) {
    return '-';
  }

  // Extrai apenas os caracteres numéricos
  const digits = phone.replace(/\D/g, '');

  if (digits.length === 11) {
    // Celular com DDD: (XX) XXXXX-XXXX
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  }

  if (digits.length === 10) {
    // Fixo com DDD: (XX) XXXX-XXXX
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
  }

  // Se já estiver formatado ou não contiver 10 ou 11 dígitos numéricos
  if (phone.includes('(') && phone.includes(')')) {
    return phone.trim();
  }

  return phone.trim() || '-';
}
