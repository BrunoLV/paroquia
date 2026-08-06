package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateFielRequest(
        @NotBlank String nome,
        @NotBlank String cpf,
        @NotNull LocalDate dataNascimento,
        @NotBlank String email,
        @NotBlank String telefone,
        @NotBlank String endereco,
        @NotNull Vocacao vocacao,
        Boolean batizado,
        Boolean primeiraComunhao,
        Boolean crismado,
        Boolean casadoIgrejaCatolica
) {
}
