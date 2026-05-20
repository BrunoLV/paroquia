package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

public class CalendarLockedException extends RuntimeException {
    private final int ano;

    public CalendarLockedException(int ano) {
        super("O calendario para o ano " + ano + " esta fechado. Novos eventos devem ser registrados como ADICIONADO_EXTRA.");
        this.ano = ano;
    }

    public int getAno() {
        return ano;
    }
}
