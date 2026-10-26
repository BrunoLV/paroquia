package br.com.nsfatima.gestao.conselho.application.dto;

import java.util.List;

/** Página de atas visíveis ao ator e o total que atende ao filtro. */
public record PaginaVisoes(List<AtaVisao> itens, long total) {

    public PaginaVisoes {
        itens = List.copyOf(itens);
    }
}
