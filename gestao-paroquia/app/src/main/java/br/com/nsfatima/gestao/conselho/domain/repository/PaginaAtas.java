package br.com.nsfatima.gestao.conselho.domain.repository;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;

import java.util.List;

/** Página de atas e o total de registros que atendem ao filtro. */
public record PaginaAtas(List<AtaReuniao> itens, long total) {

    public PaginaAtas {
        itens = List.copyOf(itens);
    }

    public static PaginaAtas vazia() {
        return new PaginaAtas(List.of(), 0);
    }
}
