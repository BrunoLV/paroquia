package br.com.nsfatima.gestao.projeto.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Serviço de domínio que centraliza as regras de autorização para operações sobre
 * {@code Projeto}: quem pode criar, editar, iniciar/concluir e cancelar. As verificações são
 * baseadas em papel do ator (`PapelOrganizacional`) e no escopo de organização
 * (`organizacaoResponsavelId` do projeto vs. organização do ator), não em posse/ownership
 * simples.
 *
 * <p>Regras de autorização aplicadas:
 * <ol>
 *     <li><b>Criar</b> ({@link #assertCanCreate}): o ator precisa ter um papel em
 *     {@code PROJECT_EDITOR_ROLES}; atores que não são "high-level" só podem criar projetos para
 *     a própria organização.</li>
 *     <li><b>Editar</b> ({@link #assertCanEdit}): mesma condição de papel de
 *     {@code PROJECT_EDITOR_ROLES}; atores que não são "high-level" só podem editar projetos da
 *     própria organização.</li>
 *     <li><b>Ator de alto nível</b> ({@link #isHighLevelActor}): {@code PAROCO} e {@code ADMIN}
 *     são sempre considerados de alto nível; coordenadores de conselho pastoral
 *     ({@code PRESIDENTE_CPC}, {@code MESA_CPP}, {@code COORD_PASTORAL_GLOBAL}) só são
 *     considerados de alto nível quando o tipo de organização do ator é
 *     {@code CONSELHO_PASTORAL}, {@code CPP} ou {@code CPC}.</li>
 *     <li><b>Iniciar/Concluir</b> ({@link #assertCanStartOrConclude}): atores de alto nível
 *     sempre podem; caso contrário, apenas líderes de organização
 *     ({@code COORD_PASTORAL_GLOBAL}, {@code COORD_PASTORAL_LOCAL}, {@code COORD_MOVIMENTO},
 *     {@code COORD_PASTORAL}, {@code PRESIDENTE_CPC}) e apenas quando o projeto pertence à
 *     própria organização do ator.</li>
 *     <li><b>Cancelar</b> ({@link #assertCanCancel}): reutiliza integralmente a regra de
 *     iniciar/concluir. É essa mesma verificação que decide, em
 *     {@code TransitionProjetoUseCase#cancelar}, se o cancelamento é aplicado imediatamente
 *     (ator de alto nível) ou se é desviado para uma solicitação de aprovação pendente
 *     (demais atores autorizados a cancelar).</li>
 * </ol>
 */
@Service
public class ProjetoAuthorizationService {

    private static final Set<PapelOrganizacional> PROJECT_EDITOR_ROLES = Set.of(
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

    /**
     * Verifica se o ator pode criar um projeto para a organização informada. Exige um papel em
     * {@code PROJECT_EDITOR_ROLES}; atores que não são de alto nível ({@link #isHighLevelActor})
     * só podem criar projetos cuja organização responsável seja a própria organização do ator.
     *
     * @param actorContext              contexto do ator autenticado.
     * @param organizacaoResponsavelId  organização para a qual o projeto será criado.
     * @throws AccessDeniedException se o papel não permitir criar projetos ou se o ator tentar
     *                                criar para outra organização sem ser de alto nível.
     */
    public void assertCanCreate(EventoActorContext actorContext, UUID organizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (!PROJECT_EDITOR_ROLES.contains(normalizedRole)) {
            throw new AccessDeniedException("Usuário não tem permissão para criar projetos");
        }

        // Only Paroco or Conselho Coordinators can create projects for any organization.
        // Others can only create for their own organization.
        boolean isHighLevelActor = isHighLevelActor(actorContext);

        if (!isHighLevelActor && actorContext.organizationId() != null
                && !actorContext.organizationId().equals(organizacaoResponsavelId)) {
            throw new AccessDeniedException("Usuário não pode criar projetos para outra organização");
        }
    }

    /**
     * Verifica se o ator pode editar um projeto já existente pertencente à organização
     * informada. Exige um papel em {@code PROJECT_EDITOR_ROLES}; atores que não são de alto
     * nível só podem editar projetos da própria organização.
     *
     * @param actorContext                      contexto do ator autenticado.
     * @param projectOrganizacaoResponsavelId   organização responsável pelo projeto a editar.
     * @throws ForbiddenOperationException se o papel não permitir editar projetos ou se o ator
     *                                      tentar editar projeto de outra organização sem ser
     *                                      de alto nível.
     */
    public void assertCanEdit(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());

        if (!PROJECT_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException("Usuário não tem permissão para editar projetos");
        }

        boolean isHighLevelActor = isHighLevelActor(actorContext);

        if (!isHighLevelActor && actorContext.organizationId() != null
                && !actorContext.organizationId().equals(projectOrganizacaoResponsavelId)) {
            throw new ForbiddenOperationException("Usuário não pode editar projetos de outra organização");
        }
    }

    /**
     * Determina se o ator é considerado de "alto nível" para fins de autorização de projetos.
     * {@code PAROCO} e {@code ADMIN} são sempre de alto nível; coordenadores de conselho
     * pastoral ({@code PRESIDENTE_CPC}, {@code MESA_CPP}, {@code COORD_PASTORAL_GLOBAL}) só são
     * de alto nível quando pertencem a uma organização do tipo {@code CONSELHO_PASTORAL},
     * {@code CPP} ou {@code CPC}. Atores de alto nível ignoram o escopo de organização em todas
     * as demais verificações desta classe.
     *
     * @param actorContext contexto do ator autenticado.
     * @return {@code true} se o ator for considerado de alto nível.
     */
    public boolean isHighLevelActor(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());
        return normalizedRole == PapelOrganizacional.PAROCO
                || normalizedRole == PapelOrganizacional.ADMIN
                || ((normalizedOrgType == TipoOrganizacao.CONSELHO_PASTORAL
                        || normalizedOrgType == TipoOrganizacao.CPP
                        || normalizedOrgType == TipoOrganizacao.CPC)
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL));
    }

    /**
     * Verifica se o ator pode iniciar ({@code ABERTO -> EM_ANDAMENTO}) ou concluir
     * ({@code EM_ANDAMENTO -> CONCLUIDO}) o projeto informado. Atores de alto nível sempre
     * podem; demais atores só podem quando ocupam um papel de liderança de organização
     * ({@code COORD_PASTORAL_GLOBAL}, {@code COORD_PASTORAL_LOCAL}, {@code COORD_MOVIMENTO},
     * {@code COORD_PASTORAL}, {@code PRESIDENTE_CPC}) e o projeto pertence à própria
     * organização do ator.
     *
     * @param actorContext                      contexto do ator autenticado.
     * @param projectOrganizacaoResponsavelId   organização responsável pelo projeto.
     * @throws ForbiddenOperationException se nenhuma das condições de autorização for satisfeita.
     */
    public void assertCanStartOrConclude(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        if (isHighLevelActor(actorContext)) {
            return;
        }
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        boolean isOrgLeader = (normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_LOCAL
                || normalizedRole == PapelOrganizacional.COORD_MOVIMENTO
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL
                || normalizedRole == PapelOrganizacional.PRESIDENTE_CPC);
        if (isOrgLeader && actorContext.organizationId() != null
                && actorContext.organizationId().equals(projectOrganizacaoResponsavelId)) {
            return;
        }
        throw new ForbiddenOperationException("Usuário não tem permissão para executar esta transição de projeto");
    }

    /**
     * Verifica se o ator pode solicitar o cancelamento do projeto. Reutiliza integralmente a
     * regra de {@link #assertCanStartOrConclude}: o resultado desta verificação (lançar ou não
     * exceção) não decide sozinho se o cancelamento é imediato — {@code TransitionProjetoUseCase}
     * ainda consulta {@link #isHighLevelActor} para decidir entre aplicar o cancelamento
     * diretamente ou desviá-lo para uma solicitação de aprovação pendente.
     *
     * @param actorContext                      contexto do ator autenticado.
     * @param projectOrganizacaoResponsavelId   organização responsável pelo projeto.
     * @throws ForbiddenOperationException se o ator não tiver permissão para agir sobre o
     *                                      projeto (mesma condição de {@link #assertCanStartOrConclude}).
     */
    public void assertCanCancel(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        assertCanStartOrConclude(actorContext, projectOrganizacaoResponsavelId);
    }
}

