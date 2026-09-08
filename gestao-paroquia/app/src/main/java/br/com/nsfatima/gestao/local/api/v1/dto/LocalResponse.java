package br.com.nsfatima.gestao.local.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload de saída representando um Local, com dados de identificação, igreja vinculada,
 * endereço estruturado e auditoria.
 */
@Schema(description = "Dados de resposta de um Local")
public record LocalResponse(
        @Schema(description = "Identificador único")
        UUID id,

        @Schema(description = "Nome do local")
        String nome,

        @Schema(description = "Tipo do local")
        String tipo,

        @Schema(description = "Endereço textual formatado do local")
        String endereco,

        @Schema(description = "Capacidade máxima legada")
        Integer capacidade,

        @Schema(description = "Capacidade máxima de ocupação")
        Integer capacidadeMaxima,

        @Schema(description = "Características adicionais legadas")
        String caracteristicas,

        @Schema(description = "Descrição detalhada do local")
        String descricao,

        @Schema(description = "Indica se o local está ativo")
        boolean ativo,

        @Schema(description = "ID da organização (legado)")
        UUID grupoParoquialId,

        @Schema(description = "ID da Igreja/Comunidade vinculada")
        UUID igrejaId,

        @Schema(description = "Nome da organização (legado)")
        String grupoParoquialNome,

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

        @Schema(description = "Data/hora de exclusão lógica")
        Instant deletedAt,

        @Schema(description = "Data/hora de criação do registro")
        Instant createdAt,

        @Schema(description = "Data/hora da última atualização")
        Instant updatedAt
) {
    public LocalResponse(UUID id, String nome, String tipo, String endereco,
                         Integer capacidade, String caracteristicas, boolean ativo) {
        this(id, nome, tipo, endereco, capacidade, capacidade, caracteristicas, caracteristicas,
                ativo, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null);
    }

    public LocalResponse(UUID id, String nome, String tipo, String endereco,
                         Integer capacidade, String caracteristicas, boolean ativo,
                         UUID grupoParoquialId, String grupoParoquialNome) {
        this(id, nome, tipo, endereco, capacidade, capacidade, caracteristicas, caracteristicas,
                ativo, grupoParoquialId, grupoParoquialId, grupoParoquialNome, grupoParoquialNome,
                null, null, null, null, null, null, null, null, null, null);
    }
}
