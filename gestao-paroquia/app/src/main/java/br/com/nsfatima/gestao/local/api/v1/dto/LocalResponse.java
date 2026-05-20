package br.com.nsfatima.gestao.local.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados de resposta de um Local")
public record LocalResponse(
        @Schema(description = "Identificador único")
        UUID id,
        @Schema(description = "Nome do local")
        String nome,
        @Schema(description = "Tipo do local")
        String tipo,
        @Schema(description = "Endereço do local")
        String endereco,
        @Schema(description = "Capacidade máxima")
        Integer capacidade,
        @Schema(description = "Características adicionais")
        String caracteristicas,
        @Schema(description = "Indica se o local está ativo")
        boolean ativo
) {
}
