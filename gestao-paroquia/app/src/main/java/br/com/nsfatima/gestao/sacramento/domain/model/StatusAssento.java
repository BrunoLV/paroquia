package br.com.nsfatima.gestao.sacramento.domain.model;

/** Ciclo de vida do assento: rascunho editável, selado (somente leitura) ou cancelado com motivo. */
public enum StatusAssento {
    DRAFT,
    SEALED,
    CANCELADO
}
