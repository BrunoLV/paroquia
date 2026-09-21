package br.com.nsfatima.gestao.reserva.domain.service;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ReservaConflitoServiceTest {

    private final ReservaRepository repository = Mockito.mock(ReservaRepository.class);
    private final ReservaConflitoService service = new ReservaConflitoService(repository);

    @Test
    @DisplayName("Não deve lançar exceção quando não há sobreposição de reservas")
    void naoDeveLancarExcecaoSemSobreposicao() {
        UUID itemId = UUID.randomUUID();
        Instant agora = Instant.now();
        IntervaloReserva intervalo = new IntervaloReserva(
                agora.plus(2, ChronoUnit.HOURS), agora.plus(4, ChronoUnit.HOURS), 15);

        when(repository.findConflitosPotenciaisComLock(eq(TipoItemReserva.LOCAL), eq(itemId), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertThatNoException().isThrownBy(() -> service.validarDisponibilidadeComLock(
                TipoItemReserva.LOCAL, itemId, "Sala 1", intervalo, null));
    }

    @Test
    @DisplayName("Deve lançar ReservaConflitoException quando houver reserva no mesmo intervalo considerando buffer")
    void deveLancarExcecaoComSobreposicao() {
        UUID itemId = UUID.randomUUID();
        Instant agora = Instant.now();
        IntervaloReserva intervalo = new IntervaloReserva(
                agora.plus(2, ChronoUnit.HOURS), agora.plus(4, ChronoUnit.HOURS), 15);

        Reserva existente = Reserva.criar(
                ReservaId.gerar(),
                TipoItemReserva.LOCAL,
                itemId,
                new IntervaloReserva(agora.plus(3, ChronoUnit.HOURS), agora.plus(5, ChronoUnit.HOURS), 10),
                "Casamento",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.CONFIRMADA,
                agora
        );

        when(repository.findConflitosPotenciaisComLock(eq(TipoItemReserva.LOCAL), eq(itemId), any(), any(), any()))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.validarDisponibilidadeComLock(
                TipoItemReserva.LOCAL, itemId, "Sala 1", intervalo, null))
                .isInstanceOf(ReservaConflitoException.class)
                .hasMessageContaining("Identificado conflito de agenda ou período de preparação e desocupação para o recurso.");
    }
}
