package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class EscopoUsuarioAuthorizationService {

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

        // 1. ADMIN, PAROCO e VIGARIO têm permissões plenas de gestão geral
        if (userRole == PapelOrganizacional.ADMIN || userRole == PapelOrganizacional.PAROCO
                || userRole == PapelOrganizacional.VIGARIO) {
            return true;
        }

        // 2. Coordenador Pastoral Global: responsável por essa pastoral em todas as comunidades
        if (userRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        // 3. Coordenador Pastoral Local: administrador local restrito à sua pastoral exata
        if (userRole == PapelOrganizacional.COORD_PASTORAL_LOCAL) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        // 4. Coordenador de Movimento: gestão de afinidade carismática
        if (userRole == PapelOrganizacional.COORD_MOVIMENTO) {
            return Objects.equals(userOrgId, targetOrgId);
        }

        // 5. Council Leadership (CPC / CPP / CAE)
        if (userRole == PapelOrganizacional.PRESIDENTE_CPC
                || userRole == PapelOrganizacional.MESA_CPP
                || userRole == PapelOrganizacional.MEMBRO_CPP) {
            return Objects.equals(userOrgId, targetOrgId) || Objects.equals(userOrgId, targetCommunityId);
        }

        // Compatibilidade legada para transição
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

        // Papéis de membro exigem status de participante ou organização correspondente
        if (userRole == PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                || userRole == PapelOrganizacional.MEMBRO_PASTORAL
                || userRole == PapelOrganizacional.MEMBRO_MOVIMENTO) {
            return Objects.equals(userOrgId, targetOrgId) || isParticipant;
        }

        return true;
    }

    public boolean canViewPendenteEvents(PapelOrganizacional userRole) {
        if (userRole == null) {
            return false;
        }
        return userRole != PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                && userRole != PapelOrganizacional.MEMBRO_PASTORAL
                && userRole != PapelOrganizacional.MEMBRO_MOVIMENTO;
    }

    /**
     * Valida se o papel e o contexto de comunidade do usuário permitem interação com targetCommunityId.
     * - Papéis globais (ADMIN, PAROCO, VIGARIO, BISPO_DIOCESANO, COORD_PASTORAL_GLOBAL, COORD_MOVIMENTO) têm
     *   acesso entre comunidades.
     * - Papéis locais (COORD_PASTORAL_LOCAL, VOLUNTARIO_PASTORAL_LOCAL, PRESIDENTE_CPC) exigem
     *   userCommunityId == targetCommunityId.
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
