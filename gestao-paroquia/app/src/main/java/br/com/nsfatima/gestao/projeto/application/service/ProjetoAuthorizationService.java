package br.com.nsfatima.gestao.projeto.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.projeto.domain.policy.AutorizacaoProjetoPolicy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de aplicação que orquestra a autorização de projetos, adaptando o contexto de segurança
 * {@link EventoActorContext} para a política de domínio pura {@link AutorizacaoProjetoPolicy}.
 */
@Service
public class ProjetoAuthorizationService {

    private final AutorizacaoProjetoPolicy policy;

    public ProjetoAuthorizationService(AutorizacaoProjetoPolicy policy) {
        this.policy = policy;
    }

    /**
     * Valida se o ator pode criar um projeto para o grupo informado.
     *
     * @param actorContext contexto do ator autenticado
     * @param grupoParoquialResponsavelId organização alvo
     */
    public void assertCanCreate(EventoActorContext actorContext, UUID grupoParoquialResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());

        try {
            policy.assertCanCreate(
                    normalizedRole, normalizedOrgType, actorContext.organizationId(), grupoParoquialResponsavelId);
        } catch (ForbiddenOperationException e) {
            throw new AccessDeniedException(e.getMessage());
        }
    }

    /**
     * Valida se o ator pode editar um projeto existente.
     *
     * @param actorContext contexto do ator autenticado
     * @param projectGrupoParoquialResponsavelId organização dona do projeto
     */
    public void assertCanEdit(EventoActorContext actorContext, UUID projectGrupoParoquialResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());
        policy.assertCanEdit(
                normalizedRole, normalizedOrgType, actorContext.organizationId(), projectGrupoParoquialResponsavelId);
    }

    /**
     * Determina se o ator é considerado de alto nível.
     *
     * @param actorContext contexto do ator autenticado
     * @return true se for ator de alto nível
     */
    public boolean isAtorDeAltoNivel(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());
        return policy.isAtorDeAltoNivel(normalizedRole, normalizedOrgType);
    }

    /**
     * Valida se o ator pode iniciar ou concluir o projeto informado.
     *
     * @param actorContext contexto do ator autenticado
     * @param projectGrupoParoquialResponsavelId organização dona do projeto
     */
    public void assertCanStartOrConclude(EventoActorContext actorContext, UUID projectGrupoParoquialResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());
        policy.assertCanStartOrConclude(
                normalizedRole, normalizedOrgType, actorContext.organizationId(), projectGrupoParoquialResponsavelId);
    }

    /**
     * Valida se o ator pode cancelar o projeto informado.
     *
     * @param actorContext contexto do ator autenticado
     * @param projectGrupoParoquialResponsavelId organização dona do projeto
     */
    public void assertCanCancel(EventoActorContext actorContext, UUID projectGrupoParoquialResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());
        policy.assertCanCancel(
                normalizedRole, normalizedOrgType, actorContext.organizationId(), projectGrupoParoquialResponsavelId);
    }
}
