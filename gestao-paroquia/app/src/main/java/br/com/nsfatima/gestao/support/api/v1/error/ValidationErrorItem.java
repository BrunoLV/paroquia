package br.com.nsfatima.gestao.support.api.v1.error;

public record ValidationErrorItem(
        String code,
        String field,
        String message,
        String rejectedValue) {
}
