package br.com.nsfatima.gestao.conselho.domain.model;

/**
 * Desfecho de uma deliberação. Os conselhos têm voto estritamente consultivo (Cân. 536 §2), por isso o
 * resultado é um parecer ao Pároco, e não uma decisão com força própria.
 */
public enum ResultadoDeliberacao {
    PARECER_FAVORAVEL,
    PARECER_CONTRARIO,
    ADIADA,
    INFORMATIVA
}
