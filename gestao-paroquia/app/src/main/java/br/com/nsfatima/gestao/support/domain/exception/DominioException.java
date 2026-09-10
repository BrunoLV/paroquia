package br.com.nsfatima.gestao.support.domain.exception;

/**
 * Base opcional para exceções de regra de negócio disparadas pela camada de domínio.
 *
 * <p>Nunca deve carregar PII em mensagem, causa ou contexto (LGPD).</p>
 */
public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensagem) {
        super(mensagem);
    }

    protected DominioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
