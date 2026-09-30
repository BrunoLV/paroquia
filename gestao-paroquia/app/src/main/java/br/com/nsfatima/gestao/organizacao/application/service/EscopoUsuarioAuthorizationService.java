package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.policy.AutorizacaoEscopoUsuarioPolicy;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de aplicação que orquestra a autorização de escopo de usuário delegando
 * para a política pura {@link AutorizacaoEscopoUsuarioPolicy}.
 */
@Service
public class EscopoUsuarioAuthorizationService {

    private final AutorizacaoEscopoUsuarioPolicy policy;

    public EscopoUsuarioAuthorizationService(AutorizacaoEscopoUsuarioPolicy policy) {
        this.policy = policy;
    }

    public boolean canManageOrganizationData(
            String userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            UUID targetCommunityId) {
        return policy.canManageOrganizationData(userRole, userOrgType, userOrgId, targetOrgId, targetCommunityId);
    }

    public boolean canApproveEvents(
            String userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId) {
        return policy.canApproveEvents(userRole, userOrgType, userOrgId, targetOrgId);
    }

    public boolean canAddObservation(
            String userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            boolean isParticipant) {
        return policy.canAddObservation(userRole, userOrgType, userOrgId, targetOrgId, isParticipant);
    }

    public boolean canViewPendenteEvents(String userRole) {
        return policy.canViewPendenteEvents(userRole);
    }

    public boolean hasTerritorialAccess(
            String userRole,
            UUID userCommunityId,
            UUID targetCommunityId) {
        return policy.hasTerritorialAccess(userRole, userCommunityId, targetCommunityId);
    }
}
