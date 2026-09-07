package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidosResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

/**
 * Remove todos os vínculos de corresponsabilidade pastoral de um evento da paróquia.
 */
@Service
public class ClearEventoEnvolvidosUseCase {

    private final EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    public ClearEventoEnvolvidosUseCase(EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository) {
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
    }

    /**
     * Resets the collaboration list for an event, removing all pastoral links when the event's logistical
     * requirements are completely reset.
     * <p>
     * Usage Example:
     * {@code
     * useCase.execute(eventoId);
     * }
     */
    @Transactional
    public EventoEnvolvidosResponse execute(UUID eventoId) {
        eventoEnvolvidoJpaRepository.deleteByEventoId(eventoId);
        return new EventoEnvolvidosResponse(eventoId, Collections.emptyList());
    }
}
