package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.UUID;

/**
 * Contrato puro de autorização para gestão de ativos patrimoniais da paróquia.
 */
public interface AutorizacaoPatrimonioPolicy {

    /**
     * Verifica se o papel do ator confere alçada de alto nível em patrimônio.
     *
     * @param papel papel organizacional do ator
     * @return true se for ator de alto nível
     */
    boolean isHighLevelActor(PapelOrganizacional papel);

    /**
     * Valida se o ator tem permissão para gerir bens da unidade territorial informada.
     *
     * @param papel papel organizacional do ator
     * @param atorOrganizacaoId ID da organização vinculada ao ator
     * @param grupoParoquialId ID da unidade territorial proprietária do ativo
     */
    void assertPodeGerirPatrimonio(PapelOrganizacional papel, UUID atorOrganizacaoId, UUID grupoParoquialId);
}
