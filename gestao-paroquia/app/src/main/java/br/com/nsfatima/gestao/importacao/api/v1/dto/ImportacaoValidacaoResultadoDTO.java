package br.com.nsfatima.gestao.importacao.api.v1.dto;

import java.util.List;

public record ImportacaoValidacaoResultadoDTO(
        int totalLinhas,
        int totalValidos,
        int totalInvalidos,
        List<ImportacaoItemResultadoDTO> itens
) {}
