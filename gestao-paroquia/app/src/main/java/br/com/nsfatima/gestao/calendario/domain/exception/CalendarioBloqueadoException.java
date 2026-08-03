package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando se tenta registrar um evento comum em um ano cujo calendário
 * litúrgico já foi encerrado (provisionamento fechado). Regra de negócio: uma vez
 * fechado, o ano só aceita novos eventos como {@code ADICIONADO_EXTRA}, nunca como
 * evento regular de calendário.
 */
public class CalendarioBloqueadoException extends RuntimeException {
    private final int ano;

    /**
     * Cria a exceção para o ano litúrgico bloqueado informado.
     *
     * @param ano ano litúrgico cujo calendário está fechado
     */
    public CalendarioBloqueadoException(int ano) {
        super("O calendario para o ano " + ano +
                " esta fechado. Novos eventos devem ser registrados como ADICIONADO_EXTRA.");
        this.ano = ano;
    }

    /**
     * Retorna o ano litúrgico cujo calendário está fechado.
     *
     * @return ano litúrgico bloqueado
     */
    public int getAno() {
        return ano;
    }
}
