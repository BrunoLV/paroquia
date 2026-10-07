package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.AmbitoOrganismo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Payload de criação/atualização de um item do catálogo oficial de Pastorais e Movimentos. O nome deve ser
 * único e o tipo restrito a {@code PASTORAL} ou {@code MOVIMENTO}, conforme validado por
 * {@code PastoralMovimentoOficialService}.
 */
@Schema(description = "Request to create or update a Pastoral or Movimento Oficial")
public record PastoralMovimentoOficialRequest(
        @Schema(description = "Official catalog name", example = "Pastoral da Criança")
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String nome,

        @Schema(description = "Type (must be PASTORAL or MOVIMENTO)")
        @NotNull(message = "Type is required")
        TipoOrganizacao tipo,

        @Schema(description = "Ecclesial scope of origin of the organism")
        AmbitoOrganismo ambito,

        @Schema(description = "Authority that recognized the statutes (required for MOVIMENTO)",
                example = "Bispo diocesano")
        @Size(max = 150, message = "Authority must not exceed 150 characters")
        String estatutoAutoridade,

        @Schema(description = "Date of the statutes recognition (not in the future)")
        LocalDate estatutoData,

        @Schema(description = "Decree, document or protocol number of the recognition")
        @Size(max = 255, message = "Reference must not exceed 255 characters")
        String estatutoReferencia
) {
}
