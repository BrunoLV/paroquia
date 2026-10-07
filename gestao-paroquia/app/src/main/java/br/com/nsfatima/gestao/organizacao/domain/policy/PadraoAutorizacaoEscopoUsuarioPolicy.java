package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de escopo de usuário sem frameworks ou I/O.
 */
public final class PadraoAutorizacaoEscopoUsuarioPolicy implements AutorizacaoEscopoUsuarioPolicy {

    @Override
    public boolean canManageOrganizationData(
            String userRole,
            TipoOrganizacao userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            UUID targetCommunityId) {
        if (userRole == null || userRole.isBlank()) {
            return false;
        }

        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(userRole);
        if (papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO
                || papelGlobal == PapelGlobal.VIGARIO || papelGlobal == PapelGlobal.SECRETARIA_PAROQUIAL) {
            return true;
        }

        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(userRole);
        if (papelOrg == PapelOrganizacional.COORDENADOR || papelOrg == PapelOrganizacional.SECRETARIO_GRUPO) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        if (papelOrg == PapelOrganizacional.SECRETARIO_CONSELHO) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        return false;
    }

    @Override
    public boolean canApproveEvents(
            String userRole,
            TipoOrganizacao userOrgType,
            UUID userOrgId,
            UUID targetOrgId) {
        if (userRole == null || userRole.isBlank()) {
            return false;
        }

        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(userRole);
        return papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO;
    }

    @Override
    public boolean canAddObservation(
            String userRole,
            TipoOrganizacao userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            boolean isParticipant) {
        if (userRole == null || userRole.isBlank()) {
            return false;
        }

        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(userRole);
        if (papelOrg == PapelOrganizacional.VOLUNTARIO || papelOrg == PapelOrganizacional.MEMBRO_ATIVO) {
            return Objects.equals(userOrgId, targetOrgId) || isParticipant;
        }

        return true;
    }

    @Override
    public boolean canViewPendenteEvents(String userRole) {
        if (userRole == null || userRole.isBlank()) {
            return false;
        }
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(userRole);
        if (papelGlobal == PapelGlobal.FIEL) {
            return false;
        }
        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(userRole);
        return papelOrg != PapelOrganizacional.VOLUNTARIO && papelOrg != PapelOrganizacional.MEMBRO_ATIVO;
    }

    @Override
    public boolean hasTerritorialAccess(
            String userRole,
            UUID userCommunityId,
            UUID targetCommunityId) {
        if (userRole == null || userRole.isBlank()) {
            return false;
        }

        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(userRole);
        if (papelGlobal == PapelGlobal.ADMIN || papelGlobal == PapelGlobal.PAROCO
                || papelGlobal == PapelGlobal.VIGARIO || papelGlobal == PapelGlobal.BISPO_DIOCESANO
                || papelGlobal == PapelGlobal.SECRETARIA_PAROQUIAL) {
            return true;
        }

        if (userCommunityId == null || targetCommunityId == null) {
            return true;
        }

        return Objects.equals(userCommunityId, targetCommunityId);
    }
}
