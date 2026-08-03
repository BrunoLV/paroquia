package br.com.nsfatima.gestao.support.api.v1.error;

/**
 * Detalhe individual de um erro de validação/regra de negócio dentro de
 * {@link ValidationErrorResponse}, identificando o campo afetado (quando aplicável) e o valor que
 * foi rejeitado, para permitir que o frontend destaque o campo problemático em formulários.
 */
public record ValidationErrorItem(
        String code,
        String field,
        String message,
        String rejectedValue) {
}
