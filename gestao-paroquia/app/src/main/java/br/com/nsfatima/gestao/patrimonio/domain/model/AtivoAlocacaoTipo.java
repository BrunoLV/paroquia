package br.com.nsfatima.gestao.patrimonio.domain.model;

/**
 * Natureza da ocupação de um ativo patrimonial, distinguindo alocação a um evento de retirada por
 * manutenção — os dois tipos disputam o mesmo espaço de disponibilidade do ativo.
 */
public enum AtivoAlocacaoTipo {
    /** Ativo alocado a um evento (uso operacional, podendo ser empréstimo entre organizações). */
    ALOCACAO_EVENTO,
    /** Ativo retirado de circulação para manutenção. */
    MANUTENCAO
}
