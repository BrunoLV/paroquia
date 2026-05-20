package br.com.nsfatima.gestao.projeto.api.v1.dto;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Solicitação para atualização parcial de um projeto")
public record ProjetoPatchRequest(
        @Schema(description = "Novo nome do projeto")
        @Size(max = 160) @Pattern(regexp = ".*\\S.*", message = "nome must not be blank if provided") String nome,

        @Schema(description = "Nova descrição do projeto")
        @Size(max = 2000) String descricao,

        @Schema(description = "Nova organização responsável")
        UUID organizacaoResponsavelId,

        @Schema(description = "Nova data de início")
        Instant inicio,

        @Schema(description = "Nova data de término")
        Instant fim,

        @Schema(description = "Novo status do projeto")
        ProjetoStatus status) {
}
