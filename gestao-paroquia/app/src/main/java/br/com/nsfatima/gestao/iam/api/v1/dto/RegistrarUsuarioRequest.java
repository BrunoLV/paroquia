package br.com.nsfatima.gestao.iam.api.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RegistrarUsuarioRequest(
    @NotBlank(message = "Nome é obrigatório")
    String nome,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String username,

    @NotBlank(message = "Senha é obrigatória")
    String password,

    @NotBlank(message = "Telefone é obrigatório")
    String telefone,

    @NotNull(message = "Data de nascimento é obrigatória")
    LocalDate dataNascimento,

    List<UUID> organizacoesSolicitadas
) {}
