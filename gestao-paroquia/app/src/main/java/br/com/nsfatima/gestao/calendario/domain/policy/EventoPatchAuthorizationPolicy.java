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

    /** Papéis organizacionais habilitados a criar eventos. */
    Set<PapelOrganizacional> creatorRoles();

    /** Papéis organizacionais habilitados a editar eventos existentes. */
    Set<PapelOrganizacional> editorRoles();

    /** {@code true} se o papel estiver habilitado a criar eventos. */
    boolean isCreatorRole(String rawRole);

    /** {@code true} se o papel estiver habilitado a editar eventos. */
    boolean isEditorRole(String rawRole);

    /** {@code true} se o papel for isento da restrição de escopo de organização/igreja. */
    boolean isScopeExempt(String rawRole);

    /** {@code true} se o papel possui prerrogativa de efetivar agendamento imediato por decisão pastoral. */
    boolean isLiberacaoConflitoPermitida(String rawRole);

    /**
     * Determina se a criação resultante deve ser aplicada imediatamente ou requer aprovação.
     *
     * @param rawRole papel global ou organizacional do ator
     * @param tipoGrupo tipo do grupo do ator
     * @return modo de criação resultante
     */
    ModoRequisicaoCriacao resolveModoRequisicaoCriacao(String rawRole, TipoGrupo tipoGrupo);

    /**
     * Garante que o ator tem permissão para alterar a organização responsável por um evento.
     *
     * @throws OperacaoProibidaException se o ator não possuir alçada para alterar organização responsável
     */
    void assertCanChangeResponsibleOrganization(String rawRole, TipoGrupo tipoGrupo);
}
