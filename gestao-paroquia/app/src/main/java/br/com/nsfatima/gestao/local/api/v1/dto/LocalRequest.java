package br.com.nsfatima.gestao.local.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload de entrada para criação/atualização de um Local. {@code nome} e {@code tipo} são obrigatórios
 * (o nome também deve ser único entre os locais cadastrados, restrição aplicada na persistência);
 * {@code grupoParoquialId} é opcional e vincula o local a uma organização (Matriz/Comunidade) para escopo
 * de autorização e listagem.
 */
@Schema(description = "Dados para criação ou atualização de um Local")
public record LocalRequest(
        @Schema(description = "Nome do local", example = "Salão Paroquial")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 160, message = "O nome não pode exceder 160 caracteres")
        String nome,

        @Schema(description = "Tipo do local", example = "SALÃO")
        @NotBlank(message = "O tipo é obrigatório")
        @Size(max = 32, message = "O tipo não pode exceder 32 caracteres")
        String tipo,

        @Schema(description = "Endereço do local", example = "Rua das Flores, 123")
        @Size(max = 500, message = "O endereço não pode exceder 500 caracteres")
        String endereco,

        @Schema(description = "Capacidade máxima de pessoas", example = "200")
        Integer capacidade,

        @Schema(description = "Características adicionais", example = "Ar condicionado, Projetor")
        String caracteristicas,

        @Schema(description = "Status do local", example = "true")
        boolean ativo,

        @Schema(description = "ID da organização (Matriz ou Comunidade) de subordinação")
        UUID grupoParoquialId
) {
    public LocalRequest(String nome, String tipo, String endereco, Integer capacidade,
                        String caracteristicas, boolean ativo) {
        this(nome, tipo, endereco, capacidade, caracteristicas, ativo, null);
    }
}
