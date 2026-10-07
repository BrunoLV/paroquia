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
@Schema(description = "Proposta de mandato de coordenação")
public record MandatoCoordenacaoRequest(
        @Schema(description = "Fiel proposto para a função")
        @NotNull(message = "O fiel é obrigatório")
        UUID fielId,

        @Schema(description = "Coordenador ou vice-coordenador")
        @NotNull(message = "A função é obrigatória")
        FuncaoCoordenacao funcao,

        @Schema(description = "Primeiro dia do mandato")
        @NotNull(message = "A data de início é obrigatória")
        LocalDate inicio,

        @Schema(description = "Último dia do mandato")
        @NotNull(message = "A data de término é obrigatória")
        LocalDate fim
) {
}
