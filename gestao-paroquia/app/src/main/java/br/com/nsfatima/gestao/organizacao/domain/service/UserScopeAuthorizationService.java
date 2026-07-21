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
        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO || userRole == PapelOrganizacional.VIGARIO) {
            return true;
        }

        // 2. Read-only / Member roles cannot manage data directly
        if (userRole == PapelOrganizacional.PADRE || userRole == PapelOrganizacional.SECRETARIO || userRole == PapelOrganizacional.MEMBRO) {
            return false;
        }

        // 3. COORDENADOR & VICE_COORDENADOR
        if (userRole == PapelOrganizacional.COORDENADOR || userRole == PapelOrganizacional.VICE_COORDENADOR) {
            if (userOrgType == TipoOrganizacao.CONSELHO_PASTORAL || userOrgType == TipoOrganizacao.CONSELHO_ECONOMICO) {
                // Conseil pastoral/economico leadership can manage items within their own community/matriz scope
                return Objects.equals(userOrgId, targetOrgId) || Objects.equals(userOrgId, targetCommunityId);
            }
            if (userOrgType == TipoOrganizacao.PASTORAL || userOrgType == TipoOrganizacao.MOVIMENTO) {
                // Pastoral/Movimento leadership can only manage items of their exact pastoral/movimento
                return Objects.equals(userOrgId, targetOrgId);
            }
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

        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO || userRole == PapelOrganizacional.VIGARIO) {
            return true;
        }

        if ((userRole == PapelOrganizacional.COORDENADOR || userRole == PapelOrganizacional.VICE_COORDENADOR)
                && (userOrgType == TipoOrganizacao.CONSELHO_PASTORAL || userOrgType == TipoOrganizacao.CONSELHO_ECONOMICO)) {
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

        // All users except MEMBRO can add observations globally
        if (userRole != PapelOrganizacional.MEMBRO) {
            return true;
        }

        // MEMBRO can add observation if targetOrg is their own pastoral or they are participant
        return Objects.equals(userOrgId, targetOrgId) || isParticipant;
    }

    public boolean canViewPendingEvents(PapelOrganizacional userRole) {
        if (userRole == null) {
            return false;
        }
        return userRole != PapelOrganizacional.MEMBRO;
    }
}
