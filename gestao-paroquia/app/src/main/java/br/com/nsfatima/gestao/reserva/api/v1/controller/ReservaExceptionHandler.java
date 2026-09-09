package br.com.nsfatima.gestao.reserva.api.v1.controller;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.reserva.api.v1.dto.ConflitoDetalheResponse;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNotFoundException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.support.api.v1.error.BaseExceptionHandler;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interceptador e tratador centralizado de exceções de domínio do Módulo de Reservas,
 * convertendo erros de colisão de agenda, carência, autorização e integridade operacional
 * em respostas HTTP uniformes e auditadas.
 */
@RestControllerAdvice
@Order(2)
public class ReservaExceptionHandler extends BaseExceptionHandler {

    public ReservaExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    /**
     * Trata colisões de agenda ou de tempo de carência (buffer) entre reservas ativas e novas solicitações.
     * Retorna HTTP 409 com detalhes completos consumíveis pelos diálogos de aviso e resolução da UI.
     */
    @ExceptionHandler(ReservaConflitoException.class)
    public ResponseEntity<Map<String, Object>> handleConflito(ReservaConflitoException ex) {
        List<ConflitoDetalheResponse> detalhes = ex.getConflitos().stream()
                .map(ConflitoDetalheResponse::de)
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ErrorCodes.CONFLITO_RESERVA.name());
        body.put("error", ErrorCodes.CONFLITO_RESERVA.name());
        body.put("message", ex.getMessage());
        body.put("correlationId", resolveCorrelationId());
        body.put("conflitos", detalhes);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Trata tentativas de operação sem privilégio ou pertencimento pastoral adequado.
     */
    @ExceptionHandler(ReservaNaoAutorizadaException.class)
    public ResponseEntity<ValidationErrorResponse> handleNaoAutorizada(
            ReservaNaoAutorizadaException ex,
            HttpServletRequest request
    ) {
        logSecurityDenial(ErrorCodes.FORBIDDEN, ex.getMessage(), request);
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.FORBIDDEN,
                ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.FORBIDDEN.name(), "reserva", ex.getMessage(), null))
        );
    }

    /**
     * Trata recursos de reserva inexistentes para o identificador fornecido.
     */
    @ExceptionHandler(ReservaNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleNotFound(ReservaNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.RESOURCE_NOT_FOUND.name(), "reservaId", ex.getMessage(), null))
        );
    }

    /**
     * Trata transições ou argumentos de negócio inválidos no ciclo de vida da reserva.
     */
    @ExceptionHandler(ReservaOperacaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleOperacaoInvalida(ReservaOperacaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.DOMAIN_RULE_VIOLATION.name(), "operacao", ex.getMessage(), null))
        );
    }
}
