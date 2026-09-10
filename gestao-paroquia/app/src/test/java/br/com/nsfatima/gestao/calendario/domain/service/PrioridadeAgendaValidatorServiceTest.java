package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException;
import br.com.nsfatima.gestao.calendario.domain.policy.HierarquiaPrioridadeAgendaPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

class ValidadorPrioridadeAgendaTest {

    private EventoJpaRepository eventoJpaRepository;
    private ValidadorPrioridadeAgenda validatorService;

    @BeforeEach
    void setUp() {
        eventoJpaRepository = mock(EventoJpaRepository.class);
        validatorService = new ValidadorPrioridadeAgenda(eventoJpaRepository, new HierarquiaPrioridadeAgendaPolicy());
    }

    @Test
    @DisplayName("Nível 4 (MOVIMENTO) deve ser rejeitado se houver conflito com Nível 2 (LITURGICO)")
    void nivel4_DeveRejeitarConflitoComNivel2() {
        Instant inicio = Instant.now();
        Instant fim = inicio.plusSeconds(3600);
        UUID localId = UUID.randomUUID();

        EventoEntity eventoExistente = new EventoEntity();
        eventoExistente.setId(UUID.randomUUID());
        eventoExistente.setCategoria(CategoriaEvento.LITURGICO);
        eventoExistente.setLocalId(localId);
        eventoExistente.setInicioUtc(inicio);
        eventoExistente.setFimUtc(fim);
        eventoExistente.setStatus("CONFIRMADO");

        when(eventoJpaRepository.findOverlappingEvents(any(), any(), any()))
                .thenReturn(List.of(eventoExistente));

        assertThrows(ConflitoAgendaEclesialException.class, () ->
                validatorService.validarPrioridadeEConflitos(
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
                validatorService.validarPrioridadeEConflitos(
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

    @Test
    @DisplayName("Nível 1 (SOLENIDADE) deve retornar e auto-cancelar eventos de menor prioridade (Níveis 2, 3 e 4)")
    void nivel1_DeveAutoCancelarEventosDeMenorPrioridade() {
        Instant inicio = Instant.now();
        Instant fim = inicio.plusSeconds(3600);

        EventoEntity eventoNivel3 = new EventoEntity();
        eventoNivel3.setId(UUID.randomUUID());
        eventoNivel3.setCategoria(CategoriaEvento.PASTORAL);
        eventoNivel3.setStatus("CONFIRMADO");

        when(eventoJpaRepository.findOverlappingEvents(any(), any(), any()))
                .thenReturn(List.of(eventoNivel3));

        List<EventoEntity> cancelados = validatorService.validarPrioridadeEConflitos(
                CategoriaEvento.SOLENIDADE, inicio, fim, null, null, null, false
        );

        assertEquals(1, cancelados.size());

        validatorService.processarCancelamentosAutomaticos(cancelados, "Cancelamento automático por Solenidade Global");
        assertEquals("CANCELADO", eventoNivel3.getStatus());
        assertEquals("Cancelamento automático por Solenidade Global", eventoNivel3.getCanceladoMotivo());
        verify(eventoJpaRepository).saveAll(cancelados);
    }
}
