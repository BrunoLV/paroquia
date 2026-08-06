package br.com.nsfatima.gestao.organizacao.domain.model;

/**
 * Classifica um nó da hierarquia territorial/estrutural da paróquia — {@code CLERO} (raiz) →
 * {@code MATRIZ}/{@code COMUNIDADE}. Diferente de {@link TipoGrupo}, não representa um agrupamento de
 * fiéis, e sim a estrutura territorial que hospeda os grupos paroquiais.
 */
public enum TipoUnidadeTerritorial {
    CLERO,
    MATRIZ,
    COMUNIDADE
}
