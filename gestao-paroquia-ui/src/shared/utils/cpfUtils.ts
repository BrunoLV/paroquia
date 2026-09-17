/**
 * Utilitários para formatação, limpeza e anonimização (LGPD) de CPF.
 */

export type CpfMaskVariant =
  | 'full'          // 000.000.000-00 (Exibição padrão)
  | 'clean'         // 00000000000 (Apenas dígitos)
  | 'masked_middle' // 123.***.***-89 (LGPD: oculta o meio)
  | 'masked_ends'   // ***.456.789-** (LGPD: oculta início e dígitos verificadores)
  | 'last_five'     // ***.***.789-89 (LGPD: exibe 5 últimos dígitos)
  | 'masked_dv';    // 123.456.789-** (LGPD: oculta apenas DV)

/**
 * Remove todos os caracteres não numéricos.
 */
export function cleanCpf(cpf?: string | null): string {
  if (!cpf) return '';
  return cpf.replace(/\D/g, '');
}

/**
 * Formata um CPF para exibição ou entrada progressiva.
 *
 * @param cpf CPF com ou sem caracteres de formatação
 * @param variant Variante de formatação/anonimização (padrão: 'full')
 * @returns String formatada ou '-' se for inválida/nula
 */
export function formatCpf(cpf?: string | null, variant: CpfMaskVariant = 'full'): string {
  if (!cpf || !cpf.trim()) {
    return '-';
  }

  const digits = cleanCpf(cpf);

  if (digits.length !== 11) {
    // Se for entrada parcial durante digitação no formulário
    if (variant === 'full' && digits.length > 0 && digits.length < 11) {
      return formatProgressiveCpf(digits);
    }
    return cpf.trim() || '-';
  }

  const d1 = digits.slice(0, 3);
  const d2 = digits.slice(3, 6);
  const d3 = digits.slice(6, 9);
  const dv = digits.slice(9, 11);

  switch (variant) {
    case 'clean':
      return digits;
    case 'masked_middle':
      return `${d1}.***.***-${dv}`;
    case 'masked_ends':
      return `***.${d2}.${d3}-**`;
    case 'last_five':
      return `***.***.${d3}-${dv}`;
    case 'masked_dv':
      return `${d1}.${d2}.${d3}-**`;
    case 'full':
    default:
      return `${d1}.${d2}.${d3}-${dv}`;
  }
}

/**
 * Formata progressivamente enquanto o usuário digita no input.
 */
export function formatProgressiveCpf(digits: string): string {
  const clean = digits.replace(/\D/g, '').slice(0, 11);
  if (clean.length <= 3) return clean;
  if (clean.length <= 6) return `${clean.slice(0, 3)}.${clean.slice(3)}`;
  if (clean.length <= 9) return `${clean.slice(0, 3)}.${clean.slice(3, 6)}.${clean.slice(6)}`;
  return `${clean.slice(0, 3)}.${clean.slice(3, 6)}.${clean.slice(6, 9)}-${clean.slice(9)}`;
}
