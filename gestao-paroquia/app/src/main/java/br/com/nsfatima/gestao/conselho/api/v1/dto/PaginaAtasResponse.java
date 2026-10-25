package br.com.nsfatima.gestao.conselho.api.v1.dto;

import java.util.List;

/** Página de atas e o total de registros que atendem ao filtro. */
public record PaginaAtasResponse(List<AtaResumoResponse> itens, long total, int pagina, int tamanho) {
}
