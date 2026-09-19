package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando uma operação tenta alterar uma regra eclesial imutável do
 * provisionamento litúrgico (por exemplo, o ciclo litúrgico do ano ou a data do
 * Domingo de Páscoa), valores que são fixados por norma canônica e não podem ser
 * recalculados ou sobrescritos após a criação do ano litúrgico.
 */
public class RegraEclesialImutavelException extends RuntimeException {
    /**
     * Cria a exceção com a mensagem descrevendo a regra imutável violada.
     *
     * @param message descrição da regra eclesial que não pode ser alterada
     */
    public RegraEclesialImutavelException(String message) {
        super(message);
    }
}
