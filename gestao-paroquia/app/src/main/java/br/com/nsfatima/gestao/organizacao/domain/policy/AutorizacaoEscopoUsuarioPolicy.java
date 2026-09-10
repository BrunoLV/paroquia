package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.UUID;

/**
 * Contrato de política pura para regras de autorização de escopo do usuário.
 */
public interface AutorizacaoEscopoUsuarioPolicy {

    boolean canManageOrganizationData(
            PapelOrganizacional userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            UUID targetCommunityId);

    boolean canApproveEvents(
            PapelOrganizacional userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId);

    boolean canAddObservation(
            PapelOrganizacional userRole,
            TipoGrupo userOrgType,
            UUID userOrgId,
            UUID targetOrgId,
            boolean isParticipant);

    boolean canViewPendenteEvents(PapelOrganizacional userRole);

    boolean hasTerritorialAccess(
            PapelOrganizacional userRole,
            UUID userCommunityId,
            UUID targetCommunityId);
}
