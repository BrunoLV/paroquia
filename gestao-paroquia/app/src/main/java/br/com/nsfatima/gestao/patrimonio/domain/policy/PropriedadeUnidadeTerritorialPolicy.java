package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;

/**
 * Define a regra de elegibilidade de uma unidade territorial para a guarda de patrimônio.
 *
 * DECISÃO DE DESIGN (Strategy): a regra recebe somente o fato já carregado pelo caso de uso,
 * preservando o domínio de repositórios e detalhes de persistência.
 */
@FunctionalInterface
public interface PropriedadeUnidadeTerritorialPolicy {

    /**
     * Valida a aptidão do tipo territorial para possuir um ativo patrimonial.
     *
     * @param tipo tipo territorial previamente resolvido pela aplicação
     */
    void assertPermitida(TipoUnidadeTerritorial tipo);
}
