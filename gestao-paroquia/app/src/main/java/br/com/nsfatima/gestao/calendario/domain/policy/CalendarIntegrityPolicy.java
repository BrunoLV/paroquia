package br.com.nsfatima.gestao.calendario.domain.policy;

import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CalendarIntegrityPolicy {

    public static final String CONFLICT_PENDING = "CONFLICT_PENDING";

    public void validateInterval(Instant inicioUtc, Instant fimUtc) {
        if (inicioUtc == null || fimUtc == null) {
            throw new IllegalArgumentException("inicio/fim devem ser informados");
        }
        if (!fimUtc.isAfter(inicioUtc)) {
            throw new IllegalArgumentException("fim deve ser maior que inicio");
        }
    }

    public String resolveConflictState(boolean hasOverlap) {
        return hasOverlap ? CONFLICT_PENDING : null;
    }

    public String resolveConflictReason(boolean hasOverlap) {
        return hasOverlap ? "Detected overlap with an existing event" : null;
    }
}
