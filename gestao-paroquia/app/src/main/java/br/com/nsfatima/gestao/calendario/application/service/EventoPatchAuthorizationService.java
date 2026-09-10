package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.iam.application.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Serviço de domínio que concentra as regras de autorização para criação, edição,
 * gestão de participantes e mudança de organização responsável de um evento de
 * calendário. A autorização combina papel organizacional (conjuntos fechados de
 * papéis habilitados a criar/editar) com escopo de organização/igreja: fora do
 * Pároco, o ator só pode agir dentro da própria organização/igreja vinculada, a
 * menos que possua outro vínculo válido (verificado via
 * {@link SecurityContextHolder}) que cubra o escopo do evento. Também decide se
 * uma criação de evento é aplicada imediatamente ou requer aprovação, e restringe o
 * bypass de conflito de agenda eclesial ao Pároco/Admin.
 */
@Service
public class EventoPatchAuthorizationService {

    private static final Set<PapelOrganizacional> ORG_EDITOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.COORD_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.VIGARIO,
            PapelOrganizacional.PADRE);
    private static final Set<PapelOrganizacional> ORG_CREATOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.COORD_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.SECRETARIO,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.VIGARIO,
            PapelOrganizacional.PADRE);

    private final PapelGlobalAuthorizationService papelGlobalAuthorizationService;

    public EventoPatchAuthorizationService(PapelGlobalAuthorizationService papelGlobalAuthorizationService) {
        this.papelGlobalAuthorizationService = papelGlobalAuthorizationService;
    }

    /**
     * Garante que o ator tem permissão para criar um evento no escopo informado,
     * sem retornar o modo de criação resultante.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param grupoParoquialResponsavelId identificador da organização responsável pelo evento a ser criado
     * @param igrejaVinculoId identificador da igreja vinculada ao evento a ser criado, ou {@code null}
     * @throws AccessDeniedException se o ator não tiver papel ou escopo autorizado a criar o evento
     */
    public void assertCanCreate(
            EventoActorContext actorContext, UUID grupoParoquialResponsavelId, UUID igrejaVinculoId) {
        resolveCreateRequestMode(actorContext, grupoParoquialResponsavelId, igrejaVinculoId);
    }

    /**
     * Garante que o ator tem permissão para utilizar o bypass de conflito de
     * agenda eclesial ao criar/editar um evento — restrito a Pároco/Admin,
     * verificado tanto pelo contexto de ator quanto pelas autoridades de segurança
     * do usuário autenticado.
     *
     * @param actorContext contexto de papel/organização do ator autenticado, ou {@code null}
     * @throws AccessDeniedException se o ator não for Pároco nem Admin
     */
    public void assertCanBypassAgendaConflict(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = actorContext != null
                ? PapelOrganizacional.fromStoredValue(actorContext.role())
                : null;

        boolean isParoco = normalizedRole == PapelOrganizacional.PAROCO || normalizedRole == PapelOrganizacional.ADMIN;

        if (!isParoco) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID actorId = auth != null && auth.getPrincipal() instanceof UsuarioDetails details
                    ? details.getUsuarioId() : null;
            isParoco = actorId != null && papelGlobalAuthorizationService.isGlobalByUsuarioId(actorId);
        }

        if (!isParoco) {
            throw new AccessDeniedException(
                    "Apenas o Pároco possui permissão para utilizar o bypass de conflito de agenda eclesial");
        }
    }

    /**
     * Valida se o ator tem papel autorizado a criar eventos e se pode fazê-lo no
     * escopo de organização/igreja informado, e determina se a criação resultante
     * deve ser aplicada imediatamente ou requer aprovação: Pároco e determinadas
     * lideranças de conselho pastoral (Presidente CPC, Mesa CPP, Coordenador Global)
     * criam imediatamente; os demais papéis autorizados requerem aprovação.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param grupoParoquialResponsavelId identificador da organização responsável pelo evento a ser criado
     * @param igrejaVinculoId identificador da igreja vinculada ao evento a ser criado, ou {@code null}
     * @return modo de criação resultante (imediato ou sujeito a aprovação)
     * @throws AccessDeniedException se o ator não tiver papel ou escopo autorizado a criar o evento
     */
    public CreateRequestMode resolveCreateRequestMode(EventoActorContext actorContext,
                                                      UUID grupoParoquialResponsavelId, UUID igrejaVinculoId) {

        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());

        if (!ORG_CREATOR_ROLES.contains(normalizedRole)) {
            throw new AccessDeniedException("Usuário não tem permissão para esta operação de criação");
        }

        if (normalizedRole != PapelOrganizacional.PAROCO && actorContext.organizationId() != null) {
            boolean matchesOrg = actorContext.organizationId().equals(grupoParoquialResponsavelId);
            boolean matchesChurch = (igrejaVinculoId == null) ||
                    (actorContext.igrejaVinculoId() != null && actorContext.igrejaVinculoId().equals(igrejaVinculoId));

            if (!matchesOrg || !matchesChurch) {
                boolean hasOtherValidVinculo = checkUserVinculo(grupoParoquialResponsavelId,
                        igrejaVinculoId, ORG_CREATOR_ROLES);

                if (!hasOtherValidVinculo) {
                    throw new AccessDeniedException(
                            "Usuário não pode criar eventos fora do escopo da organização responsável");
                }
            }
        }

        if (normalizedRole == PapelOrganizacional.PAROCO
                || (normalizedOrgType == TipoGrupo.CONSELHO
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL))) {
            return CreateRequestMode.IMMEDIATE;
        }

        return CreateRequestMode.REQUIRES_APROVACAO;
    }

    /**
     * Garante que o ator tem permissão para editar dados gerais de um evento
     * existente: precisa ter papel editor autorizado e, salvo o Pároco, precisa
     * atuar dentro da organização/igreja responsável pelo evento (ou possuir outro
     * vínculo válido que cubra esse escopo).
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param eventoGrupoParoquialResponsavelId identificador da organização responsável pelo evento
     * @param eventIgrejaVinculoId identificador da igreja vinculada ao evento, ou {@code null}
     * @throws ForbiddenOperationException se o ator não tiver papel ou escopo autorizado a editar o evento
     */
    public void assertCanEditGeneral(EventoActorContext actorContext,
                                     UUID eventoGrupoParoquialResponsavelId, UUID eventIgrejaVinculoId) {

        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        if (!ORG_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException("Usuário não tem permissão para esta operação de alteração");
        }

        if (normalizedRole != PapelOrganizacional.PAROCO && actorContext.organizationId() != null) {
            boolean matchesOrg = actorContext.organizationId().equals(eventoGrupoParoquialResponsavelId);
            boolean matchesChurch = (eventIgrejaVinculoId == null) || (actorContext.igrejaVinculoId() != null &&
                    actorContext.igrejaVinculoId().equals(eventIgrejaVinculoId));

            if (!matchesOrg || !matchesChurch) {
                boolean hasOtherValidVinculo = checkUserVinculo(eventoGrupoParoquialResponsavelId,
                        eventIgrejaVinculoId, ORG_EDITOR_ROLES);

                if (!hasOtherValidVinculo) {
                    throw new ForbiddenOperationException(
                            "Usuário não pode editar eventos fora do escopo da organização responsável");
                }
            }
        }
    }

    /**
     * Garante que o ator tem permissão para gerenciar os participantes (envolvidos)
     * de um evento — reutiliza a mesma regra de {@link #assertCanEditGeneral}, pois
     * a gestão de participantes é tratada como parte da edição geral do evento.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param eventoGrupoParoquialResponsavelId identificador da organização responsável pelo evento
     * @param eventIgrejaVinculoId identificador da igreja vinculada ao evento, ou {@code null}
     * @throws ForbiddenOperationException se o ator não tiver papel ou escopo autorizado a editar o evento
     */
    public void assertCanManageParticipants(EventoActorContext actorContext,
                                            UUID eventoGrupoParoquialResponsavelId, UUID eventIgrejaVinculoId) {

        assertCanEditGeneral(actorContext, eventoGrupoParoquialResponsavelId, eventIgrejaVinculoId);
    }

    /**
     * Garante que o ator tem permissão para alterar a organização responsável por
     * um evento — restrito ao Pároco e a determinadas lideranças de conselho
     * pastoral (Presidente CPC, Mesa CPP, Coordenador Global).
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @throws ForbiddenOperationException se o ator não for Pároco nem liderança de conselho pastoral autorizada
     */
    public void assertCanChangeResponsibleOrganization(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());
        boolean conselhoCoordinator = normalizedOrgType == TipoGrupo.CONSELHO
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL);
        boolean parroco = normalizedRole == PapelOrganizacional.PAROCO;

        if (!conselhoCoordinator && !parroco) {
            throw new ForbiddenOperationException(
                    "Apenas coordenador de conselho ou pároco pode alterar a organização responsável");
        }
    }

    private boolean checkUserVinculo(UUID grupoParoquialResponsavelId,
                                        UUID eventIgrejaVinculoId, Set<PapelOrganizacional> allowedRoles) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getVinculos().stream()
                    .anyMatch(m -> {
                        boolean sameOrg = m.organizationId().equals(grupoParoquialResponsavelId);
                        boolean roleAllowed = allowedRoles.contains(PapelOrganizacional.fromStoredValue(m.role()));
                        boolean sameChurch = (eventIgrejaVinculoId == null) ||
                                (m.igrejaVinculoId() != null && m.igrejaVinculoId().equals(eventIgrejaVinculoId));
                        return sameOrg && roleAllowed && sameChurch;
                    });
        }
        return false;
    }

    /**
     * Modo de processamento de uma solicitação de criação de evento, definido
     * conforme o papel/escopo do ator solicitante.
     */
    public enum CreateRequestMode {
        /** O evento é criado imediatamente, sem passar pelo fluxo de aprovação. */
        IMMEDIATE,
        /** A criação fica pendente até ser decidida através do fluxo de aprovação. */
        REQUIRES_APROVACAO
    }
}
