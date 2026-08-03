package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando uma requisição reutiliza uma chave de idempotência já registrada,
 * mas com um corpo de requisição diferente do original — indício de reuso indevido
 * da chave em vez de um retry legítimo da mesma operação.
 */
public class IdempotencyConflictException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo o conflito de idempotência detectado.
     *
     * @param message descrição do conflito entre a requisição atual e a original
     */
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
