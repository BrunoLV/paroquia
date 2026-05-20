package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListRemovedObservacoesForAuditUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public ListRemovedObservacoesForAuditUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    /**
     * Provides access to soft-deleted observations for administrative and auditing purposes, ensuring full visibility into the event's history.
     * 
     * Usage Example:
     * {@code
     * List<ObservacaoResponse> removed = useCase.execute(eventoId);
     * }
     */
    public List<ObservacaoResponse> execute(UUID eventoId) {
        return observacaoEventoJpaRepository.findByEventoIdAndRemovidaTrueOrderByCriadoEmUtcAscIdAsc(eventoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ObservacaoResponse toResponse(ObservacaoEventoEntity entity) {
        return new ObservacaoResponse(
                entity.getId(),
                entity.getEventoId(),
                entity.getUsuarioId(),
                TipoObservacaoResponse.fromStoredValue(
                        entity.getTipo(),
                        legacyEnumInconsistencyPublisher,
                        entity.getEventoId().toString()),
                entity.getConteudo(),
                entity.getCriadoEmUtc());
    }
}
