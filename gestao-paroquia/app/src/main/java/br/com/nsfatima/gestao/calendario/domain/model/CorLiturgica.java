package br.com.nsfatima.gestao.calendario.domain.model;

/**
 * Cor litúrgica associada a um marco/solenidade do calendário católico, usada
 * apenas para representação visual e informativa do ano litúrgico calculado por
 * {@code MotorLiturgico}.
 */
public enum CorLiturgica {
    /** Alegria, glória — usada em Natal, Páscoa, festas de Nossa Senhora e santos não mártires. */
    BRANCO,
    /** Sacrifício, martírio, Espírito Santo — usada em Pentecostes e festas de mártires. */
    VERMELHO,
    /** Penitência, preparação — usada no Advento e na Quaresma. */
    ROXO,
    /** Esperança, Tempo Comum — usada fora dos períodos festivos e penitenciais. */
    VERDE
}
