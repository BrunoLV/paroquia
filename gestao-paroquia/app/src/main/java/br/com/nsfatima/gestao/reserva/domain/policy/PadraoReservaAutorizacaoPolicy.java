package br.com.nsfatima.gestao.reserva.domain.policy;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementação pura da política de autorização e alçada de aprovação de reservas.
 */
public final class PadraoReservaAutorizacaoPolicy implements ReservaAutorizacaoPolicy {

    @Override
    public StatusReserva resolveStatusInicial(UsuarioActor solicitante, UUID pastoralMovimentoId) {
        Objects.requireNonNull(solicitante, "Solicitante é obrigatório.");
        if (solicitante.isAdminOuParoco() || solicitante.isCoordenadorDaOrganizacao(pastoralMovimentoId)) {
            return StatusReserva.CONFIRMADA;
        }
        return StatusReserva.PENDENTE;
    }

    @Override
    public void assertAutorizacaoAprovacao(UsuarioActor operador, Reserva reserva) {
        Objects.requireNonNull(operador, "Operador é obrigatório.");
        Objects.requireNonNull(reserva, "Reserva é obrigatória.");

        if (operador.isAdminOuParoco()) {
            return;
        }

        if (operador.isCoordenadorDaOrganizacao(reserva.getPastoralMovimentoId())) {
            return;
        }

        throw new ReservaNaoAutorizadaException(
                "Apenas administradores, o pároco ou o coordenador da respectiva pastoral podem "
                        + "aprovar/recusar esta reserva.");
    }

    @Override
    public void assertAutorizacaoCancelamento(UsuarioActor operador, Reserva reserva, Instant agora) {
        Objects.requireNonNull(operador, "Operador é obrigatório.");
        Objects.requireNonNull(reserva, "Reserva é obrigatória.");

        if (operador.isAdminOuParoco()) {
            return;
        }

        if (operador.isCoordenadorDaOrganizacao(reserva.getPastoralMovimentoId())) {
            return;
        }

        if (Objects.equals(operador.id(), reserva.getSolicitanteId())) {
            if (reserva.getIntervalo().dataHoraInicio().isBefore(agora)) {
                throw new ReservaNaoAutorizadaException("O solicitante só pode cancelar reservas com data futura.");
            }
            return;
        }

        throw new ReservaNaoAutorizadaException("Você não possui permissão para cancelar esta reserva.");
    }
}
