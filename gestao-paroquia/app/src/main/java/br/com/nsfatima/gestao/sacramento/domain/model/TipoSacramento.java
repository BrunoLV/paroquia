package br.com.nsfatima.gestao.sacramento.domain.model;

/**
 * Sacramentos que geram assento perpétuo em livro paroquial. Penitência é deliberadamente ausente
 * (sigilo sacramental, Cân. 983) e a Primeira Comunhão é matéria pastoral, não assento canônico.
 */
public enum TipoSacramento {
    BATISMO,
    CONFIRMACAO,
    MATRIMONIO,
    ORDEM
}
