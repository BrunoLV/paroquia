package br.com.nsfatima.gestao.conselho.domain.model;

/** Situação de um convocado na reunião; a ausência justificada não conta como presença para o quórum. */
public enum SituacaoPresenca {
    PRESENTE,
    AUSENTE_JUSTIFICADA,
    AUSENTE
}
