package br.com.nsfatima.gestao.calendario.domain.exception;

public class CalendarioBloqueadoException extends RuntimeException {
    private final int ano;

    public CalendarioBloqueadoException(int ano) {
        super("O calendario para o ano " + ano +
                " esta fechado. Novos eventos devem ser registrados como ADICIONADO_EXTRA.");
        this.ano = ano;
    }

    public int getAno() {
        return ano;
    }
}
