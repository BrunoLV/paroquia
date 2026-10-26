package br.com.nsfatima.gestao.support.api.v1.error;

/**
 * Catálogo central de códigos de erro de negócio/validação usados nas respostas padronizadas
 * ({@link ValidationErrorResponse}) em toda a API. Centralizar os códigos aqui (em vez de strings
 * literais espalhadas pelos handlers de cada contexto) evita duplicação e divergência de valores
 * entre backend e frontend, que consome estes mesmos códigos para exibir mensagens localizadas.
 */
public enum ErrorCodes {
    VALIDATION_ERROR,
    VALIDATION_REQUIRED_FIELD,
    VALIDATION_FIELD_INVALID,
    VALIDATION_ENUM_VALUE_INVALID,
    VALIDATION_UNKNOWN_FIELD,
    AUTH_REQUIRED,
    AUTH_INVALID,
    ACCESS_DENIED,
    TOO_MANY_REQUESTS,
    ROLE_SCOPE_INVALID,
    AUTHZ_SOURCE_UNAVAILABLE,
    SESSION_EXPIRED,
    RESOURCE_NOT_FOUND,
    EVENT_NOT_FOUND,
    FORBIDDEN,
    APROVACAO_OBRIGATORIA,
    APROVACAO_NAO_ENCONTRADA,
    APROVACAO_JA_DECIDIDA,
    APROVACAO_EXECUCAO_FALHOU,
    INVALID_STATUS_TRANSITION,
    CONFLICT,
    CALENDAR_CONFLICT,
    IDEMPOTENCY_KEY_CONFLICT,
    CALENDAR_LOCKED_FOR_YEAR,
    OBSERVACAO_TIPO_MANUAL_INVALIDO,
    OBSERVACAO_NAO_ENCONTRADA,
    OBSERVACAO_AUTOR_INVALIDO,
    OBSERVACAO_TIPO_IMUTAVEL,
    AUDIT_PERSISTENCE_REQUIRED,
    CONFLITO_AGENDA_ECLESIAL,
    CONFLITO_RESERVA,
    DOMAIN_RULE_VIOLATION
}
