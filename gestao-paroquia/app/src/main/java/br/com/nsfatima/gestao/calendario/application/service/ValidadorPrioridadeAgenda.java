package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.policy.PrioridadeAgendaPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orquestra fatos persistidos da agenda para a {@link PrioridadeAgendaPolicy} e persiste somente
 * os cancelamentos que a regra já decidiu. A separação impede que a Policy conheça JPA ou mutile
 * entidades externas ao agregado em que a decisão foi tomada.
 */
public class ValidadorPrioridadeAgenda {

    private static final String MOTIVO_CANCELAMENTO_PADRAO =
            "Cancelado automaticamente devido a conflito de prioridade de agenda eclesial";

    private final EventoJpaRepository eventoJpaRepository;
    private final PrioridadeAgendaPolicy prioridadeAgendaPolicy;

    public ValidadorPrioridadeAgenda(
            EventoJpaRepository eventoJpaRepository,
            PrioridadeAgendaPolicy prioridadeAgendaPolicy) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.prioridadeAgendaPolicy = prioridadeAgendaPolicy;
    }

    /** Carrega sobreposições e delega a decisão pura de prioridade para a Policy. */
    @Transactional
    public List<EventoEntity> validarPrioridadeEConflitos(
            CategoriaEvento categoria,
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            UUID igrejaVinculoId,
            UUID eventoAtualId,
            boolean ignorarValidacao) {
        if (ignorarValidacao || categoria == null || inicioUtc == null || fimUtc == null) {
            return List.of();
        }
        List<EventoEntity> sobrepostos = eventoJpaRepository.findOverlappingEvents(inicioUtc, fimUtc, eventoAtualId);
        Map<UUID, EventoEntity> porId = sobrepostos.stream()
                .collect(Collectors.toMap(EventoEntity::getId, evento -> evento));
        PrioridadeAgendaPolicy.EventoSobreposto novoEvento = new PrioridadeAgendaPolicy.EventoSobreposto(
                null, categoria.getNivelPrioridade(), localId, igrejaVinculoId);
        return prioridadeAgendaPolicy.resolver(novoEvento, sobrepostos.stream().map(this::paraFato).toList()).stream()
                .map(decisao -> porId.get(decisao.id()))
                .toList();
    }

    /** Persiste o efeito operacional já decidido pela hierarquia eclesial. */
    @Transactional
    public void processarCancelamentosAutomaticos(List<EventoEntity> eventos, String motivo) {
        if (eventos == null || eventos.isEmpty()) {
            return;
        }
        String motivoFinal = motivo == null || motivo.isBlank() ? MOTIVO_CANCELAMENTO_PADRAO : motivo;
        for (EventoEntity evento : eventos) {
            evento.setStatus(EventoStatusInput.CANCELADO.name());
            evento.setCanceladoMotivo(motivoFinal);
        }
        eventoJpaRepository.saveAll(eventos);
    }

    private PrioridadeAgendaPolicy.EventoSobreposto paraFato(EventoEntity evento) {
        int prioridade = evento.getCategoria() == null ? 4 : evento.getCategoria().getNivelPrioridade();
        return new PrioridadeAgendaPolicy.EventoSobreposto(
                evento.getId(), prioridade, evento.getLocalId(), evento.getIgrejaVinculoId());
    }
}
