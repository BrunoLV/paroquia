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
@Schema(description = "Requisição para criar ou atualizar uma pastoral ou movimento oficial")
public record PastoralMovimentoOficialRequest(
        @Schema(description = "Nome no catálogo oficial", example = "Pastoral da Criança")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
        String nome,

        @Schema(description = "Tipo (deve ser PASTORAL ou MOVIMENTO)")
        @NotNull(message = "O tipo é obrigatório")
        TipoOrganizacao tipo,

        @Schema(description = "Âmbito eclesial de origem do organismo")
        AmbitoOrganismo ambito,

        @Schema(description = "Autoridade que reconheceu os estatutos (obrigatória para MOVIMENTO)",
                example = "Bispo diocesano")
        @Size(max = 150, message = "A autoridade não pode exceder 150 caracteres")
        String estatutoAutoridade,

        @Schema(description = "Data do reconhecimento dos estatutos (não pode ser futura)")
        LocalDate estatutoData,

        @Schema(description = "Decreto, documento ou protocolo do reconhecimento")
        @Size(max = 255, message = "A referência não pode exceder 255 caracteres")
        String estatutoReferencia
) {
}
