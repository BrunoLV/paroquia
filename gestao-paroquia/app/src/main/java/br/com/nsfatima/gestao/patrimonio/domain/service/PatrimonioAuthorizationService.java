package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Centraliza as regras de autorização do contexto de patrimônio: apenas papéis organizacionais de
 * coordenação/liderança podem criar, transferir, alocar, desalocar ou colocar ativos em
 * manutenção, e — salvo para atores de alto nível (admin, pároco, coordenação global/conselho) —
 * a ação fica restrita à organização dona do ativo.
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
     * Verifica se o ator pode cadastrar um ativo para a organização informada.
     *
     * @param actorContext  contexto do ator autenticado
     * @param grupoParoquialId organização dona do ativo a criar
     * @throws ForbiddenOperationException se o papel do ator não for elegível ou o ator não tiver
     *     escopo sobre a organização
     */
    public void assertCanCreate(EventoActorContext actorContext, UUID grupoParoquialId) {
        assertScoped(actorContext, grupoParoquialId, "Usuário não tem permissão para criar ativos",
                "Usuário não pode criar ativos para outra organização");
    }

    /**
     * Verifica se o ator pode transferir a posse de um ativo pertencente à organização informada.
     *
     * @param actorContext        contexto do ator autenticado
     * @param ativoGrupoParoquialId organização dona do ativo a transferir
     * @throws ForbiddenOperationException se o papel do ator não for elegível ou o ator não tiver
     *     escopo sobre a organização
     */
    public void assertCanTransfer(EventoActorContext actorContext, UUID ativoGrupoParoquialId) {
        assertScoped(actorContext, ativoGrupoParoquialId, "Usuário não tem permissão para transferir ativos",
                "Usuário não pode transferir ativos de outra organização");
    }

    /**
     * Verifica se o ator pode alocar um ativo pertencente à organização informada.
     *
     * @param actorContext        contexto do ator autenticado
     * @param ativoGrupoParoquialId organização dona do ativo a alocar
     * @throws ForbiddenOperationException se o papel do ator não for elegível ou o ator não tiver
     *     escopo sobre a organização
     */
    public void assertCanAlocar(EventoActorContext actorContext, UUID ativoGrupoParoquialId) {
        assertScoped(actorContext, ativoGrupoParoquialId, "Usuário não tem permissão para alocar ativos",
                "Usuário não pode alocar ativos de outra organização");
    }

    /**
     * Verifica se o ator pode desalocar um ativo pertencente à organização informada.
     *
     * @param actorContext        contexto do ator autenticado
     * @param ativoGrupoParoquialId organização dona do ativo a desalocar
     * @throws ForbiddenOperationException se o papel do ator não for elegível ou o ator não tiver
     *     escopo sobre a organização
     */
    public void assertCanDesalocar(EventoActorContext actorContext, UUID ativoGrupoParoquialId) {
        assertScoped(actorContext, ativoGrupoParoquialId, "Usuário não tem permissão para desalocar ativos",
                "Usuário não pode desalocar ativos de outra organização");
    }

    /**
     * Verifica se o ator pode colocar em manutenção um ativo pertencente à organização informada.
     *
     * @param actorContext        contexto do ator autenticado
     * @param ativoGrupoParoquialId organização dona do ativo
     * @throws ForbiddenOperationException se o papel do ator não for elegível ou o ator não tiver
     *     escopo sobre a organização
     */
    public void assertCanIniciarManutencao(EventoActorContext actorContext, UUID ativoGrupoParoquialId) {
        assertScoped(actorContext, ativoGrupoParoquialId,
                "Usuário não tem permissão para colocar ativos em manutenção",
                "Usuário não pode colocar ativos de outra organização em manutenção");
    }

    private void assertScoped(EventoActorContext actorContext, UUID grupoParoquialId, String roleMessage,
                               String scopeMessage) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());

        if (!PATRIMONIO_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException(roleMessage);
        }

        if (!isHighLevelActor(actorContext) && actorContext.organizationId() != null
                && !actorContext.organizationId().equals(grupoParoquialId)) {
            throw new ForbiddenOperationException(scopeMessage);
        }
    }

    /**
     * Indica se o ator ocupa um papel de alto nível (admin, pároco, coordenação global ou de
     * conselho), que dispensa a restrição de escopo por organização nas operações de patrimônio.
     *
     * @param actorContext contexto do ator autenticado
     * @return {@code true} se o ator for admin, pároco, coordenador pastoral global, presidente
     *     do CPC ou membro da mesa do CPP
     */
    public boolean isHighLevelActor(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        return normalizedRole == PapelOrganizacional.ADMIN
                || normalizedRole == PapelOrganizacional.PAROCO
                || normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL;
    }
}
