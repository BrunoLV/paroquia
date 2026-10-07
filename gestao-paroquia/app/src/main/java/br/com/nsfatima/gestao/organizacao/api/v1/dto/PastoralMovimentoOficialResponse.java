package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Dados públicos de um item do catálogo oficial de Pastorais e Movimentos.
 */
@Schema(description = "Response with Pastoral or Movimento Oficial data")
public record PastoralMovimentoOficialResponse(
        @Schema(description = "Unique identifier")
        UUID id,
        @Schema(description = "Official catalog name")
        String nome,
        @Schema(description = "Type (PASTORAL or MOVIMENTO)")
        TipoOrganizacao tipo
) {
}
