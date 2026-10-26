package br.com.nsfatima.gestao.reserva.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaTest {

    @Test
    @DisplayName("Deve criar reserva com estado PENDENTE e dados corretos")
    void deveCriarReservaComEstadoPendente() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, fim, 15);
        UUID itemId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        Reserva reserva = Reserva.criar(
                ReservaId.gerar(),
                TipoItemReserva.LOCAL,
                itemId,
                intervalo,
                "Ensaio Coral",
                solicitanteId,
                orgId,
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        assertThat(reserva.getId()).isNotNull();
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(reserva.getTipoItem()).isEqualTo(TipoItemReserva.LOCAL);
        assertThat(reserva.getItemId()).isEqualTo(itemId);
        assertThat(reserva.getIntervalo().bufferMinutos()).isEqualTo(15);
        assertThat(reserva.getIntervalo().fimComBuffer()).isEqualTo(fim.plus(15, ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("Deve aprovar reserva pendente com sucesso")
    void deveAprovarReservaPendente() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.gerar(),
                TipoItemReserva.LOCAL,
                UUID.randomUUID(),
                intervalo,
                "Reunião",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        UUID aprovadorId = UUID.randomUUID();
        Instant agora = Instant.now();
        reserva.aprovar(aprovadorId, agora);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(reserva.getAprovadorId()).isEqualTo(aprovadorId);
        assertThat(reserva.getAprovadoEm()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Não deve permitir aprovar reserva já cancelada")
    void naoDeveAprovarReservaCancelada() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.gerar(),
                TipoItemReserva.LOCAL,
                UUID.randomUUID(),
                intervalo,
                "Reunião",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        reserva.cancelar(UUID.randomUUID(), "Mudança de planos dos organizadores", false, Instant.now());

        assertThatThrownBy(() -> reserva.aprovar(UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Apenas reservas com status PENDENTE");
    }

    @Test
    @DisplayName("Deve registrar retirada e devolução para ativo patrimonial")
    void deveRegistrarRetiradaEDevolucaoParaAtivo() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.gerar(),
                TipoItemReserva.ATIVO,
                UUID.randomUUID(),
                intervalo,
                "Som",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        reserva.aprovar(UUID.randomUUID(), Instant.now());
        reserva.registrarRetirada(UUID.randomUUID(), Instant.now());
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.RETIRADO);

        reserva.registrarDevolucao(UUID.randomUUID(), "Cabo P10 com chiado leve", Instant.now());
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
        assertThat(reserva.getCheckAvarias()).isEqualTo("Cabo P10 com chiado leve");
    }

    @Test
    @DisplayName("Não deve permitir registrar retirada para local")
    void naoDevePermitirRetiradaParaLocal() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.gerar(),
                TipoItemReserva.LOCAL,
                UUID.randomUUID(),
                intervalo,
                "Salão",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.CONFIRMADA,
                Instant.now()
        );

        assertThatThrownBy(() -> reserva.registrarRetirada(UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exclusivamente a bens patrimoniais");
    }

    private Reserva reservaPendente(TipoItemReserva tipo) {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        return Reserva.criar(ReservaId.gerar(), tipo, UUID.randomUUID(), intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), null, null, StatusReserva.PENDENTE, Instant.now());
    }

    @Test
    @DisplayName("Deve rejeitar reserva pendente com motivo válido")
    void deveRejeitarReservaPendente() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        UUID aprovadorId = UUID.randomUUID();

        reserva.rejeitar(aprovadorId, "Sem disponibilidade de espaço físico", Instant.now());

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(reserva.getCanceladoPorId()).isEqualTo(aprovadorId);
        assertThat(reserva.getMotivoCancelamento()).isEqualTo("Sem disponibilidade de espaço físico");
    }

    @Test
    @DisplayName("Não deve rejeitar com motivo em branco")
    void naoDeveRejeitarComMotivoEmBranco() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        assertThatThrownBy(() -> reserva.rejeitar(UUID.randomUUID(), "   ", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Não deve rejeitar reserva que não está PENDENTE")
    void naoDeveRejeitarReservaNaoPendente() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        reserva.aprovar(UUID.randomUUID(), Instant.now());
        assertThatThrownBy(() -> reserva.rejeitar(UUID.randomUUID(), "motivo válido", Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Admin/pároco deve fornecer justificativa mínima de 15 caracteres para cancelar")
    void adminDeveFornecerJustificativaMinima() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        assertThatThrownBy(() -> reserva.cancelar(UUID.randomUUID(), "curto", true, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínima de 15 caracteres");
    }

    @Test
    @DisplayName("Não deve cancelar com motivo em branco")
    void naoDeveCancelarComMotivoEmBranco() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        assertThatThrownBy(() -> reserva.cancelar(UUID.randomUUID(), "   ", false, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Não deve cancelar reserva em estado final (já cancelada)")
    void naoDeveCancelarReservaEmEstadoFinal() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        reserva.cancelar(UUID.randomUUID(), "motivo válido suficiente", false, Instant.now());
        assertThatThrownBy(() -> reserva.cancelar(UUID.randomUUID(), "outro motivo válido", false, Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Não deve registrar devolução para reserva de LOCAL")
    void naoDeveRegistrarDevolucaoParaLocal() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        reserva.aprovar(UUID.randomUUID(), Instant.now());
        assertThatThrownBy(() -> reserva.registrarDevolucao(UUID.randomUUID(), "check", Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exclusivamente a bens patrimoniais");
    }

    @Test
    @DisplayName("Não deve registrar devolução quando reserva não está RETIRADO")
    void naoDeveRegistrarDevolucaoQuandoNaoRetirado() {
        Reserva reserva = reservaPendente(TipoItemReserva.ATIVO);
        reserva.aprovar(UUID.randomUUID(), Instant.now());
        assertThatThrownBy(() -> reserva.registrarDevolucao(UUID.randomUUID(), "check", Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RETIRADO");
    }

    @Test
    @DisplayName("Não deve registrar devolução com checklist em branco")
    void naoDeveRegistrarDevolucaoComChecklistEmBranco() {
        Reserva reserva = reservaPendente(TipoItemReserva.ATIVO);
        reserva.aprovar(UUID.randomUUID(), Instant.now());
        reserva.registrarRetirada(UUID.randomUUID(), Instant.now());
        assertThatThrownBy(() -> reserva.registrarDevolucao(UUID.randomUUID(), "   ", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve finalizar uso direto de LOCAL confirmado")
    void deveFinalizarUsoLocal() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        reserva.aprovar(UUID.randomUUID(), Instant.now());
        reserva.finalizarUsoLocal(Instant.now().plus(5, ChronoUnit.HOURS));
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
    }

    @Test
    @DisplayName("Não deve finalizar uso de ATIVO diretamente")
    void naoDeveFinalizarUsoDeAtivo() {
        Reserva reserva = reservaPendente(TipoItemReserva.ATIVO);
        reserva.aprovar(UUID.randomUUID(), Instant.now());
        assertThatThrownBy(() -> reserva.finalizarUsoLocal(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Não deve finalizar uso de LOCAL que não está confirmado")
    void naoDeveFinalizarLocalNaoConfirmado() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        assertThatThrownBy(() -> reserva.finalizarUsoLocal(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deve expirar reserva pendente e não permitir expirar reserva não pendente")
    void deveExpirarReservaPendente() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        reserva.expirar(Instant.now());
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.EXPIRADA);

        assertThatThrownBy(() -> reserva.expirar(Instant.now())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deve atualizar intervalo e descrição da reserva")
    void deveAtualizarIntervaloEDescricao() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        Instant novoInicio = Instant.now().plus(5, ChronoUnit.HOURS);
        IntervaloReserva novoIntervalo = new IntervaloReserva(novoInicio, novoInicio.plus(1, ChronoUnit.HOURS), 0);

        reserva.atualizarIntervalo(novoIntervalo, Instant.now());
        reserva.atualizarDescricao("Nova descrição", Instant.now());

        assertThat(reserva.getIntervalo()).isEqualTo(novoIntervalo);
        assertThat(reserva.getDescricao()).isEqualTo("Nova descrição");
    }

    @Test
    @DisplayName("Não deve atualizar descrição para valor em branco")
    void naoDeveAtualizarDescricaoEmBranco() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        assertThatThrownBy(() -> reserva.atualizarDescricao("   ", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve marcar soft delete preenchendo deletedAt e updatedAt")
    void deveMarcarSoftDelete() {
        Reserva reserva = reservaPendente(TipoItemReserva.LOCAL);
        Instant agora = Instant.now();
        reserva.softDelete(agora);
        assertThat(reserva.getDeletedAt()).isEqualTo(agora);
        assertThat(reserva.getUpdatedAt()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Não deve criar reserva com descrição vazia")
    void naoDeveCriarComDescricaoVazia() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        assertThatThrownBy(() -> Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, UUID.randomUUID(),
                intervalo, "   ", UUID.randomUUID(), UUID.randomUUID(), null, null, StatusReserva.PENDENTE,
                Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
