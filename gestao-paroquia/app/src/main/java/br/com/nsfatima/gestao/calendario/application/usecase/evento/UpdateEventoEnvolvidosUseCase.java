package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidoInput;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidosResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEventoEnvolvidosUseCase {

    private final EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    public UpdateEventoEnvolvidosUseCase(EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository) {
        this.eventoEnvolvidoJpaRepository = eventoEnvolvidoJpaRepository;
    }

    /**
     * Synchronizes the list of organizations collaborating on an event, ensuring that the network of participants is always current and accurately reflects logistical plans.
     * 
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, List.of(new EventoEnvolvidoInput(orgId, PapelEnvolvido.APOIO)));
     * }
     */
    @Transactional
    public EventoEnvolvidosResponse execute(UUID eventoId, List<EventoEnvolvidoInput> envolvidos) {
        eventoEnvolvidoJpaRepository.deleteByEventoId(eventoId);

        if (envolvidos != null && !envolvidos.isEmpty()) {
            List<EventoEnvolvidoEntity> entities = envolvidos.stream()
                    .map(input -> {
                        EventoEnvolvidoEntity entity = new EventoEnvolvidoEntity();
                        entity.setEventoId(eventoId);
                        entity.setOrganizacaoId(input.organizacaoId());
                        entity.setPapelParticipacao(input.papel());
                        return entity;
                    })
                    .toList();
            eventoEnvolvidoJpaRepository.saveAll(entities);
        }

        List<EventoEnvolvidosResponse.EventoEnvolvidoOutput> persisted = eventoEnvolvidoJpaRepository.findByEventoId(eventoId)
                .stream()
                .map(entity -> new EventoEnvolvidosResponse.EventoEnvolvidoOutput(
                        entity.getOrganizacaoId(),
                        entity.getPapelParticipacao()))
                .toList();

        return new EventoEnvolvidosResponse(eventoId, persisted);
    }
}
