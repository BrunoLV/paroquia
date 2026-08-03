package br.com.nsfatima.gestao.governanca.domain.service;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Verifica se o ator autenticado tem privilégio para operar a trava de governança do Ano Paroquial (abrir, fechar
 * ou concluir um ano). Esta é uma autorização de escopo mais restrito que a autorização geral de eventos: apenas o
 * Pároco/Admin ou os coordenadores do Conselho Pastoral (CPP/CPC) podem alterar o ciclo de vida do ano civil, dado
 * o impacto sobre todo o planejamento anual da Matriz e das Comunidades.
 */
@Service
public class AnoParoquialAuthorizationService {

    /**
     * Valida se o ator atual possui permissão para gerenciar a trava do calendário.
     * Somente o Paroco ou os Coordenadores do Conselho possuem este privilégio.
     * <p>
     * Usage Example:
     * authService.assertCanManage(actorContext);
     */
    public void assertCanManage(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        boolean isParoco = normalizedRole == PapelOrganizacional.PAROCO || normalizedRole == PapelOrganizacional.ADMIN;
        boolean isConselhoCoordinator = (normalizedOrgType == TipoOrganizacao.CONSELHO_PASTORAL
                || normalizedOrgType == TipoOrganizacao.CPP
                || normalizedOrgType == TipoOrganizacao.CPC)
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL);

        if (!isParoco && !isConselhoCoordinator) {
            throw new AccessDeniedException("Apenas o Paroco ou o Coordenador do Conselho podem gerir o Ano Paroquial");
        }
    }
}
