package br.com.nsfatima.gestao.support.api.v1.error;

import java.util.List;

public record ValidationErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        List<ValidationErrorItem> errors) {
}
