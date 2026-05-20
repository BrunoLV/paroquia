package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Mapa de colaboração das pastorais envolvidas no projeto")
public record MapaColaboracaoDTO(
        @Schema(description = "Lista única de nomes das pastorais/grupos envolvidos")
        List<String> envolvidos
) {
}
