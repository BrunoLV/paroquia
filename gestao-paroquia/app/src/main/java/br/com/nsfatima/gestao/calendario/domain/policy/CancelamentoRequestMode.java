package br.com.nsfatima.gestao.calendario.domain.policy;

/**
 * Modo de processamento de uma solicitação de cancelamento de evento, definido
 * conforme o papel/escopo do ator solicitante.
 */
public enum CancelamentoRequestMode {
    /** O cancelamento é aplicado imediatamente, sem passar pelo fluxo de aprovação. */
    IMMEDIATE,
    /** O cancelamento fica pendente até ser decidido através do fluxo de aprovação. */
    REQUIRES_APROVACAO
}
