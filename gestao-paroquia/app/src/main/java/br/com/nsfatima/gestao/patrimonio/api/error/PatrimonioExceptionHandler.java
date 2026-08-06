package br.com.nsfatima.gestao.patrimonio.api.error;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoCancelamentoInvalidoException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoDesalocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPeriodoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoTransferenciaBloqueadaException;
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
 * Traduz as exceções de domínio do contexto de patrimônio (ativo não encontrado, transferência
 * bloqueada, alocação/desalocação/manutenção/cancelamento inválidos, período conflitante) em
 * respostas HTTP de erro padronizadas para a API.
 */
@RestControllerAdvice
@Order(3)
public class PatrimonioExceptionHandler extends BaseExceptionHandler {

    private static final String CAMPO_ALOCACAO = "alocacao";

    public PatrimonioExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    /**
     * Ativo patrimonial inexistente para o id informado — retorna 404.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 404 padronizada
     */
    @ExceptionHandler(AtivoPatrimonialNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleNotFound(AtivoPatrimonialNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.RESOURCE_NOT_FOUND.name(), "ativo", ex.getMessage(), null)));
    }

    /**
     * Transferência de organização dona bloqueada pela política de vinculação (ex.: ativo com
     * alocação ativa) — retorna 409.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 409 padronizada
     */
    @ExceptionHandler(AtivoTransferenciaBloqueadaException.class)
    public ResponseEntity<ValidationErrorResponse> handleTransferenciaBloqueada(
            AtivoTransferenciaBloqueadaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "status", ex.getMessage(), null)));
    }

    /**
     * Pedido de alocação de ativo que viola uma regra de negócio (ex.: ativo indisponível,
     * em manutenção) — retorna 400.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 400 padronizada
     */
    @ExceptionHandler(AtivoAlocacaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleAlocacaoInvalida(AtivoAlocacaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(), CAMPO_ALOCACAO, ex.getMessage(), null)));
    }

    /**
     * Organização informada é inválida para a operação de patrimônio solicitada — retorna 400.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 400 padronizada
     */
    @ExceptionHandler(AtivoGrupoParoquialInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleGrupoParoquialInvalida(AtivoGrupoParoquialInvalidaException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(), "grupoParoquialId", ex.getMessage(), null)));
    }

    /**
     * Período de alocação solicitado conflita com outra ocupação já registrada para o mesmo
     * ativo — retorna 409.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 409 padronizada
     */
    @ExceptionHandler(AtivoPeriodoConflitanteException.class)
    public ResponseEntity<ValidationErrorResponse> handlePeriodoConflitante(AtivoPeriodoConflitanteException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "periodo", ex.getMessage(), null)));
    }

    /**
     * Pedido de desalocação inválido para o estado corrente do ativo/alocação — retorna 409.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 409 padronizada
     */
    @ExceptionHandler(AtivoDesalocacaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleDesalocacaoInvalida(AtivoDesalocacaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), CAMPO_ALOCACAO, ex.getMessage(), null)));
    }

    /**
     * Início/finalização de manutenção inválidos para o estado corrente do ativo — retorna 409.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 409 padronizada
     */
    @ExceptionHandler(AtivoManutencaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleManutencaoInvalida(AtivoManutencaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "manutencao", ex.getMessage(), null)));
    }

    /**
     * Cancelamento de alocação inválido — a alocação já teve seu período iniciado ou já não está
     * mais em estado cancelável — retorna 409.
     *
     * @param ex exceção de domínio capturada
     * @return resposta de erro 409 padronizada
     */
    @ExceptionHandler(AtivoAlocacaoCancelamentoInvalidoException.class)
    public ResponseEntity<ValidationErrorResponse> handleCancelamentoInvalido(
            AtivoAlocacaoCancelamentoInvalidoException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), CAMPO_ALOCACAO, ex.getMessage(), null)));
    }
}
