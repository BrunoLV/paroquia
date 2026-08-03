package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada ao tentar desalocar um ativo patrimonial que não possui uma alocação de evento corrente
 * (status {@code CONFIRMADA} ou {@code ATRASADA}) para devolver.
 */
public class AtivoDesalocacaoInvalidaException extends RuntimeException {

    public AtivoDesalocacaoInvalidaException(String message) {
        super(message);
    }
}
