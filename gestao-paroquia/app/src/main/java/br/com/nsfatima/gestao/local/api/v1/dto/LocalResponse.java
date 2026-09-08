package br.com.nsfatima.gestao.local.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload de resposta com dados de identificação, igreja vinculada, endereço estruturado e auditoria de um Local.
 */
@Schema(description = "Dados de resposta de um Local paroquial")
public record LocalResponse(
        @Schema(description = "Identificador único")
        UUID id,

        @Schema(description = "Nome do local")
        String nome,

        @Schema(description = "Tipo do local")
        String tipo,

        @Schema(description = "Capacidade máxima de ocupação")
        Integer capacidadeMaxima,

        @Schema(description = "Descrição detalhada do local")
        String descricao,

        @Schema(description = "Indica se o local está ativo")
        boolean ativo,

        @Schema(description = "ID da Igreja/Comunidade vinculada")
        UUID igrejaId,

        @Schema(description = "Nome da Igreja/Comunidade vinculada")
        String igrejaNome,

        @Schema(description = "CEP formatado")
        String cep,

        @Schema(description = "Logradouro")
        String logradouro,

        @Schema(description = "Número predial")
        String numero,

        @Schema(description = "Complemento")
        String complemento,

        @Schema(description = "Bairro")
        String bairro,

        @Schema(description = "Cidade")
        String cidade,

        @Schema(description = "UF")
        String uf,

        @Schema(description = "Endereço textual formatado do local")
        String enderecoFormatado,

        @Schema(description = "Data/hora de exclusão lógica")
        Instant deletedAt,

        @Schema(description = "Data/hora de criação do registro")
        Instant createdAt,

        @Schema(description = "Data/hora da última atualização")
        Instant updatedAt
) {
    @JsonProperty("organizacaoId")
    public UUID organizacaoId() {
        return igrejaId;
    }

    @JsonProperty("organizacaoNome")
    public String organizacaoNome() {
        return igrejaNome;
    }
}
