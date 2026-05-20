package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Exception thrown when an event attempt to transition to an illegal status.
 * 
 * Usage Example:
 * throw new InvalidStatusTransitionException("RASCUNHO", "CANCELADO", "Only confirmed events can be cancelled");
 */
public class InvalidStatusTransitionException extends RuntimeException {

    private final String currentStatus;
    private final String targetStatus;

    public InvalidStatusTransitionException(String currentStatus, String targetStatus, String detail) {
        super("Invalid status transition from '%s' to '%s': %s".formatted(currentStatus, targetStatus, detail));
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
