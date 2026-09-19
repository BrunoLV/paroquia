package br.com.nsfatima.gestao.aprovacao.domain.exception;

/**
 * Lançada quando a solicitação foi aprovada, mas a execução da ação de negócio associada (via
 * {@code EstrategiaExecucaoAprovacao}) falhou, ou quando a solicitação expirou/tem tipo não suportado. Sinaliza
 * ao {@code DecidirSolicitacaoAprovacaoUseCase} que a aprovação deve ser marcada com status FALHA_EXECUCAO —
 * a decisão foi registrada como aprovada, mas o efeito colateral esperado (ex.: efetivar o evento) não ocorreu.
 */
public class AprovacaoExecucaoFalhouException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo a falha de execução.
     *
     * @param message mensagem descritiva do erro
     */
    public AprovacaoExecucaoFalhouException(String message) {
        super(message);
    }
}
