package br.com.nsfatima.gestao.membro.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * Idoneidade pastoral do agente e as pendências que o impedem de exercer a função que ocupa.
 */
@Schema(description = "Pastoral suitability of an agent and its pending requirements")
public record IdoneidadeResponse(
        LocalDate formacaoConcluidaEm,
        LocalDate vivenciaSacramentalAtestadaEm,
        LocalDate termoProtecaoMenoresEm,
        LocalDate termoProtecaoMenoresValidoAte,
        LocalDate enviadoEm,
        String enviadoPor,
        @Schema(description = "Whether the agent has no pending requirement for the current role")
        boolean apto,
        @Schema(description = "Pending requirements for the current role")
        List<String> pendencias
) {
}
