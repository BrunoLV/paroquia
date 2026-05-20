package br.com.nsfatima.gestao.projeto.api.v1.dto;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados de um projeto paroquial")
public record ProjetoResponse(
        @Schema(description = "ID único do projeto")
        UUID id,

        @Schema(description = "Nome do projeto")
        String nome,

        @Schema(description = "Descrição do projeto")
        String descricao,

        @Schema(description = "ID da organização responsável")
        UUID organizacaoResponsavelId,

        @Schema(description = "Data de início do projeto")
        Instant inicio,

        @Schema(description = "Data de término do projeto")
        Instant fim,

        @Schema(description = "Status atual do projeto")
        ProjetoStatus status,

        @Schema(description = "Indica se o projeto foi atualizado")
        boolean updated) {
}
