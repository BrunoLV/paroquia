package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Centraliza a autorização do contexto de patrimônio. Toda operação mutante (cadastrar, editar
 * descrição, enviar/concluir manutenção, excluir) tem a mesma regra: o ator precisa ter um papel
 * de coordenação/liderança e — salvo atores de alto nível (admin, pároco, coordenação global ou de
 * conselho) — atuar no escopo da unidade territorial dona do bem.
 */
@Service
public class PatrimonioAuthorizationService {

    private static final Set<PapelOrganizacional> PATRIMONIO_EDITOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.COORD_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP);

    /**
     * Verifica se o ator pode gerir (cadastrar, editar, enviar para manutenção, concluir, excluir)
     * um ativo pertencente à unidade territorial informada.
     *
     * @throws ForbiddenOperationException se o papel não for elegível ou faltar escopo sobre a unidade
     */
    public void assertPodeGerirPatrimonio(EventoActorContext actorContext, UUID grupoParoquialId) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(actorContext.role());

        if (!PATRIMONIO_EDITOR_ROLES.contains(papel)) {
            throw new ForbiddenOperationException("Usuário não tem permissão para gerir ativos patrimoniais");
        }

        if (!isHighLevelActor(actorContext)
                && actorContext.organizationId() != null
                && !actorContext.organizationId().equals(grupoParoquialId)) {
            throw new ForbiddenOperationException("Usuário não pode gerir ativos de outra unidade territorial");
        }
    }

    /**
     * Indica se o ator ocupa um papel de alto nível, que dispensa a restrição de escopo por unidade
     * territorial nas operações de patrimônio.
     */
    public boolean isHighLevelActor(EventoActorContext actorContext) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(actorContext.role());
        return papel == PapelOrganizacional.ADMIN
                || papel == PapelOrganizacional.PAROCO
                || papel == PapelOrganizacional.PRESIDENTE_CPC
                || papel == PapelOrganizacional.MESA_CPP
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL;
    }
}
