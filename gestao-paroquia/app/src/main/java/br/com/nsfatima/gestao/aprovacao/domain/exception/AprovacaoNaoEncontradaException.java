package br.com.nsfatima.gestao.aprovacao.domain.exception;

/**
 * Lançada quando uma solicitação de aprovação referenciada por ID não é encontrada — tipicamente ao tentar
 * decidir ou consultar uma aprovação inexistente.
 */
public class AprovacaoNaoEncontradaException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo a aprovação não encontrada.
     *
     * @param message mensagem descritiva do erro
     */
    public AprovacaoNaoEncontradaException(String message) {
        super(message);
    }
}
