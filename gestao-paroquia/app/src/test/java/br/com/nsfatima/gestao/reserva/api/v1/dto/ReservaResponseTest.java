package br.com.nsfatima.gestao.reserva.api.v1.dto;

import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaResponseTest {

    @Test
    @DisplayName("de(ReservaDetalheDto) retorna null quando dto é nulo")
    void deRetornaNullParaDtoNulo() {
        assertThat(ReservaResponse.de((ReservaDetalheDto) null)).isNull();
    }

    @Test
    @DisplayName("de(Reserva) retorna null quando reserva é nula")
    void deRetornaNullParaReservaNula() {
        assertThat(ReservaResponse.de((Reserva) null)).isNull();
    }

    @Test
    @DisplayName("de(ReservaDetalheDto) copia todos os campos do dto de detalhe")
    void deMapeiaTodosOsCamposDoDto() {
        Instant agora = Instant.now();
        ReservaDetalheDto dto = new ReservaDetalheDto(
                UUID.randomUUID(), TipoItemReserva.LOCAL, UUID.randomUUID(), "Salão",
                agora, agora.plusSeconds(3600), 15, agora.plusSeconds(4500), "desc",
                UUID.randomUUID(), "solicitante", UUID.randomUUID(), "pastoral",
                UUID.randomUUID(), "evento", UUID.randomUUID(), StatusReserva.CONFIRMADA,
                UUID.randomUUID(), "aprovador", agora, "motivo", UUID.randomUUID(), "cancelador",
                "sem avarias", agora, agora);

        ReservaResponse response = ReservaResponse.de(dto);

        assertThat(response.id()).isEqualTo(dto.id());
        assertThat(response.itemNome()).isEqualTo("Salão");
        assertThat(response.solicitanteNome()).isEqualTo("solicitante");
        assertThat(response.pastoralMovimentoNome()).isEqualTo("pastoral");
        assertThat(response.eventoTitulo()).isEqualTo("evento");
        assertThat(response.aprovadorNome()).isEqualTo("aprovador");
        assertThat(response.canceladoPorNome()).isEqualTo("cancelador");
        assertThat(response.checkAvarias()).isEqualTo("sem avarias");
        assertThat(response.status()).isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    @DisplayName("de(Reserva) mapeia direto do agregado deixando nomes derivados nulos")
    void deMapeiaDiretoDoAgregado() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 10);
        Reserva reserva = Reserva.criar(
                ReservaId.generate(), TipoItemReserva.ATIVO, UUID.randomUUID(), intervalo, "Som",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, StatusReserva.PENDENTE, Instant.now());

        ReservaResponse response = ReservaResponse.de(reserva);

        assertThat(response.id()).isEqualTo(reserva.getId().value());
        assertThat(response.itemNome()).isNull();
        assertThat(response.solicitanteNome()).isNull();
        assertThat(response.pastoralMovimentoNome()).isNull();
        assertThat(response.eventoTitulo()).isNull();
        assertThat(response.aprovadorNome()).isNull();
        assertThat(response.canceladoPorNome()).isNull();
        assertThat(response.tipoItem()).isEqualTo(TipoItemReserva.ATIVO);
        assertThat(response.bufferMinutos()).isEqualTo(10);
        assertThat(response.fimComBuffer()).isEqualTo(intervalo.fimComBuffer());
    }
}
