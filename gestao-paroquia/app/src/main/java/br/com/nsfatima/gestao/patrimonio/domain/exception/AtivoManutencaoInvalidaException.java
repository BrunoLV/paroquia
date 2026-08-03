package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada quando o início ou a finalização de um período de manutenção de ativo patrimonial viola
 * uma regra de negócio (ex.: ativo já {@code BAIXADO}, período com data final anterior à
 * inicial, ausência de manutenção corrente a finalizar).
 */
public class AtivoManutencaoInvalidaException extends RuntimeException {

    public AtivoManutencaoInvalidaException(String message) {
        super(message);
    }
}
