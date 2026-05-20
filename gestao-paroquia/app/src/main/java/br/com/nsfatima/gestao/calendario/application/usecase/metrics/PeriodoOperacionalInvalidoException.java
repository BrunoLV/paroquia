package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

public class PeriodoOperacionalInvalidoException extends RuntimeException {

    private final String field;
    private final boolean requiredField;

    public PeriodoOperacionalInvalidoException(String field, String message, boolean requiredField) {
        super(message);
        this.field = field;
        this.requiredField = requiredField;
    }

    public String getField() {
        return field;
    }

    public boolean isRequiredField() {
        return requiredField;
    }
}
