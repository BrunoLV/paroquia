package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.FuncaoCoordenacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Proposta de mandato de coordenação, sujeita à homologação do Pároco. O período respeita a duração máxima
 * definida em {@code PoliticaDuracaoMandato}.
 */
@Schema(description = "Proposal of a coordination mandate")
public record MandatoCoordenacaoRequest(
        @Schema(description = "Faithful proposed for the role")
        @NotNull(message = "Faithful is required")
        UUID fielId,

        @Schema(description = "Coordinator or vice-coordinator")
        @NotNull(message = "Role is required")
        FuncaoCoordenacao funcao,

        @Schema(description = "First day of the mandate")
        @NotNull(message = "Start date is required")
        LocalDate inicio,

        @Schema(description = "Last day of the mandate")
        @NotNull(message = "End date is required")
        LocalDate fim
) {
}
