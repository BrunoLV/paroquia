package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoIdempotencyEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoIdempotencyJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoIdempotencyServiceTest {

    @Mock
    private EventoIdempotencyJpaRepository idempotencyRepository;
    @Mock
    private EventoJpaRepository eventoRepository;
    @Mock
    private EventoMapper eventoMapper;

    @Test
    void chaveDeEventoExcluidoNaoFazReplayEPermiteCriarDeNovo() {
        EventoIdempotencyService service =
                new EventoIdempotencyService(idempotencyRepository, eventoRepository, eventoMapper);
        CriarEventoRequest request = new CriarEventoRequest("Missa", null, null, UUID.randomUUID(), null, null,
                List.of(), null, Instant.parse("2026-08-02T12:00:00Z"), Instant.parse("2026-08-02T13:00:00Z"),
                null, null, null, false, null, null, false, null, null, null);
        UUID eventoId = UUID.randomUUID();
        EventoResponse criado = mock(EventoResponse.class);
        when(criado.id()).thenReturn(eventoId);
        service.execute("k", request, () -> criado);
        ArgumentCaptor<EventoIdempotencyEntity> gravado = ArgumentCaptor.forClass(EventoIdempotencyEntity.class);
        verify(idempotencyRepository).save(gravado.capture());
        EventoIdempotencyEntity registro = gravado.getValue();
        when(idempotencyRepository.findById("k")).thenReturn(Optional.of(registro));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        assertThat(service.findExisting("k", request)).isEmpty();

        var ordem = inOrder(idempotencyRepository);
        ordem.verify(idempotencyRepository).delete(registro);
        ordem.verify(idempotencyRepository).flush();
    }
}
