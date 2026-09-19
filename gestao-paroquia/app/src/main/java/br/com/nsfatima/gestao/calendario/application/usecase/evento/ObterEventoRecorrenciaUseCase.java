package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaDetalheResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNaoEncontradoException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Recupera a regra de recorrência completa de um evento — a criação (via
 * {@code PUT /eventos/{id}/recorrencia}) grava frequência, dias da semana e limites, mas até aqui
 * não havia forma de reler esses dados: a edição e a ficha de detalhes de um evento recorrente só
 * enxergavam o {@code recorrenciaId}, nunca o conteúdo da regra.
 */
@Service
public class ObterEventoRecorrenciaUseCase {

    private final EventoJpaRepository eventoRepository;
    private final EventoRecorrenciaJpaRepository recorrenciaRepository;

    public ObterEventoRecorrenciaUseCase(
            EventoJpaRepository eventoRepository,
            EventoRecorrenciaJpaRepository recorrenciaRepository) {
        this.eventoRepository = eventoRepository;
        this.recorrenciaRepository = recorrenciaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<EventoRecorrenciaDetalheResponse> execute(UUID eventoId) {
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNaoEncontradoException(eventoId));

        if (evento.getRecorrenciaId() == null) {
            return Optional.empty();
        }

        return recorrenciaRepository.findById(evento.getRecorrenciaId())
                .map(EventoRecorrenciaDetalheResponse::de);
    }
}
