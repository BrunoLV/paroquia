package br.com.nsfatima.gestao.conselho.domain.policy;

/** Ações que um ator pode ter sobre uma ata, conforme seu papel e a situação da ata. */
public enum AcaoAta {
    LER,
    REDIGIR,
    SUBMETER,
    HOMOLOGAR,
    VETAR,
    REABRIR,
    RETIFICAR,
    EXPORTAR,
    VER_TRILHA
}
