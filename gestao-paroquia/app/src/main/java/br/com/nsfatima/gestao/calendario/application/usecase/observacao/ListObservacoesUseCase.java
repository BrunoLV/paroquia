package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ListObservacoesUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public ListObservacoesUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    /**
     * Retrieves all active observations for a specific event to support collaborative views and logistical coordination.
     * 
     * Usage Example:
     * {@code
     * List<ObservacaoResponse> all = useCase.execute(eventoId);
     * }
     */
    public List<ObservacaoResponse> execute(UUID eventoId) {
        return observacaoEventoJpaRepository.findByEventoIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(eventoId)
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
