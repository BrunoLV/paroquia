package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.UUID;

/**
 * Contrato puro de autorização para operações do ciclo de vida de projetos eclesiais.
 */
public interface AutorizacaoProjetoPolicy {

    /**
     * Determina se o ator possui alçada de alto nível (Pároco, Admin ou Liderança de Conselho).
     *
     * @param papel papel organizacional do ator
     * @param tipoGrupo tipo do grupo ao qual o ator está vinculado
     * @return true se o ator for de alto nível
     */
    boolean isHighLevelActor(PapelOrganizacional papel, TipoGrupo tipoGrupo);

    /**
     * Valida permissão para criação de projetos no grupo responsável.
     *
     * @param papel papel organizacional do ator
     * @param tipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    void assertCanCreate(PapelOrganizacional papel, TipoGrupo tipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId);

    /**
     * Valida permissão para edição de dados do projeto.
     *
     * @param papel papel organizacional do ator
     * @param tipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    void assertCanEdit(PapelOrganizacional papel, TipoGrupo tipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId);

    /**
     * Valida permissão para transição de status (iniciar ou concluir projeto).
     *
     * @param papel papel organizacional do ator
     * @param tipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    void assertCanStartOrConclude(PapelOrganizacional papel, TipoGrupo tipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId);

    /**
     * Valida permissão para solicitação ou execução de cancelamento de projeto.
     *
     * @param papel papel organizacional do ator
     * @param tipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    default void assertCanCancel(PapelOrganizacional papel, TipoGrupo tipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        assertCanStartOrConclude(papel, tipoGrupo, atorOrganizacaoId, targetOrgId);
    }
}
