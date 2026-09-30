package br.com.nsfatima.gestao.reserva.domain.service;

import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.policy.ReservaAutorizacaoPolicy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço de domínio responsável por orquestrar a matriz de permissões e alçadas de aprovação do
 * módulo de Reservas: monta o {@link UsuarioActor} e delega a decisão à {@link ReservaAutorizacaoPolicy}.
 */
public class ReservaAuthorizationService {

    public static final Set<String> ROLES_ADMINISTRATIVAS = Set.of(
            "ROLE_ADMIN", "ROLE_GLOBAL_ADMIN", "ADMIN",
            "ROLE_PAROCO", "ROLE_GLOBAL_PAROCO", "PAROCO",
            "ROLE_CLERO_PAROCO",
            "ROLE_SECRETARIA_PAROQUIAL", "ROLE_GLOBAL_SECRETARIA_PAROQUIAL", "SECRETARIA_PAROQUIAL"
    );

    public static final Set<String> ROLES_COORDENACAO = Set.of(
            "COORDENADOR", "coordenador"
    );

    private final ReservaAutorizacaoPolicy policy;

    public ReservaAuthorizationService(ReservaAutorizacaoPolicy policy) {
        this.policy = policy;
    }

    public record UsuarioActor(
            UUID id,
            String nome,
            List<String> roles,
            List<UUID> organizacaoIdsOndeECoordenador,
            List<UUID> organizacaoIdsOndeEMembro
    ) {
        public boolean isAdminOuParoco() {
            if (roles == null) {
                return false;
            }
            return roles.stream().anyMatch(ROLES_ADMINISTRATIVAS::contains);
        }

        public boolean isCoordenadorDaOrganizacao(UUID orgId) {
            if (isAdminOuParoco()) {
                return true;
            }
            if (orgId == null || organizacaoIdsOndeECoordenador == null) {
                return false;
            }
            return organizacaoIdsOndeECoordenador.contains(orgId);
        }

        public boolean isMembroDaOrganizacao(UUID orgId) {
            if (isAdminOuParoco()) {
                return true;
            }
            if (orgId == null) {
                return false;
            }
            boolean coord = organizacaoIdsOndeECoordenador != null && organizacaoIdsOndeECoordenador.contains(orgId);
            boolean membro = organizacaoIdsOndeEMembro != null && organizacaoIdsOndeEMembro.contains(orgId);
            return coord || membro;
        }
    }

    /**
     * Determina se a reserva nasce com status CONFIRMADA ou PENDENTE com base no perfil do solicitante.
     */
    public StatusReserva determinarStatusInicial(UsuarioActor solicitante, UUID pastoralMovimentoId) {
        return policy.resolveStatusInicial(solicitante, pastoralMovimentoId);
    }

    /**
     * Valida se o usuário tem autorização para aprovar/rejeitar a solicitação pendente.
     */
    public void validarAutorizacaoAprovacao(UsuarioActor operador, Reserva reserva) {
        policy.assertAutorizacaoAprovacao(operador, reserva);
    }

    /**
     * Valida se o usuário pode cancelar a reserva conforme as regras de negócio:
     * - Admin/Pároco: cancela qualquer reserva (com justificativa mínima de 15 caracteres validada no agregado).
     * - Coordenador: cancela reservas vinculadas à sua pastoral.
     * - Fiel/Solicitante: cancela apenas suas próprias reservas que ainda estejam com data futura.
     */
    public void validarAutorizacaoCancelamento(UsuarioActor operador, Reserva reserva, Instant agora) {
        policy.assertAutorizacaoCancelamento(operador, reserva, agora);
    }
}
