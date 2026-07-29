package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserScopeAuthorizationService {

    public boolean canManageOrganizationData(
            PapelOrganizacional userRole,
            TipoOrganizacao userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            UUID targetCommunityId
    ) {
        if (userRole == null) {
            return false;
        }

        // 1. ADMIN, PAROCO and VIGARIO have full general management permissions
        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO
                || userRole == PapelOrganizacional.VIGARIO) {
            return true;
        }

        // 2. Global Pastoral Coordinator: Master of that pastoral across all communities
        if (userRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        // 3. Local Pastoral Coordinator: Local admin restricted to their exact pastoral
        if (userRole == PapelOrganizacional.COORD_PASTORAL_LOCAL) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        // 4. Movement Coordinator: Charismatic affinity management
        if (userRole == PapelOrganizacional.COORD_MOVIMENTO) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        // 5. Council Leadership (CPC / CPP / CAE)
        if (userRole == PapelOrganizacional.PRESIDENTE_CPC
                || userRole == PapelOrganizacional.MESA_CPP
                || userRole == PapelOrganizacional.MEMBRO_CPP) {
            return Objects.equals(userOrgId, targetOrgId) || Objects.equals(userOrgId, targetCommunityId);
        }

        // Legacy compatibility for transition
        if (userRole == PapelOrganizacional.COORD_PASTORAL) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        return false;
    }

    public boolean canApproveEvents(
            PapelOrganizacional userRole,
            TipoOrganizacao userOrgType,
            UUID userOrgId,
            UUID targetOrgId
    ) {
        if (userRole == null) {
            return false;
        }

        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO
                || userRole == PapelOrganizacional.VIGARIO) {
            return true;
        }

        if (userRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || userRole == PapelOrganizacional.MESA_CPP) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        if (userRole == PapelOrganizacional.PRESIDENTE_CPC
                && (userOrgType == TipoOrganizacao.CONSELHO_PASTORAL
                || userOrgType == TipoOrganizacao.CPC)) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        return false;
    }

    public boolean canAddObservation(
            PapelOrganizacional userRole,
            TipoOrganizacao userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            boolean isParticipant
    ) {
        if (userRole == null) {
            return false;
        }

        // Member roles need participant status or matching org
        if (userRole == PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                || userRole == PapelOrganizacional.MEMBRO_PASTORAL
                || userRole == PapelOrganizacional.MEMBRO_MOVIMENTO) {
            return Objects.equals(userOrgId, targetOrgId) || isParticipant;
        }

        return true;
    }

    public boolean canViewPendingEvents(PapelOrganizacional userRole) {
        if (userRole == null) {
            return false;
        }
        return userRole != PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                && userRole != PapelOrganizacional.MEMBRO_PASTORAL
                && userRole != PapelOrganizacional.MEMBRO_MOVIMENTO;
    }

    /**
     * Validates whether the given user role and community context allow interaction with targetCommunityId.
     * - Global roles (ADMIN, PAROCO, VIGARIO, BISPO_DIOCESANO, COORD_PASTORAL_GLOBAL, COORD_MOVIMENTO) have cross-community access.
     * - Local roles (COORD_PASTORAL_LOCAL, VOLUNTARIO_PASTORAL_LOCAL, PRESIDENTE_CPC) require userCommunityId == targetCommunityId.
     */
    public boolean hasTerritorialAccess(
            PapelOrganizacional userRole,
            UUID userCommunityId,
            UUID targetCommunityId
    ) {
        if (userRole == null) {
            return false;
        }

        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO
                || userRole == PapelOrganizacional.VIGARIO || userRole == PapelOrganizacional.BISPO_DIOCESANO
                || userRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL || userRole == PapelOrganizacional.COORD_MOVIMENTO) {
            return true;
        }

        if (userCommunityId == null || targetCommunityId == null) {
            return true;
        }

        return Objects.equals(userCommunityId, targetCommunityId);
    }
}
