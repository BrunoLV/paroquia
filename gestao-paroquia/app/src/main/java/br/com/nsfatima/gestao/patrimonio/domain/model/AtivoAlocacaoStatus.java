package br.com.nsfatima.gestao.patrimonio.domain.model;

/**
 * Status do ciclo de vida de uma ocupação (alocação a evento ou manutenção) de um ativo
 * patrimonial.
 */
public enum AtivoAlocacaoStatus {
    /** Empréstimo entre organizações aguardando decisão de aprovação; ainda não afeta o ativo. */
    PENDENTE_APROVACAO,
    /** Ocupação confirmada e em vigor (ou agendada) sobre o ativo. */
    CONFIRMADA,
    /** Ocupação confirmada cuja data de fim já passou sem devolução registrada. */
    ATRASADA,
    /** Ocupação encerrada normalmente, com o ativo devolvido. */
    DEVOLVIDA,
    /** Ocupação cancelada antes de iniciar (reserva não efetivada, ou empréstimo reprovado). */
    CANCELADA
}
