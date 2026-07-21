package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Response with Organization data")
public record OrganizacaoResponse(
    @Schema(description = "Unique identifier")
    UUID id,
    @Schema(description = "Organization name")
    String nome,
    @Schema(description = "Organization type")
    TipoOrganizacao tipo,
    @Schema(description = "Contact information")
    String contato,
    @Schema(description = "Coordinator name")
    String coordenador,
    @Schema(description = "Vice-coordinator name")
    String viceCoordenador,
    @Schema(description = "Whether the organization is active")
    boolean ativo,
    @Schema(description = "Parent organization unique identifier")
    UUID parentId,
    @Schema(description = "Pastoral/Movimento Oficial unique identifier")
    UUID pastoralOficialId
) {
}
