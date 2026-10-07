package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.AmbitoOrganismo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
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
        TipoOrganizacao tipo,
        @Schema(description = "Ecclesial scope of origin")
        AmbitoOrganismo ambito,
        @Schema(description = "Authority that recognized the statutes")
        String estatutoAutoridade,
        @Schema(description = "Date of the statutes recognition")
        LocalDate estatutoData,
        @Schema(description = "Decree, document or protocol number")
        String estatutoReferencia,
        @Schema(description = "Whether the statutes have formal recognition (Can. 299 and 304)")
        boolean estatutoReconhecido
) {
}
