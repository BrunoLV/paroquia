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
public class ListMinhasObservacoesUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public ListMinhasObservacoesUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    /**
     * Filters observations to show only those created by the current user, facilitating personal management of notes and technical remarks.
     * 
     * Usage Example:
     * {@code
     * List<ObservacaoResponse> list = useCase.execute(eventoId, usuarioId);
     * }
     */
    public List<ObservacaoResponse> execute(UUID eventoId, UUID usuarioId) {
        return observacaoEventoJpaRepository
                .findByEventoIdAndUsuarioIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(eventoId, usuarioId)
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
