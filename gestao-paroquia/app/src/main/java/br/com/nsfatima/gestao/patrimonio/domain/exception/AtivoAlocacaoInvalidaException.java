package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada quando um pedido de alocação de ativo patrimonial viola uma regra de negócio (ex.:
 * ativo indisponível para o período, evento/local referenciado inválido, ocupação vinculada a uma
 * aprovação não encontrada).
 */
public class AtivoAlocacaoInvalidaException extends RuntimeException {

    public AtivoAlocacaoInvalidaException(String message) {
        super(message);
    }
}
