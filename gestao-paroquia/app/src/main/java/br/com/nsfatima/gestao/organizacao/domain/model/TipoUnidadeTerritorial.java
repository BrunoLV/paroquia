package br.com.nsfatima.gestao.organizacao.domain.model;

/**
 * Classifica um nó da hierarquia territorial e predial da paróquia:
 * {@code MATRIZ} (sede paroquial física) ou {@code COMUNIDADE} (capela/comunidade filial).
 * O Governo Eclesiástico (Clero e Diocese) não é uma unidade territorial, e sim a autoridade pastoral e canônica.
 */
public enum TipoUnidadeTerritorial {
    MATRIZ,
    COMUNIDADE
}
