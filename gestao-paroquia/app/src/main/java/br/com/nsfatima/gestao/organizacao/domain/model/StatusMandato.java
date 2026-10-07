package br.com.nsfatima.gestao.organizacao.domain.model;

/**
 * Situação canônica de um mandato de coordenação: nasce {@code PROPOSTO} (escolha do grupo ou do conselho),
 * só vale após {@code HOMOLOGADO} pelo Pároco (Cân. 519 e 317), pode ser {@code VETADO} e termina
 * {@code ENCERRADO} ao fim do prazo ou por decisão pastoral.
 */
public enum StatusMandato {
    PROPOSTO,
    HOMOLOGADO,
    VETADO,
    ENCERRADO
}
