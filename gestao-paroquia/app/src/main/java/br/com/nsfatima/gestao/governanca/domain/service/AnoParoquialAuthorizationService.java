package br.com.nsfatima.gestao.governanca.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AnoParoquialAuthorizationService {

    /**
     * Valida se o ator atual possui permissão para gerenciar a trava do calendário.
     * Somente o Paroco ou os Coordenadores do Conselho possuem este privilégio.
     * 
     * Usage Example:
     * authService.assertCanManage(actorContext);
     */
    public void assertCanManage(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        boolean isParoco = normalizedRole == PapelOrganizacional.PAROCO;
        boolean isConselhoCoordinator = normalizedOrgType == TipoOrganizacao.CONSELHO 
                && (normalizedRole == PapelOrganizacional.COORDENADOR || normalizedRole == PapelOrganizacional.VICE_COORDENADOR);

        if (!isParoco && !isConselhoCoordinator) {
            throw new AccessDeniedException("Apenas o Paroco ou o Coordenador do Conselho podem gerir o Ano Paroquial");
        }
    }
}
