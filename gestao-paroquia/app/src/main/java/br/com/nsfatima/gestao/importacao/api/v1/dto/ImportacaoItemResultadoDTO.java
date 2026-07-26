package br.com.nsfatima.gestao.importacao.api.v1.dto;

import java.util.Map;

public record ImportacaoItemResultadoDTO(
        int linha,
        boolean valido,
        String mensagemErro,
        Map<String, String> dados
) {
}
