package br.com.nsfatima.gestao.calendario.domain.policy;

import java.util.List;
import java.util.UUID;

/** Decide conflitos e cancelamentos da hierarquia eclesial sem consultar ou alterar persistência. */
@FunctionalInterface
public interface PrioridadeAgendaPolicy {

    List<EventoSobreposto> resolver(EventoSobreposto novoEvento, List<EventoSobreposto> existentes);

    /** Fato mínimo de agenda usado pela regra, sem expor entidade de persistência. */
    record EventoSobreposto(UUID id, int prioridade, UUID localId, UUID igrejaId) {
    }
}
