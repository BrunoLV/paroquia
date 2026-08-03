package br.com.nsfatima.gestao.calendario.domain.model;

/**
 * Classifica como a data de uma solenidade litúrgica é determinada, usado por
 * {@code MotorLiturgico} para decidir o algoritmo de cálculo do marco no ano
 * nominal.
 */
public enum TipoSolenidade {
    /** Data fixa no calendário civil, igual todos os anos (ex.: Natal, 25 de dezembro). */
    FIXA,
    /** Data calculada a partir do Domingo de Páscoa (ex.: Corpus Christi, Pentecostes). */
    MOVEL
}
