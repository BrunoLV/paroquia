package br.com.nsfatima.gestao.projeto.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
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
     * @param organizacaoResponsavelId organização alvo
     */
    public void assertCanCreate(EventoActorContext actorContext, UUID organizacaoResponsavelId) {
        try {
            policy.assertCanCreate(
                    actorContext.role(),
                    actorContext.organizationType(),
                    actorContext.organizationId(),
                    organizacaoResponsavelId);
        } catch (OperacaoProibidaException e) {
            throw new AccessDeniedException(e.getMessage());
        }
    }

    /**
     * Valida se o ator pode editar um projeto existente.
     *
     * @param actorContext contexto do ator autenticado
     * @param projectOrganizacaoResponsavelId organização dona do projeto
     */
    public void assertCanEdit(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        policy.assertCanEdit(
                actorContext.role(),
                actorContext.organizationType(),
                actorContext.organizationId(),
                projectOrganizacaoResponsavelId);
    }

    /**
     * Determina se o ator é considerado de alto nível.
     *
     * @param actorContext contexto do ator autenticado
     * @return true se for ator de alto nível
     */
    public boolean isAtorDeAltoNivel(EventoActorContext actorContext) {
        return policy.isAtorDeAltoNivel(actorContext.role(), actorContext.organizationType());
    }

    /**
     * Valida se o ator pode iniciar ou concluir o projeto informado.
     *
     * @param actorContext contexto do ator autenticado
     * @param projectOrganizacaoResponsavelId organização dona do projeto
     */
    public void assertCanStartOrConclude(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        policy.assertCanStartOrConclude(
                actorContext.role(),
                actorContext.organizationType(),
                actorContext.organizationId(),
                projectOrganizacaoResponsavelId);
    }

    /**
     * Valida se o ator pode cancelar o projeto informado.
     *
     * @param actorContext contexto do ator autenticado
     * @param projectOrganizacaoResponsavelId organização dona do projeto
     */
    public void assertCanCancel(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        policy.assertCanCancel(
                actorContext.role(),
                actorContext.organizationType(),
                actorContext.organizationId(),
                projectOrganizacaoResponsavelId);
    }
}
