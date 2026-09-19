package br.com.nsfatima.gestao.patrimonio.api.error;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoExclusaoBloqueadaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNaoEncontradoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.ReservaImpeditiva;
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
 * Traduz as exceções de domínio do contexto de patrimônio em respostas HTTP padronizadas: ativo
 * não encontrado (404), unidade territorial inválida (400), manutenção inválida (409), manutenção
 * bloqueada por reservas conflitantes (409, com a lista das reservas) e exclusão bloqueada (409).
 */
@RestControllerAdvice
@Order(3)
public class PatrimonioExceptionHandler extends BaseExceptionHandler {

    public PatrimonioExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(AtivoPatrimonialNaoEncontradoException.class)
    public ResponseEntity<ValidationErrorResponse> handleNotFound(AtivoPatrimonialNaoEncontradoException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.RESOURCE_NOT_FOUND.name(), "ativo", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoGrupoParoquialInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleGrupoParoquialInvalida(
            AtivoGrupoParoquialInvalidaException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(), "grupoParoquialId", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoManutencaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleManutencaoInvalida(AtivoManutencaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "manutencao", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoManutencaoConflitanteException.class)
    public ResponseEntity<ValidationErrorResponse> handleManutencaoConflitante(AtivoManutencaoConflitanteException ex) {
        List<ValidationErrorItem> itens = ex.getReservasImpeditivas().stream()
                .map(PatrimonioExceptionHandler::toItem)
                .toList();
        return buildValidation(HttpStatus.CONFLICT, ErrorCodes.INVALID_STATUS_TRANSITION, ex.getMessage(), itens);
    }

    @ExceptionHandler(AtivoExclusaoBloqueadaException.class)
    public ResponseEntity<ValidationErrorResponse> handleExclusaoBloqueada(AtivoExclusaoBloqueadaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "exclusao", ex.getMessage(), null)));
    }

    private static ValidationErrorItem toItem(ReservaImpeditiva r) {
        String descricao = "%s (%s a %s) — %s".formatted(
                r.descricao(), r.inicio(), r.fim(), r.solicitanteNome());
        return new ValidationErrorItem(
                ErrorCodes.INVALID_STATUS_TRANSITION.name(), "reservaImpeditiva", descricao, r.reservaId().toString());
    }
}
