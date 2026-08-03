package br.com.nsfatima.gestao.aprovacao.domain.exception;

/**
 * Lançada quando se tenta decidir (aprovar/reprovar) uma solicitação de aprovação cujo status não é mais
 * PENDENTE — ou seja, a solicitação já foi decidida anteriormente. Impede decisões duplicadas/concorrentes
 * sobre a mesma solicitação.
 */
public class AprovacaoJaDecididaException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo a solicitação já decidida.
     *
     * @param message mensagem descritiva do erro
     */
    public AprovacaoJaDecididaException(String message) {
        super(message);
    }
}
