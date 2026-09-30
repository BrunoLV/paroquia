package br.com.nsfatima.gestao.projeto.domain.policy;

import java.util.UUID;

/**
 * Contrato puro de autorização para operações do ciclo de vida de projetos eclesiais.
 */
public interface AutorizacaoProjetoPolicy {

    /**
     * Determina se o ator possui alçada canônica de alto nível (Pároco ou Bispo Diocesano).
     *
     * @param rawRole papel global ou organizacional do ator
     * @param rawTipoGrupo tipo do grupo ao qual o ator está vinculado
     * @return true se o ator for de alto nível
     */
    boolean isAtorDeAltoNivel(String rawRole, String rawTipoGrupo);

    /**
     * Valida permissão para criação de projetos no grupo responsável.
     *
     * @param rawRole papel global ou organizacional do ator
     * @param rawTipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    void assertCanCreate(String rawRole, String rawTipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId);

    /**
     * Valida permissão para edição de dados do projeto.
     *
     * @param rawRole papel global ou organizacional do ator
     * @param rawTipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    void assertCanEdit(String rawRole, String rawTipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId);

    /**
     * Valida permissão para transição de status (iniciar ou concluir projeto).
     *
     * @param rawRole papel global ou organizacional do ator
     * @param rawTipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    void assertCanStartOrConclude(
            String rawRole, String rawTipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId);

    /**
     * Valida permissão para solicitação ou execução de cancelamento de projeto.
     *
     * @param rawRole papel global ou organizacional do ator
     * @param rawTipoGrupo tipo do grupo do ator
     * @param atorOrganizacaoId ID da organização do ator
     * @param targetOrgId ID da organização responsável pelo projeto
     */
    default void assertCanCancel(
            String rawRole, String rawTipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        assertCanStartOrConclude(rawRole, rawTipoGrupo, atorOrganizacaoId, targetOrgId);
    }
}
