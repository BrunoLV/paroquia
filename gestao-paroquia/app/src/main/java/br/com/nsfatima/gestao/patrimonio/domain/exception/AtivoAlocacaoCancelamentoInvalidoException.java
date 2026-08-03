package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada ao tentar cancelar uma ocupação (alocação ou manutenção) de ativo que já teve seu
 * período iniciado, ou que não está mais em um status cancelável — apenas reservas futuras ainda
 * não iniciadas podem ser canceladas.
 */
public class AtivoAlocacaoCancelamentoInvalidoException extends RuntimeException {

    public AtivoAlocacaoCancelamentoInvalidoException(String message) {
        super(message);
    }
}
