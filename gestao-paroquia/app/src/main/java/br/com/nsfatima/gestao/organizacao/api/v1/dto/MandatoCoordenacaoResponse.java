package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.FuncaoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.StatusMandato;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados públicos de um mandato de coordenação, incluindo o nome do fiel apenas para exibição.
 */
@Schema(description = "Coordination mandate data")
public record MandatoCoordenacaoResponse(
        @Schema(description = "Unique identifier") UUID id,
        @Schema(description = "Organization identifier") UUID organizacaoId,
        @Schema(description = "Faithful identifier") UUID fielId,
        @Schema(description = "Faithful display name") String fielNome,
        @Schema(description = "Coordinator or vice-coordinator") FuncaoCoordenacao funcao,
        @Schema(description = "First day of the mandate") LocalDate inicio,
        @Schema(description = "Last day of the mandate") LocalDate fim,
        @Schema(description = "Canonical status") StatusMandato status,
        @Schema(description = "Whether the mandate is in force today") boolean vigente,
        @Schema(description = "Reason for the veto, when vetoed") String motivoVeto,
        @Schema(description = "Moment of the last decision") Instant decididoEmUtc
) {
}
