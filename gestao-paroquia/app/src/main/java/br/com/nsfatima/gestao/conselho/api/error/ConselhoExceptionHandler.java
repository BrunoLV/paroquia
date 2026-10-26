package br.com.nsfatima.gestao.conselho.api.error;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaImutavelException;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaNaoEncontradaException;
import br.com.nsfatima.gestao.conselho.domain.exception.TipoReuniaoIncompativelException;
import br.com.nsfatima.gestao.conselho.domain.exception.TransicaoAtaInvalidaException;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.support.api.v1.error.BaseExceptionHandler;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduz as exceções do contexto de atas: ata inexistente ou fora do escopo do ator (404), ata selada ou
 * transição vedada pelo estado (409) e conteúdo ou tipo de reunião inválido (422).
 */
@RestControllerAdvice
@Order(2)
public class ConselhoExceptionHandler extends BaseExceptionHandler {

    private static final String CAMPO_ATA = "ata";

    public ConselhoExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(AtaNaoEncontradaException.class)
    public ResponseEntity<ValidationErrorResponse> handleNaoEncontrada(AtaNaoEncontradaException ex) {
        return resposta(HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({AtaImutavelException.class, TransicaoAtaInvalidaException.class})
    public ResponseEntity<ValidationErrorResponse> handleTransicao(RuntimeException ex) {
        return resposta(HttpStatus.CONFLICT, ErrorCodes.INVALID_STATUS_TRANSITION, ex.getMessage());
    }

    @ExceptionHandler({AtaInvalidaException.class, TipoReuniaoIncompativelException.class})
    public ResponseEntity<ValidationErrorResponse> handleInvalida(RuntimeException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_CONTENT, ErrorCodes.DOMAIN_RULE_VIOLATION, ex.getMessage());
    }

    private ResponseEntity<ValidationErrorResponse> resposta(HttpStatus status, ErrorCodes codigo, String mensagem) {
        return buildValidation(status, codigo, mensagem,
                List.of(new ValidationErrorItem(codigo.name(), CAMPO_ATA, mensagem, null)));
    }
}
