package br.com.nsfatima.gestao.membro.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Registro de idoneidade pastoral do agente. Contém apenas datas; o conteúdo dos documentos nunca trafega.
 * O responsável pelo envio é o próprio ator autenticado que registra a data de envio.
 */
@Schema(description = "Registro de idoneidade pastoral de um agente (somente datas)")
public record IdoneidadeRequest(
        @Schema(description = "Data de conclusão da formação doutrinária básica")
        LocalDate formacaoConcluidaEm,
        @Schema(description = "Data em que o Pároco atestou a vivência sacramental")
        LocalDate vivenciaSacramentalAtestadaEm,
        @Schema(description = "Data de assinatura do termo de proteção de menores e vulneráveis")
        LocalDate termoProtecaoMenoresEm,
        @Schema(description = "Último dia de validade do termo de proteção; vazio significa válido desde a assinatura")
        LocalDate termoProtecaoMenoresValidoAte,
        @Schema(description = "Data do envio (missão) conferido pelo Pároco")
        LocalDate enviadoEm
) {
}
