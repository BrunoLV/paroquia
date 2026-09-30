package br.com.nsfatima.gestao.patrimonio.domain.policy;

import java.util.UUID;

/**
 * Contrato puro de autorização para gestão de ativos patrimoniais da paróquia.
 */
public interface AutorizacaoPatrimonioPolicy {

    /**
     * Verifica se o papel do ator confere alçada de alto nível em patrimônio.
     *
     * @param rawRole papel global ou organizacional do ator
     * @return true se for ator de alto nível
     */
    boolean isAtorDeAltoNivel(String rawRole);

    /**
     * Valida se o ator tem permissão para gerir bens da unidade territorial informada.
     *
     * @param rawRole papel global ou organizacional do ator
     * @param atorOrganizacaoId ID da organização vinculada ao ator
     * @param grupoParoquialId ID da unidade territorial proprietária do ativo
     */
    void assertPodeGerirPatrimonio(String rawRole, UUID atorOrganizacaoId, UUID grupoParoquialId);
}
