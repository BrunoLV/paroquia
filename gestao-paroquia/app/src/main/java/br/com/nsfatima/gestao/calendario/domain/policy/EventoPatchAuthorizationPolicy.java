package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.Set;

/**
 * Contrato puro de política para as regras de autorização de criação, edição, gestão de
 * participantes e mudança de organização responsável de um evento de calendário.
 */
public interface EventoPatchAuthorizationPolicy {

    /** Papéis habilitados a criar eventos. */
    Set<PapelOrganizacional> creatorRoles();

    /** Papéis habilitados a editar eventos existentes. */
    Set<PapelOrganizacional> editorRoles();

    /** {@code true} se o papel estiver habilitado a criar eventos. */
    boolean isCreatorRole(PapelOrganizacional papel);

    /** {@code true} se o papel estiver habilitado a editar eventos. */
    boolean isEditorRole(PapelOrganizacional papel);

    /** {@code true} se o papel for isento da restrição de escopo de organização/igreja (o Pároco). */
    boolean isScopeExempt(PapelOrganizacional papel);

    /** {@code true} se o papel possui prerrogativa de efetivar agendamento imediato por decisão pastoral. */
    boolean isLiberacaoConflitoPermitida(PapelOrganizacional papel);

    /**
     * Determina se a criação resultante deve ser aplicada imediatamente ou requer aprovação.
     *
     * @param papel papel organizacional do ator, já validado como {@link #isCreatorRole(PapelOrganizacional)}
     * @param tipoGrupo tipo do grupo do ator
     * @return modo de criação resultante
     */
    ModoRequisicaoCriacao resolveModoRequisicaoCriacao(PapelOrganizacional papel, TipoGrupo tipoGrupo);

    /**
     * Garante que o ator tem permissão para alterar a organização responsável por um evento.
     *
     * @throws OperacaoProibidaException se o ator não for Pároco nem liderança de conselho pastoral autorizada
     */
    void assertCanChangeResponsibleOrganization(PapelOrganizacional papel, TipoGrupo tipoGrupo);
}
