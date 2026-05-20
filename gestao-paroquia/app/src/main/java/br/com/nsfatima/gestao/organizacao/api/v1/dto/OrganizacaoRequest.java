package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or update an Organization")
public record OrganizacaoRequest(
    @Schema(description = "Organization name", example = "Pastoral da Juventude")
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String nome,

    @Schema(description = "Organization type")
    @NotNull(message = "Type is required")
    TipoOrganizacao tipo,

    @Schema(description = "Contact information", example = "pj@nsfatima.com")
    @Size(max = 255, message = "Contact must not exceed 255 characters")
    String contato,

    @Schema(description = "Whether the organization is active", example = "true")
    boolean ativo
) {
}
