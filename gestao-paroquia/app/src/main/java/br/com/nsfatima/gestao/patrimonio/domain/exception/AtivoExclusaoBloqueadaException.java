package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada quando a exclusão lógica de um ativo é proibida por haver manutenção em aberto ou
 * reservas futuras confirmadas/pendentes vinculadas ao bem.
 */
public class AtivoExclusaoBloqueadaException extends RuntimeException {

    public AtivoExclusaoBloqueadaException(String message) {
        super(message);
    }
}
