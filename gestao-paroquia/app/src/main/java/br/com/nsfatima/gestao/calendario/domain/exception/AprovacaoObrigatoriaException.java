package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando uma mudança de data ou um cancelamento de evento é executado sem
 * uma aprovação válida vinculada — a aprovação precisa existir, pertencer ao evento,
 * estar no status {@code APROVADA} e ter sido decidida pelo Pároco
 * ({@code PAROCO}).
 */
public class AprovacaoObrigatoriaException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo a exigência de aprovação não
     * satisfeita.
     *
     * @param message descrição da regra de aprovação obrigatória violada
     */
    public AprovacaoObrigatoriaException(String message) {
        super(message);
    }
}
