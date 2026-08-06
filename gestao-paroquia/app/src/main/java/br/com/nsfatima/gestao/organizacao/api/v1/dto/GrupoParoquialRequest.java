package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload de criação/atualização de uma organização. A combinação de {@code tipo}, {@code parentId} e
 * {@code pastoralOficialId} é validada por {@code GrupoParoquialService.validateHierarchy} contra as regras da
 * hierarquia organizacional (ex.: Pastoral/Movimento exige {@code pastoralOficialId} e organização-mãe do
 * tipo {@code CONSELHO_PASTORAL}).
 */
@Schema(description = "Request to create or update an Organization")
public record GrupoParoquialRequest(
        @Schema(description = "Organization name", example = "Pastoral da Juventude")
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String nome,

        @Schema(description = "Organization type")
        @NotNull(message = "Type is required")
        TipoGrupo tipo,

        @Schema(description = "Contact information", example = "pj@nsfatima.com")
        @Size(max = 255, message = "Contact must not exceed 255 characters")
        String contato,

        @Schema(description = "Coordinator name", example = "João Silva")
        @Size(max = 255, message = "Coordinator name must not exceed 255 characters")
        String coordenador,

        @Schema(description = "Vice-coordinator name", example = "Maria Santos")
        @Size(max = 255, message = "Vice-coordinator name must not exceed 255 characters")
        String viceCoordenador,

        @Schema(description = "Whether the organization is active", example = "true")
        boolean ativo,

        @Schema(description = "Parent organization unique identifier")
        UUID parentId,

        @Schema(description = "Pastoral/Movimento Oficial unique identifier")
        UUID pastoralOficialId
) {
}
