package br.com.nsfatima.gestao.patrimonio.domain.model;

/**
 * Status corrente de um ativo patrimonial no inventário. Reflete no ativo o efeito agregado de
 * suas ocupações ({@code AtivoAlocacaoEntity}): livre para uso, em manutenção, alocado a um
 * evento, ou definitivamente retirado do acervo.
 */
public enum AtivoStatus {
    DISPONIVEL,
    EM_MANUTENCAO,
    /** Ativo baixado/descartado — não pode mais ser alocado, transferido ou colocado em manutenção. */
    BAIXADO,
    ALOCADO
}
