package br.com.nsfatima.gestao.support.api.v1.error;

import java.util.List;

/**
 * Corpo de resposta HTTP padronizado para erros de validação e de regra de negócio em toda a API,
 * usado pelos exception handlers deste e de outros contextos para uniformizar o formato de erro
 * consumido pelo frontend. O {@code correlationId} permite correlacionar a resposta com os
 * registros de log/auditoria da mesma requisição.
 */
public record ValidationErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        List<ValidationErrorItem> errors) {
}
