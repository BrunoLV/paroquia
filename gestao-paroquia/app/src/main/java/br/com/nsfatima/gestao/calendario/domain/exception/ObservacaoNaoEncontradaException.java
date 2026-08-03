package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando uma observação de evento referenciada por identificador não é
 * encontrada — tipicamente ao atualizar ou excluir uma observação já removida ou
 * inexistente.
 */
public class ObservacaoNaoEncontradaException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo a observação não encontrada.
     *
     * @param message descrição do erro de observação não encontrada
     */
    public ObservacaoNaoEncontradaException(String message) {
        super(message);
    }
}
