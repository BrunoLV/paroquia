package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ListEventosUseCase {

    private final EventoJpaRepository eventoJpaRepository;
    private final EventoMapper eventoMapper;

    public ListEventosUseCase(EventoJpaRepository eventoJpaRepository, EventoMapper eventoMapper) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.eventoMapper = eventoMapper;
    }

    /**
     * Aggregates diverse search criteria to provide a filtered, paginated view of the parish calendar for users and
     * administrative tools.
     * <p>
     * Usage Example:
     * {@code
     * Page<EventoResponse> page = useCase.execute(filters, PageRequest.of(0, 20));
     * }
     */
    public Page<EventoResponse> execute(EventoFiltroRequest filters, Pageable pageable) {
        List<CategoriaEvento> categories = filters.categoria();

        List<String> statuses = Optional.ofNullable(filters.status())
                .map(list -> list.stream().map(Enum::name).toList())
                .orElse(null);

        return eventoJpaRepository.findAllWithFilters(
                        filters.dataInicio(),
                        filters.dataInicio() != null,
                        filters.dataFim(),
                        filters.dataFim() != null,
                        filters.grupoParoquialIds(),
                        filters.grupoParoquialIds() != null && !filters.grupoParoquialIds().isEmpty(),
                        filters.projetoId(),
                        filters.projetoId() != null,
                        filters.envolvidoId(),
                        filters.envolvidoId() != null,
                        categories,
                        categories != null,
                        statuses,
                        statuses != null,
                        pageable)
                .map(eventoMapper::toResponse);
    }
}
