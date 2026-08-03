package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class IntegridadeCalendarioPolicy {

    public void validateInterval(Instant inicioUtc, Instant fimUtc) {
        if (inicioUtc == null || fimUtc == null) {
            throw new IllegalArgumentException("inicio/fim devem ser informados");
        }
        if (!fimUtc.isAfter(inicioUtc)) {
            throw new IllegalArgumentException("fim deve ser maior que inicio");
        }
    }

    public EstadoConflitoAgenda resolveEstadoConflito(boolean hasOverlap) {
        return hasOverlap ? EstadoConflitoAgenda.PENDENTE : null;
    }

    public String resolveMotivoConflito(boolean hasOverlap) {
        return hasOverlap ? "Sobreposição detectada com um evento existente" : null;
    }
}
