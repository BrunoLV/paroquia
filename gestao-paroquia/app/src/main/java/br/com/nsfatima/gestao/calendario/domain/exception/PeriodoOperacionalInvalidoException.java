package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando um período operacional de evento (janela de datas/horários de
 * início e fim) viola as regras de {@code PeriodoOperacionalPolicy} — por exemplo,
 * data de término anterior à de início, campo obrigatório ausente ou combinação de
 * campos inconsistente com o tipo de recorrência do evento.
 */
public class PeriodoOperacionalInvalidoException extends RuntimeException {

    private final String field;
    private final boolean requiredField;

    /**
     * Cria a exceção identificando o campo inválido e se a violação é de ausência de
     * campo obrigatório ou de valor inconsistente.
     *
     * @param field campo do período operacional que originou a violação
     * @param message descrição da regra de período operacional violada
     * @param requiredField {@code true} se a violação é a ausência de um campo obrigatório
     */
    public PeriodoOperacionalInvalidoException(String field, String message, boolean requiredField) {
        super(message);
        this.field = field;
        this.requiredField = requiredField;
    }

    /**
     * Retorna o nome do campo do período operacional que violou a regra de negócio.
     *
     * @return nome do campo inválido
     */
    public String getField() {
        return field;
    }

    /**
     * Indica se a violação decorre da ausência de um campo obrigatório.
     *
     * @return {@code true} se o campo era obrigatório e não foi informado
     */
    public boolean isRequiredField() {
        return requiredField;
    }
}
