package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de escopo de usuário sem frameworks ou I/O.
 */
public final class PadraoAutorizacaoEscopoUsuarioPolicy implements AutorizacaoEscopoUsuarioPolicy {

    @Override
    public boolean canManageOrganizationData(
            PapelOrganizacional userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            UUID targetCommunityId) {
        if (userRole == null) {
            return false;
        }

        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO
                || userRole == PapelOrganizacional.VIGARIO) {
            return true;
        }

        if (userRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || userRole == PapelOrganizacional.COORD_PASTORAL_LOCAL
                || userRole == PapelOrganizacional.COORD_MOVIMENTO
                || userRole == PapelOrganizacional.COORD_PASTORAL) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        if (userRole == PapelOrganizacional.PRESIDENTE_CPC
                || userRole == PapelOrganizacional.MESA_CPP
                || userRole == PapelOrganizacional.MEMBRO_CPP) {
            return Objects.equals(userOrgId, targetOrgId) || Objects.equals(userOrgId, targetCommunityId);
        }

        return false;
    }

    @Override
    public boolean canApproveEvents(
            PapelOrganizacional userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId) {
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

        if (userRole == PapelOrganizacional.PRESIDENTE_CPC && userOrgType == TipoGrupo.CONSELHO) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        return false;
    }

    @Override
    public boolean canAddObservation(
            PapelOrganizacional userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            boolean isParticipant) {
        if (userRole == null) {
            return false;
        }

        if (userRole == PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                || userRole == PapelOrganizacional.MEMBRO_PASTORAL
                || userRole == PapelOrganizacional.MEMBRO_MOVIMENTO) {
            return Objects.equals(userOrgId, targetOrgId) || isParticipant;
        }

        return true;
    }

    @Override
    public boolean canViewPendenteEvents(PapelOrganizacional userRole) {
        if (userRole == null) {
            return false;
        }
        return userRole != PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                && userRole != PapelOrganizacional.MEMBRO_PASTORAL
                && userRole != PapelOrganizacional.MEMBRO_MOVIMENTO;
    }

    @Override
    public boolean hasTerritorialAccess(
            PapelOrganizacional userRole,
            UUID userCommunityId,
            UUID targetCommunityId) {
        if (userRole == null) {
            return false;
        }

        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO
                || userRole == PapelOrganizacional.VIGARIO || userRole == PapelOrganizacional.BISPO_DIOCESANO
                || userRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || userRole == PapelOrganizacional.COORD_MOVIMENTO) {
            return true;
        }

        if (userCommunityId == null || targetCommunityId == null) {
            return true;
        }

        return Objects.equals(userCommunityId, targetCommunityId);
    }
}
