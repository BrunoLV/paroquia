package br.com.nsfatima.gestao.governanca.api.v1.dto;

import br.com.nsfatima.gestao.governanca.domain.model.StatusIncidente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Payload de requisição para atualização de status e comunicação à ANPD e aos titulares.
 */
@Schema(description = "Requisição para atualização de status e despacho de incidente de segurança")
public record AtualizarStatusIncidenteRequest(
        @Schema(description = "Novo status do incidente", example = "NOTIFICADO")
        @NotNull(message = "Status é obrigatório.")
        StatusIncidente status,

        @Schema(description = "Indica se a ANPD foi comunicada", example = "true")
        Boolean notificadoAnpd,

        @Schema(description = "Data e hora do envio da comunicação à ANPD")
        Instant dataNotificacaoAnpd,

        @Schema(description = "Protocolo oficial emitido pela ANPD", example = "ANPD-2026-987654")
        @Size(max = 100, message = "Protocolo ANPD deve ter no máximo 100 caracteres.")
        String protocoloAnpd,

        @Schema(description = "Indica se os titulares afetados foram comunicados diretamente", example = "true")
        Boolean notificadoTitulares,

        @Schema(description = "Data e hora da notificação aos titulares")
        Instant dataNotificacaoTitulares,

        @Schema(description = "Atualização das medidas de mitigação e parecer do DPO")
        String medidasMitigacao
) {
}
