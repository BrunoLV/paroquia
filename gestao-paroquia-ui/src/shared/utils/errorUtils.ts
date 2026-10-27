import type { ApiHttpError } from '@/shared/infrastructure/http/apiError';

interface ApiErrorItem {
  message?: string;
  defaultMessage?: string;
}

interface ApiErrorResponseData {
  message?: string;
  errors?: ApiErrorItem[];
}

/**
 * Extrai uma mensagem de erro amigável de uma resposta da API ou retorna o fallback.
 */
export function extractErrorMessage(err: unknown, fallback: string): string {
  if (err && typeof err === 'object' && 'response' in err) {
    const axiosError = err as ApiHttpError<ApiErrorResponseData>;
    const data = axiosError.response?.data;
    if (data) {
      if (Array.isArray(data.errors) && data.errors.length > 0) {
        const errorList = data.errors
          .map((e) => `• ${e.message || e.defaultMessage || 'Erro desconhecido'}`)
          .join('\n');
        return `Por favor, corrija os seguintes erros:\n${errorList}`;
      }
      if (typeof data.message === 'string' && data.message.trim().length > 0) {
        return data.message;
      }
    }
  }
  return fallback;
}
