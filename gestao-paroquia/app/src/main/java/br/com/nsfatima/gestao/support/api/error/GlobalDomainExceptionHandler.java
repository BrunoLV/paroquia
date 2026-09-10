package br.com.nsfatima.gestao.support.api.error;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.local.domain.exception.LocalBusinessException;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.GrupoParoquialNegocioException;
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
 * Handler global de exceções de regra de negócio dos contextos de domínio (local, organização,
 * usuário). Converte exceções específicas de cada contexto em respostas HTTP padronizadas
 * ({@link ValidationErrorResponse}), evitando que cada contexto precise reimplementar o mapeamento
 * de erro. A ordem {@code @Order(2)} garante que handlers mais específicos (ex.: validação de
 * request) sejam avaliados antes deste handler genérico.
 */
@RestControllerAdvice
@Order(2)
public class GlobalDomainExceptionHandler extends BaseExceptionHandler {

    public GlobalDomainExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    /**
     * Trata exceções de regra de negócio do contexto de local. A mensagem da exceção é inspecionada
     * para decidir entre {@code 404} (recurso inexistente) e {@code 409} (conflito de regra), pois
     * {@link LocalBusinessException} não distingue esses dois casos por tipo.
     *
     * @param ex exceção de negócio lançada pelo contexto de local
     * @return resposta HTTP padronizada com o código de erro de violação de regra de domínio
     */
    @ExceptionHandler(LocalBusinessException.class)
    public ResponseEntity<ValidationErrorResponse> handleLocalBusiness(LocalBusinessException ex) {
        HttpStatus status = ex.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return buildValidation(
                status,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "local",
                        ex.getMessage(),
                        null)));
    }

    /**
     * Trata exceções de regra de negócio do contexto de organização, aplicando a mesma heurística de
     * inspeção de mensagem usada em {@link #handleLocalBusiness(LocalBusinessException)} para escolher
     * entre {@code 404} e {@code 409}.
     *
     * @param ex exceção de negócio lançada pelo contexto de organização
     * @return resposta HTTP padronizada com o código de erro de violação de regra de domínio
     */
    @ExceptionHandler(GrupoParoquialNegocioException.class)
    public ResponseEntity<ValidationErrorResponse> handleOrganizationBusiness(GrupoParoquialNegocioException ex) {
        HttpStatus status = ex.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return buildValidation(
                status,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "organization",
                        ex.getMessage(),
                        null)));
    }

    /**
     * Trata a ausência de um usuário buscado por identificador, sempre respondendo {@code 404}
     * já que este caso não tem ambiguidade entre "não encontrado" e "conflito".
     *
     * @param ex exceção lançada quando o usuário não é encontrado
     * @return resposta HTTP {@code 404} com o código de erro de recurso não encontrado
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleUsuarioNotFound(UsuarioNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.RESOURCE_NOT_FOUND.name(),
                        "usuario",
                        ex.getMessage(),
                        null)));
    }
}
