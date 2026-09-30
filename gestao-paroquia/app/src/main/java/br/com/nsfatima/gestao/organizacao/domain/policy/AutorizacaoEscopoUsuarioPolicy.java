package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.UUID;

/**
 * Contrato de política pura para regras de autorização de escopo do usuário.
 */
public interface AutorizacaoEscopoUsuarioPolicy {

    boolean canManageOrganizationData(
            String userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            UUID targetCommunityId);

    boolean canApproveEvents(
            String userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId);

    boolean canAddObservation(
            String userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            boolean isParticipant);

    boolean canViewPendenteEvents(String userRole);

    boolean hasTerritorialAccess(
            String userRole,
            UUID userCommunityId,
            UUID targetCommunityId);
}
