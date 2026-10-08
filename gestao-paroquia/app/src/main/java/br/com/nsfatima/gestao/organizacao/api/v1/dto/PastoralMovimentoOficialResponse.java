package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.AmbitoOrganismo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados públicos de um item do catálogo oficial de Pastorais e Movimentos.
 */
@Schema(description = "Dados de uma pastoral ou movimento oficial")
public record PastoralMovimentoOficialResponse(
        @Schema(description = "Identificador único")
        UUID id,
        @Schema(description = "Nome no catálogo oficial")
        String nome,
        @Schema(description = "Tipo (PASTORAL ou MOVIMENTO)")
        TipoOrganizacao tipo,
        @Schema(description = "Âmbito eclesial de origem")
        AmbitoOrganismo ambito,
        @Schema(description = "Autoridade que reconheceu os estatutos")
        String estatutoAutoridade,
        @Schema(description = "Data do reconhecimento dos estatutos")
        LocalDate estatutoData,
        @Schema(description = "Decreto, documento ou protocolo")
        String estatutoReferencia,
        @Schema(description = "Indica se os estatutos têm reconhecimento formal (Cân. 299 e 304)")
        boolean estatutoReconhecido
) {
}
