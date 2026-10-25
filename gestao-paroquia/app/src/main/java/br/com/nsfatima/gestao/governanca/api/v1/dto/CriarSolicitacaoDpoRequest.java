package br.com.nsfatima.gestao.governanca.api.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Requisição pública de exercício de direitos dos titulares endereçada ao DPO (Art. 18 LGPD). */
public record CriarSolicitacaoDpoRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 255)
        String titularNome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        @Size(max = 255)
        String titularEmail,

        @Size(max = 14)
        String titularCpf,

        @NotBlank(message = "O tipo de solicitação é obrigatório.")
        @Size(max = 50)
        String tipoSolicitacao,

        @NotBlank(message = "A mensagem é obrigatória.")
        String mensagem
) {}
