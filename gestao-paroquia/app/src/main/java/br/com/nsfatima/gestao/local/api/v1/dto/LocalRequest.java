package br.com.nsfatima.gestao.local.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
        boolean ativo
) {
}
