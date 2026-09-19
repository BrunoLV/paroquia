package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando um usuário tenta executar uma operação para a qual não possui
 * autorização — papel/escopo organizacional insuficiente para visualizar, alterar,
 * cancelar ou decidir aprovações de eventos, projetos ou outros recursos protegidos
 * por regras de autorização baseadas em papel e escopo.
 */
public class OperacaoProibidaException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo a operação não autorizada.
     *
     * @param message descrição da restrição de autorização violada
     */
    public OperacaoProibidaException(String message) {
        super(message);
    }
}
