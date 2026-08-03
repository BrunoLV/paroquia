package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * Centraliza as regras de autorização baseadas em papel organizacional ({@link PapelOrganizacional}) e escopo
 * (organização/comunidade) da paróquia. Diferente de checagens de posse simples, aqui a permissão depende da
 * combinação papel + tipo de organização + se o escopo do usuário coincide com o escopo do recurso-alvo —
 * papéis globais (ADMIN, PAROCO, VIGARIO) atuam em toda a paróquia, enquanto papéis locais (coordenadores
 * locais, voluntários, presidências de conselho) só atuam dentro da própria organização/comunidade.
 */
@Service
public class EscopoUsuarioAuthorizationService {

    /**
     * Verifica se o usuário pode gerenciar os dados (editar, vincular membros) da organização-alvo.
     * <ul>
     *   <li>ADMIN, PAROCO e VIGARIO: acesso pleno, independente do escopo.</li>
     *   <li>Coordenador Pastoral Global/Local e Coordenador de Movimento: apenas se a organização-alvo for
     *       exatamente a organização à qual o usuário está vinculado.</li>
     *   <li>Liderança de conselho (Presidente CPC, Mesa/Membro CPP): organização-alvo igual à do usuário ou
     *       igual à comunidade-alvo.</li>
     *   <li>{@code COORD_PASTORAL} (papel legado): mesma regra do coordenador local, mantida por
     *       compatibilidade durante a transição de nomenclatura.</li>
     * </ul>
     *
     * @param userRole papel organizacional do usuário; {@code null} nunca autoriza
     * @param userOrgType tipo de organização à qual o usuário está vinculado
     * @param userOrgId identificador da organização à qual o usuário está vinculado
     * @param targetOrgId identificador da organização que se deseja gerenciar
     * @param targetCommunityId identificador da comunidade da organização-alvo
     * @return {@code true} se o usuário puder gerenciar os dados da organização-alvo
     */
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

    /**
     * Verifica se o usuário pode aprovar eventos submetidos no contexto da organização-alvo.
     * ADMIN, PAROCO e VIGARIO aprovam qualquer evento; Coordenador Pastoral Global e Mesa do CPP aprovam
     * eventos da própria organização; o Presidente do CPC só aprova quando sua organização-alvo for do tipo
     * {@code CONSELHO_PASTORAL} ou {@code CPC} e coincidir com a organização do usuário. Qualquer outro papel
     * não tem permissão de aprovação.
     *
     * @param userRole papel organizacional do usuário; {@code null} nunca autoriza
     * @param userOrgType tipo de organização à qual o usuário está vinculado
     * @param userOrgId identificador da organização à qual o usuário está vinculado
     * @param targetOrgId identificador da organização dona do evento a aprovar
     * @return {@code true} se o usuário puder aprovar eventos da organização-alvo
     */
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

    /**
     * Verifica se o usuário pode adicionar uma observação/anotação no contexto da organização-alvo. Papéis de
     * membro simples (voluntário pastoral local, membro de pastoral, membro de movimento) só podem fazê-lo se
     * pertencerem à própria organização-alvo ou já forem participantes reconhecidos do item observado; todos
     * os demais papéis têm permissão irrestrita, já que observações não alteram dados sensíveis.
     *
     * @param userRole papel organizacional do usuário; {@code null} nunca autoriza
     * @param userOrgType tipo de organização à qual o usuário está vinculado
     * @param userOrgId identificador da organização à qual o usuário está vinculado
     * @param targetOrgId identificador da organização-alvo da observação
     * @param isParticipant se o usuário já é participante reconhecido do item observado
     * @return {@code true} se o usuário puder adicionar a observação
     */
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

    /**
     * Verifica se o papel do usuário permite visualizar eventos ainda pendentes de aprovação — restrito a
     * papéis com alguma responsabilidade de gestão; voluntários e membros simples de pastoral/movimento não
     * enxergam a fila de pendências.
     *
     * @param userRole papel organizacional do usuário; {@code null} nunca autoriza
     * @return {@code true} se o papel puder visualizar eventos pendentes
     */
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
