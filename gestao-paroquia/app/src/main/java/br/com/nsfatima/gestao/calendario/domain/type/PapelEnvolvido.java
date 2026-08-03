package br.com.nsfatima.gestao.calendario.domain.type;

/**
 * Papel de uma pessoa envolvida (vinculada) a um evento de calendário, usado para
 * distinguir quem responde principalmente pela organização do evento de quem apenas
 * colabora com sua realização.
 */
public enum PapelEnvolvido {
    /** Pessoa principal responsável pela organização e execução do evento. */
    RESPONSAVEL,
    /** Pessoa que colabora/apoia a realização do evento, sem responsabilidade principal. */
    APOIO
}
