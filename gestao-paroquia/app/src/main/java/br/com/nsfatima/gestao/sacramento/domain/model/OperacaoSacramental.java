package br.com.nsfatima.gestao.sacramento.domain.model;

/** Operações sujeitas à matriz de alçadas Secretaria × Pároco (spec §4). */
public enum OperacaoSacramental {
    LER,
    LAVRAR_RASCUNHO,
    VINCULAR,
    APROVAR_VINCULO,
    DESVINCULAR,
    AVERBAR_DIGITAR,
    EMITIR_CERTIDAO,
    HOMOLOGAR,
    HOMOLOGAR_AVERBACAO,
    RETIFICAR,
    CANCELAR
}
