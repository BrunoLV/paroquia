package br.com.nsfatima.gestao.clero.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DioceseRequest(
        @NotBlank(message = "O nome da diocese é obrigatório")
        String nome,

        @NotBlank(message = "O nome do bispo diocesano é obrigatório")
        String bispoDiocesano,

        String cnpjMitra,

        String cidade,

        @Size(max = 2, message = "UF deve ter no máximo 2 caracteres")
        String uf,

        String contato,

        boolean ativo
) {
}
