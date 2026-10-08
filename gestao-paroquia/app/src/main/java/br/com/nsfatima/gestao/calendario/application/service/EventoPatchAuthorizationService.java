package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.calendario.domain.policy.ModoRequisicaoCriacao;
import br.com.nsfatima.gestao.calendario.domain.policy.EventoPatchAuthorizationPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.iam.application.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Orquestra as regras de autorização para criação, edição, gestão de participantes e mudança de
 * organização responsável de um evento de calendário: carrega os fatos de escopo (vínculos do
 * ator autenticado via {@link SecurityContextHolder}, acesso global via
 * {@link PapelGlobalAuthorizationService}) e delega a decisão de papel/escopo à
 * {@link EventoPatchAuthorizationPolicy}, que é pura e não conhece I/O.
 */
@Service
public class EventoPatchAuthorizationService {

    private final PapelGlobalAuthorizationService papelGlobalAuthorizationService;
    private final EventoPatchAuthorizationPolicy policy;

    public EventoPatchAuthorizationService(
            PapelGlobalAuthorizationService papelGlobalAuthorizationService,
            EventoPatchAuthorizationPolicy policy) {
        this.papelGlobalAuthorizationService = papelGlobalAuthorizationService;
        this.policy = policy;
    }

    /**
     * Garante que o ator tem permissão para criar um evento no escopo informado,
     * sem retornar o modo de criação resultante.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param organizacaoResponsavelId identificador da organização responsável pelo evento a ser criado
     * @param igrejaVinculoId identificador da igreja vinculada ao evento a ser criado, ou {@code null}
     * @throws AccessDeniedException se o ator não tiver papel ou escopo autorizado a criar o evento
     */
    public void assertCanCreate(
            EventoActorContext actorContext, UUID organizacaoResponsavelId, UUID igrejaVinculoId) {
        resolveModoRequisicaoCriacao(actorContext, organizacaoResponsavelId, igrejaVinculoId);
    }

    /**
     * Garante que o ator possui autoridade pastoral para efetivar o agendamento
     * imediato de eventos sem submissão prévia ao Conselho Paroquial — restrito a Pároco/Admin,
     * verificado tanto pelo contexto de ator quanto pelas autoridades de segurança
     * do usuário autenticado.
     *
     * @param actorContext contexto de papel/organização do ator autenticado, ou {@code null}
     * @throws AccessDeniedException se o ator não tiver autoridade para decisão pastoral imediata
     */
    public void assertCanLiberarConflitoAgenda(EventoActorContext actorContext) {
        String rawRole = actorContext != null ? actorContext.role() : null;

        boolean isAuthorized = policy.isLiberacaoConflitoPermitida(rawRole);

        if (!isAuthorized) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID actorId = auth != null && auth.getPrincipal() instanceof UsuarioDetails details
                    ? details.getUsuarioId() : null;
            isAuthorized = actorId != null && papelGlobalAuthorizationService.isGlobalByUsuarioId(actorId);
        }

        if (!isAuthorized) {
            throw new AccessDeniedException(
                    "Apenas o Pároco possui autoridade para efetivar agendamento imediato por decisão pastoral.");
        }
    }

    /**
     * Valida se o ator tem papel autorizado a criar eventos e se pode fazê-lo no
     * escopo de organização/igreja informado, e determina se a criação resultante
     * deve ser aplicada imediatamente ou requer aprovação.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param organizacaoResponsavelId identificador da organização responsável pelo evento a ser criado
     * @param igrejaVinculoId identificador da igreja vinculada ao evento a ser criado, ou {@code null}
     * @return modo de criação resultante (imediato ou sujeito a aprovação)
     * @throws AccessDeniedException se o ator não tiver papel ou escopo autorizado a criar o evento
     */
    public ModoRequisicaoCriacao resolveModoRequisicaoCriacao(EventoActorContext actorContext,
                                                      UUID organizacaoResponsavelId, UUID igrejaVinculoId) {

        String rawRole = actorContext.role();
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (!policy.isCreatorRole(rawRole)) {
            throw new AccessDeniedException("Usuário não tem permissão para esta operação de criação");
        }

        boolean escopoAutorizado = policy.isScopeExempt(rawRole) || actorContext.organizationId() == null
                || matchesScope(actorContext, organizacaoResponsavelId, igrejaVinculoId)
                || verificarVinculoUsuario(organizacaoResponsavelId, igrejaVinculoId, policy.creatorRoles());

        if (!escopoAutorizado) {
            throw new AccessDeniedException(
                    "Usuário não pode criar eventos fora do escopo da organização responsável");
        }

        return policy.resolveModoRequisicaoCriacao(rawRole, normalizedOrgType);
    }

    /**
     * Garante que o ator tem permissão para editar dados gerais de um evento
     * existente: precisa ter papel editor autorizado e, salvo o Pároco, precisa
     * atuar dentro da organização/igreja responsável pelo evento (ou possuir outro
     * vínculo válido que cubra esse escopo).
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param eventoOrganizacaoResponsavelId identificador da organização responsável pelo evento
     * @param eventIgrejaVinculoId identificador da igreja vinculada ao evento, ou {@code null}
     * @throws OperacaoProibidaException se o ator não tiver papel ou escopo autorizado a editar o evento
     */
    public void assertCanEditGeneral(EventoActorContext actorContext,
                                     UUID eventoOrganizacaoResponsavelId, UUID eventIgrejaVinculoId) {

        String rawRole = actorContext.role();
        if (!policy.isEditorRole(rawRole)) {
            throw new OperacaoProibidaException("Usuário não tem permissão para esta operação de alteração");
        }

        boolean escopoAutorizado = policy.isScopeExempt(rawRole) || actorContext.organizationId() == null
                || matchesScope(actorContext, eventoOrganizacaoResponsavelId, eventIgrejaVinculoId)
                || verificarVinculoUsuario(eventoOrganizacaoResponsavelId,
                        eventIgrejaVinculoId, policy.editorRoles());

        if (!escopoAutorizado) {
            throw new OperacaoProibidaException(
                    "Usuário não pode editar eventos fora do escopo da organização responsável");
        }
    }

    /**
     * Garante que o ator tem permissão para gerenciar os participantes (envolvidos)
     * de um evento — reutiliza a mesma regra de {@link #assertCanEditGeneral}, pois
     * a gestão de participantes é tratada como parte da edição geral do evento.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param eventoOrganizacaoResponsavelId identificador da organização responsável pelo evento
     * @param eventIgrejaVinculoId identificador da igreja vinculada ao evento, ou {@code null}
     * @throws OperacaoProibidaException se o ator não tiver papel ou escopo autorizado a editar o evento
     */
    public void assertCanManageParticipants(EventoActorContext actorContext,
                                            UUID eventoOrganizacaoResponsavelId, UUID eventIgrejaVinculoId) {

        assertCanEditGeneral(actorContext, eventoOrganizacaoResponsavelId, eventIgrejaVinculoId);
    }

    /**
     * Garante que o ator tem permissão para alterar a organização responsável por
     * um evento — restrito ao Pároco.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @throws OperacaoProibidaException se o ator não for Pároco
     */
    public void assertCanChangeResponsibleOrganization(EventoActorContext actorContext) {
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());
        policy.assertCanChangeResponsibleOrganization(actorContext.role(), normalizedOrgType);
    }

    private boolean matchesScope(EventoActorContext actorContext,
                                  UUID organizacaoResponsavelId, UUID igrejaVinculoId) {
        boolean matchesOrg = actorContext.organizationId().equals(organizacaoResponsavelId);
        boolean matchesChurch = (igrejaVinculoId == null)
                || (actorContext.igrejaVinculoId() != null && actorContext.igrejaVinculoId().equals(igrejaVinculoId));
        return matchesOrg && matchesChurch;
    }

    private boolean verificarVinculoUsuario(UUID organizacaoResponsavelId,
                                        UUID eventIgrejaVinculoId, Set<PapelOrganizacional> allowedRoles) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getVinculos().stream()
                    .anyMatch(m -> {
                        boolean sameOrg = m.organizationId().equals(organizacaoResponsavelId);
                        boolean roleAllowed = allowedRoles.contains(PapelOrganizacional.fromStoredValue(m.role()));
                        boolean sameChurch = (eventIgrejaVinculoId == null) ||
                                (m.igrejaVinculoId() != null && m.igrejaVinculoId().equals(eventIgrejaVinculoId));
                        return sameOrg && roleAllowed && sameChurch;
                    });
        }
        return false;
    }
}
