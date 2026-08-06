package br.com.nsfatima.gestao.local.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Payload de saída representando um Local, incluindo o nome da organização (Matriz/Comunidade) já
 * resolvido para exibição, quando o local estiver vinculado a uma.
 */
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
        boolean ativo,
        @Schema(description = "ID da organização (Matriz/Comunidade) de subordinação")
        UUID grupoParoquialId,
        @Schema(description = "Nome da organização (Matriz/Comunidade) de subordinação")
        String grupoParoquialNome
) {
    public LocalResponse(UUID id, String nome, String tipo, String endereco,
                         Integer capacidade, String caracteristicas, boolean ativo) {
        this(id, nome, tipo, endereco, capacidade, caracteristicas, ativo, null, null);
    }
}
