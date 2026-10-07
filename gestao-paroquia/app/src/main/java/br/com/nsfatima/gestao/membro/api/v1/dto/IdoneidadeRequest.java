package br.com.nsfatima.gestao.membro.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Registro de idoneidade pastoral do agente. Contém apenas datas; o conteúdo dos documentos nunca trafega.
 * O responsável pelo envio é o próprio ator autenticado que registra a data de envio.
 */
@Schema(description = "Pastoral suitability record of an agent (dates only)")
public record IdoneidadeRequest(
        @Schema(description = "Completion date of the basic doctrinal formation")
        LocalDate formacaoConcluidaEm,
        @Schema(description = "Date the pastor attested the sacramental life")
        LocalDate vivenciaSacramentalAtestadaEm,
        @Schema(description = "Signing date of the minors and vulnerable people protection pledge")
        LocalDate termoProtecaoMenoresEm,
        @Schema(description = "Last valid day of the protection pledge; empty means valid since signing")
        LocalDate termoProtecaoMenoresValidoAte,
        @Schema(description = "Date of the sending (mission) conferred by the pastor")
        LocalDate enviadoEm
) {
}
