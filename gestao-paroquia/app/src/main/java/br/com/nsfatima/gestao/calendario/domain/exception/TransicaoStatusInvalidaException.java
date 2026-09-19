package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Exceção lançada quando um evento tenta transicionar para um status inválido.
 */
public class TransicaoStatusInvalidaException extends RuntimeException {

    private final String currentStatus;
    private final String targetStatus;

    public TransicaoStatusInvalidaException(String currentStatus, String targetStatus, String detail) {
        super("Transição de status inválida de '%s' para '%s': %s".formatted(currentStatus, targetStatus, detail));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }
}
