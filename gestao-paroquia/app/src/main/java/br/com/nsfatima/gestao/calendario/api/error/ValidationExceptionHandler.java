package br.com.nsfatima.gestao.calendario.api.error;

import br.com.nsfatima.gestao.calendario.domain.exception.PeriodoOperacionalInvalidoException;
import br.com.nsfatima.gestao.observabilidade.domain.exception.PersistenciaAuditoriaObrigatoriaException;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.support.api.v1.error.BaseExceptionHandler;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.List;

/**
 * Handles request validation and JSON mapping exceptions.
 */
@RestControllerAdvice
@Order(4)
public class ValidationExceptionHandler extends BaseExceptionHandler {

    public ValidationExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationErrorItem> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(this::toValidationItem)
                .toList();

        boolean hasDomainViolation = errors.stream()
                .anyMatch(e -> ErrorCodes.DOMAIN_RULE_VIOLATION.name().equals(e.code()));

        ErrorCodes mainCode = hasDomainViolation ? ErrorCodes.DOMAIN_RULE_VIOLATION : ErrorCodes.VALIDATION_ERROR;

        return buildValidation(HttpStatus.BAD_REQUEST, mainCode, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "Validation failed",
                List.of(toValidationItem(ex)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "requestBody",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(PeriodoOperacionalInvalidoException.class)
    public ResponseEntity<ValidationErrorResponse> handlePeriodoOperacionalInvalido(
            PeriodoOperacionalInvalidoException ex) {
        ErrorCodes code = ex.isRequiredField()
                ? ErrorCodes.VALIDATION_REQUIRED_FIELD
                : ErrorCodes.VALIDATION_FIELD_INVALID;
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                code,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        code.name(),
                        ex.getField(),
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(PersistenciaAuditoriaObrigatoriaException.class)
    public ResponseEntity<ValidationErrorResponse> handlePersistenciaAuditoriaObrigatoria(
            PersistenciaAuditoriaObrigatoriaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.AUDIT_PERSISTENCE_REQUIRED,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.AUDIT_PERSISTENCE_REQUIRED.name(),
                        "auditTrail",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ValidationErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex) {
        String message = "Parametro obrigatorio ausente: " + ex.getParameterName();
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_REQUIRED_FIELD,
                message,
                List.of(new ValidationErrorItem(
                        ErrorCodes.VALIDATION_REQUIRED_FIELD.name(),
                        ex.getParameterName(),
                        message,
                        null)));
    }

    private ValidationErrorItem toValidationItem(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            ErrorCodes code = isRequiredField(fieldError) ? ErrorCodes.VALIDATION_REQUIRED_FIELD
                    : ErrorCodes.VALIDATION_FIELD_INVALID;
            Object rejectedValue = fieldError.getRejectedValue();
            return new ValidationErrorItem(
                    code.name(),
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    rejectedValue == null ? null : String.valueOf(rejectedValue));
        }

        ErrorCodes code = "ValidEventoDates".equals(error.getCode())
                ? ErrorCodes.DOMAIN_RULE_VIOLATION
                : ErrorCodes.VALIDATION_FIELD_INVALID;

        return new ValidationErrorItem(
                code.name(),
                "requestBody",
                error.getDefaultMessage(),
                null);
    }

    private ValidationErrorItem toValidationItem(HttpMessageNotReadableException ex) {
        Throwable rootCause = ex.getMostSpecificCause();
        if (rootCause instanceof UnrecognizedPropertyException unknownPropertyException) {
            return new ValidationErrorItem(
                    ErrorCodes.VALIDATION_UNKNOWN_FIELD.name(),
                    unknownPropertyException.getPropertyName(),
                    "Unknown field '%s'".formatted(unknownPropertyException.getPropertyName()),
                    unknownPropertyException.getPropertyName());
        }

        if (rootCause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType() != null
                && invalidFormatException.getTargetType().isEnum()) {
            return new ValidationErrorItem(
                    ErrorCodes.VALIDATION_ENUM_VALUE_INVALID.name(),
                    extractFieldName(invalidFormatException),
                    "Unsupported enum value '%s'".formatted(invalidFormatException.getValue()),
                    invalidFormatException.getValue() == null ? null
                            : String.valueOf(invalidFormatException.getValue()));
        }

        IllegalArgumentException enumNormalizationException = findCause(ex, IllegalArgumentException.class);
        if (enumNormalizationException != null
                && enumNormalizationException.getMessage() != null
                && enumNormalizationException.getMessage().startsWith("Unsupported enum value")) {
            JacksonException mappingException = findCause(ex, JacksonException.class);
            return new ValidationErrorItem(
                    ErrorCodes.VALIDATION_ENUM_VALUE_INVALID.name(),
                    extractFieldName(mappingException != null ? mappingException : ex),
                    enumNormalizationException.getMessage(),
                    extractRejectedValue(enumNormalizationException.getMessage()));
        }

        return new ValidationErrorItem(
                ErrorCodes.VALIDATION_FIELD_INVALID.name(),
                extractFieldName(rootCause),
                "Malformed JSON request",
                null);
    }

    private String extractRejectedValue(String message) {
        int firstQuote = message.indexOf('\'');
        if (firstQuote < 0) {
            return null;
        }

        int secondQuote = message.indexOf('\'', firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }

        return message.substring(firstQuote + 1, secondQuote);
    }

    private boolean isRequiredField(FieldError error) {
        String errorCode = error.getCode();
        return errorCode != null && switch (errorCode) {
            case "NotBlank", "NotNull", "NotEmpty" -> true;
            default -> false;
        };
    }

    private String extractFieldName(Throwable throwable) {
        if (throwable instanceof JacksonException jacksonException
                && !jacksonException.getPath().isEmpty()) {
            JacksonException.Reference reference = jacksonException.getPath().getLast();
            if (reference.getPropertyName() != null && !reference.getPropertyName().isBlank()) {
                return reference.getPropertyName();
            }
        }
        return "requestBody";
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = "Tipo de dado inválido para o parâmetro: " + ex.getName();
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_FIELD_INVALID,
                message,
                List.of(new ValidationErrorItem(
                        ErrorCodes.VALIDATION_FIELD_INVALID.name(),
                        ex.getName(),
                        message,
                        ex.getValue() == null ? null : String.valueOf(ex.getValue()))));
    }
}
