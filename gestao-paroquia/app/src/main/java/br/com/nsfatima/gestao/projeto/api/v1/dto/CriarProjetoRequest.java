package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload para criação de um novo projeto. Se o ator não tiver nível de autorização suficiente
 * ({@code ProjetoAuthorizationService#assertCanCreate}), este mesmo payload é serializado e
 * persistido como {@code actionPayloadJson} em uma solicitação de aprovação pendente, para ser
 * reconstruído e efetivado em {@code TransicionarProjetoUseCase#decidir} quando aprovado.
 */
@Schema(description = "Solicitação para criação de um novo projeto")
public record CriarProjetoRequest(
        @Schema(description = "Nome do projeto", example = "Reforma da Capela")
        @NotBlank @Size(max = 160) String nome,

        @Schema(description = "Descrição detalhada do projeto", example = "Projeto para arrecadação de fundos e " +
                "execução da reforma")
        @Size(max = 2000) String descricao,

        @Schema(description = "ID da organização responsável pelo projeto")
        @NotNull UUID grupoParoquialResponsavelId,

        @Schema(description = "Data e hora de início do projeto (UTC)")
        @NotNull Instant inicio,

        @Schema(description = "Data e hora de término do projeto (UTC)")
        @NotNull Instant fim) {
}
