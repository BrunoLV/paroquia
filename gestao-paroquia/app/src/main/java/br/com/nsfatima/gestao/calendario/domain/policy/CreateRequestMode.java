package br.com.nsfatima.gestao.calendario.domain.policy;

/**
 * Modo de processamento de uma solicitação de criação de evento, definido
 * conforme o papel/escopo do ator solicitante.
 */
public enum CreateRequestMode {
    /** O evento é criado imediatamente, sem passar pelo fluxo de aprovação. */
    IMMEDIATE,
    /** A criação fica pendente até ser decidida através do fluxo de aprovação. */
    REQUIRES_APROVACAO
}
