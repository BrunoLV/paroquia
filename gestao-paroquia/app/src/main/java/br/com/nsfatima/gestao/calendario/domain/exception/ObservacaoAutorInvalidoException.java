package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando um usuário tenta editar ou excluir uma observação de evento da
 * qual não é o autor — apenas o próprio autor pode alterar uma observação manual
 * (tipo {@code NOTA}).
 */
public class ObservacaoAutorInvalidoException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo a violação de autoria.
     *
     * @param message descrição do erro de autor inválido para a observação
     */
    public ObservacaoAutorInvalidoException(String message) {
        super(message);
    }
}
