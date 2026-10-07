package br.com.nsfatima.gestao.membro.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * Idoneidade pastoral do agente e as pendências que o impedem de exercer a função que ocupa.
 */
@Schema(description = "Idoneidade pastoral de um agente e suas pendências")
public record IdoneidadeResponse(
        LocalDate formacaoConcluidaEm,
        LocalDate vivenciaSacramentalAtestadaEm,
        LocalDate termoProtecaoMenoresEm,
        LocalDate termoProtecaoMenoresValidoAte,
        LocalDate enviadoEm,
        String enviadoPor,
        @Schema(description = "Indica se o agente não tem pendências para a função atual")
        boolean apto,
        @Schema(description = "Pendências para a função atual")
        List<String> pendencias
) {
}
