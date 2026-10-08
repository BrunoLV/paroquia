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
@Schema(description = "Dados do mandato de coordenação")
public record MandatoCoordenacaoResponse(
        @Schema(description = "Identificador único") UUID id,
        @Schema(description = "Identificador da organização") UUID organizacaoId,
        @Schema(description = "Identificador do fiel") UUID fielId,
        @Schema(description = "Nome do fiel para exibição") String fielNome,
        @Schema(description = "Coordenador ou vice-coordenador") FuncaoCoordenacao funcao,
        @Schema(description = "Primeiro dia do mandato") LocalDate inicio,
        @Schema(description = "Último dia do mandato") LocalDate fim,
        @Schema(description = "Situação canônica") StatusMandato status,
        @Schema(description = "Indica se o mandato está em vigor hoje") boolean vigente,
        @Schema(description = "Motivo do veto, quando vetado") String motivoVeto,
        @Schema(description = "Momento da última decisão") Instant decididoEmUtc
) {
}
