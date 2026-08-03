package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.domain.policy.IntegridadeCalendarioPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class EventoDomainService {

    private final IntegridadeCalendarioPolicy calendarIntegrityPolicy;

    public EventoDomainService(IntegridadeCalendarioPolicy calendarIntegrityPolicy) {
        this.calendarIntegrityPolicy = calendarIntegrityPolicy;
    }

    public void validateEvento(Instant inicio, Instant fim, EventoStatusInput status, String justificativa) {
        calendarIntegrityPolicy.validateInterval(inicio, fim);
        EventoStatusInput canonicalStatus = status == null ? EventoStatusInput.RASCUNHO : status;
        if (canonicalStatus == EventoStatusInput.ADICIONADO_EXTRA
                && (justificativa == null || justificativa.isBlank())) {
            throw new IllegalArgumentException("ADICIONADO_EXTRA exige justificativa");
        }
    }

    public void validateOrganizacaoParticipantes(UUID organizacaoResponsavelId, List<UUID> participantes) {
        if (organizacaoResponsavelId == null) {
            throw new IllegalArgumentException("organizacaoResponsavelId deve ser informado");
        }
        if (participantes == null || participantes.isEmpty()) {
            return;
        }

        if (participantes.contains(organizacaoResponsavelId)) {
            throw new IllegalArgumentException("organizacao responsavel nao pode repetir em participantes");
        }

        if (new HashSet<>(participantes).size() != participantes.size()) {
            throw new IllegalArgumentException("participantes nao podem conter valores duplicados");
        }
    }

    public EstadoConflitoAgenda resolveEstadoConflito(boolean hasOverlap) {
        return calendarIntegrityPolicy.resolveEstadoConflito(hasOverlap);
    }

    public String resolveMotivoConflito(boolean hasOverlap) {
        return calendarIntegrityPolicy.resolveMotivoConflito(hasOverlap);
    }
}
