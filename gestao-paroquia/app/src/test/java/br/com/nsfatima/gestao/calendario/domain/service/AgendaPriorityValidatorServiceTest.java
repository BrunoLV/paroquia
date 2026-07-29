package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgendaPriorityValidatorServiceTest {

    private EventoJpaRepository eventoJpaRepository;
    private AgendaPriorityValidatorService validatorService;

    @BeforeEach
    void setUp() {
        eventoJpaRepository = mock(EventoJpaRepository.class);
        validatorService = new AgendaPriorityValidatorService(eventoJpaRepository);
    }

    @Test
    @DisplayName("Nível 4 (MOVIMENTO) deve ser rejeitado se houver conflito com Nível 2 (LITURGICO)")
    void nivel4_DeveRejeitarConflitoComNivel2() {
        Instant inicio = Instant.now();
        Instant fim = inicio.plusSeconds(3600);
        UUID localId = UUID.randomUUID();

        EventoEntity eventoExistente = new EventoEntity();
        eventoExistente.setId(UUID.randomUUID());
        eventoExistente.setCategoria(CategoriaEvento.LITURGICO.name());
        eventoExistente.setLocalId(localId);
        eventoExistente.setInicioUtc(inicio);
        eventoExistente.setFimUtc(fim);
        eventoExistente.setStatus("CONFIRMADO");

        when(eventoJpaRepository.findOverlappingEvents(any(), any(), any()))
                .thenReturn(List.of(eventoExistente));

        assertThrows(ConflitoAgendaEclesialException.class, () ->
                validatorService.validatePriorityAndConflicts(
                        CategoriaEvento.MOVIMENTO,
                        inicio,
                        fim,
                        localId,
                        null,
                        null,
                        false
                )
        );
    }

    @Test
    @DisplayName("Nível 3 (PASTORAL) deve passar se não houver conflitos")
    void nivel3_DevePassarSemConflitos() {
        Instant inicio = Instant.now();
        Instant fim = inicio.plusSeconds(3600);

        when(eventoJpaRepository.findOverlappingEvents(any(), any(), any()))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                validatorService.validatePriorityAndConflicts(
                        CategoriaEvento.PASTORAL,
                        inicio,
                        fim,
                        UUID.randomUUID(),
                        null,
                        null,
                        false
                )
        );
    }
}
