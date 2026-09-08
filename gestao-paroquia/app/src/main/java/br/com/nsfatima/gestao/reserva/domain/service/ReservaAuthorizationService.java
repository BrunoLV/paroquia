package br.com.nsfatima.gestao.reserva.domain.service;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço de domínio responsável pela matriz de permissões e alçadas de aprovação do módulo de Reservas.
 */
public class ReservaAuthorizationService {

    public static final Set<String> ROLES_ADMINISTRATIVAS = Set.of(
            "ROLE_ADMIN", "ADMIN",
            "ROLE_PAROCO", "PAROCO",
            "ROLE_CLERO_PAROCO",
            "ROLE_SECRETARIA_PAROQUIAL", "SECRETARIA_PAROQUIAL"
    );

    public static final Set<String> ROLES_COORDENACAO = Set.of(
            "COORD_PASTORAL", "COORD_PASTORAL_GLOBAL", "COORD_PASTORAL_LOCAL", "COORD_MOVIMENTO"
    );

    public record UsuarioActor(
            UUID id,
            String nome,
            List<String> roles,
            List<UUID> organizacaoIdsOndeECoordenador,
            List<UUID> organizacaoIdsOndeEMembro
    ) {
        public boolean isAdminOuParoco() {
            if (roles == null) return false;
            return roles.stream().anyMatch(ROLES_ADMINISTRATIVAS::contains);
        }

        public boolean isCoordenadorDaOrganizacao(UUID orgId) {
            if (isAdminOuParoco()) return true;
            if (orgId == null || organizacaoIdsOndeECoordenador == null) return false;
            return organizacaoIdsOndeECoordenador.contains(orgId);
        }

        public boolean isMembroDaOrganizacao(UUID orgId) {
            if (isAdminOuParoco()) return true;
            if (orgId == null) return false;
            boolean coord = organizacaoIdsOndeECoordenador != null && organizacaoIdsOndeECoordenador.contains(orgId);
            boolean membro = organizacaoIdsOndeEMembro != null && organizacaoIdsOndeEMembro.contains(orgId);
            return coord || membro;
        }
    }

    /**
     * Determina se a reserva nasce com status CONFIRMADA ou PENDENTE com base no perfil do solicitante.
     */
    public StatusReserva determinarStatusInicial(UsuarioActor solicitante, UUID pastoralMovimentoId) {
        Objects.requireNonNull(solicitante, "Solicitante é obrigatório.");
        if (solicitante.isAdminOuParoco() || solicitante.isCoordenadorDaOrganizacao(pastoralMovimentoId)) {
            return StatusReserva.CONFIRMADA;
        }
        return StatusReserva.PENDENTE;
    }

    /**
     * Valida se o usuário tem autorização para aprovar/rejeitar a solicitação pendente.
     */
    public void validarAutorizacaoAprovacao(UsuarioActor operador, Reserva reserva) {
        Objects.requireNonNull(operador, "Operador é obrigatório.");
        Objects.requireNonNull(reserva, "Reserva é obrigatória.");

        if (operador.isAdminOuParoco()) {
            return;
        }

        if (operador.isCoordenadorDaOrganizacao(reserva.getPastoralMovimentoId())) {
            return;
        }

        throw new ReservaNaoAutorizadaException("Apenas administradores, o pároco ou o coordenador da respectiva pastoral podem aprovar/recusar esta reserva.");
    }

    /**
     * Valida se o usuário pode cancelar a reserva conforme as regras de negócio:
     * - Admin/Pároco: cancela qualquer reserva (com justificativa mínima de 15 caracteres validada no agregado).
     * - Coordenador: cancela reservas vinculadas à sua pastoral.
     * - Fiel/Solicitante: cancela apenas suas próprias reservas que ainda estejam com data futura.
     */
    public void validarAutorizacaoCancelamento(UsuarioActor operador, Reserva reserva, Instant agora) {
        Objects.requireNonNull(operador, "Operador é obrigatório.");
        Objects.requireNonNull(reserva, "Reserva é obrigatória.");

        if (operador.isAdminOuParoco()) {
            return;
        }

        if (operador.isCoordenadorDaOrganizacao(reserva.getPastoralMovimentoId())) {
            return;
        }

        // Se for o próprio solicitante
        if (Objects.equals(operador.id(), reserva.getSolicitanteId())) {
            if (reserva.getIntervalo().dataHoraInicio().isBefore(agora)) {
                throw new ReservaNaoAutorizadaException("O solicitante só pode cancelar reservas com data futura.");
            }
            return;
        }

        throw new ReservaNaoAutorizadaException("Você não possui permissão para cancelar esta reserva.");
    }
}
