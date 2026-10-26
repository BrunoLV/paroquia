package br.com.nsfatima.gestao.governanca.api.v1.dto;

import br.com.nsfatima.gestao.governanca.domain.model.GravidadeIncidente;
import br.com.nsfatima.gestao.governanca.domain.model.IncidenteSeguranca;
import br.com.nsfatima.gestao.governanca.domain.model.StatusIncidente;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Resposta de consulta estruturada de um incidente de segurança.
 */
@Schema(description = "Dados estruturados de um incidente de segurança registrado")
public record IncidenteSegurancaResponse(
        @Schema(description = "Identificador único do incidente")
        UUID id,

        @Schema(description = "Título da ocorrência")
        String titulo,

        @Schema(description = "Descrição detalhada")
        String descricao,

        @Schema(description = "Natureza dos dados pessoais afetados")
        String tipoDadoAfetado,

        @Schema(description = "Gravidade")
        GravidadeIncidente categoriaGravidade,

        @Schema(description = "Data e hora da ocorrência em UTC")
        Instant dataOcorrencia,

        @Schema(description = "Data e hora da detecção em UTC")
        Instant dataDeteccao,

        @Schema(description = "Medidas de mitigação adotadas")
        String medidasMitigacao,

        @Schema(description = "Se a ANPD foi notificada")
        boolean notificadoAnpd,

        @Schema(description = "Data da notificação à ANPD")
        Instant dataNotificacaoAnpd,

        @Schema(description = "Protocolo oficial na ANPD")
        String protocoloAnpd,

        @Schema(description = "Se os titulares foram notificados")
        boolean notificadoTitulares,

        @Schema(description = "Data da notificação aos titulares")
        Instant dataNotificacaoTitulares,

        @Schema(description = "Status do tratamento")
        StatusIncidente status,

        @Schema(description = "Data de registro em UTC")
        Instant criadoEmUtc,
        @Schema(description = "Prazo de comunicação à ANPD e aos titulares: 3 dias úteis após a detecção")
        Instant prazoComunicacaoUtc,
        @Schema(description = "Verdadeiro se o prazo de comunicação venceu sem notificação à ANPD nem encerramento")
        boolean prazoVencido
) {
    public static IncidenteSegurancaResponse de(IncidenteSeguranca incidente, Instant criadoEmUtc, Instant agora) {
        return new IncidenteSegurancaResponse(
                incidente.id(),
                incidente.titulo(),
                incidente.descricao(),
                incidente.tipoDadoAfetado(),
                incidente.categoriaGravidade(),
                incidente.dataOcorrencia(),
                incidente.dataDeteccao(),
                incidente.medidasMitigacao(),
                incidente.notificadoAnpd(),
                incidente.dataNotificacaoAnpd(),
                incidente.protocoloAnpd(),
                incidente.notificadoTitulares(),
                incidente.dataNotificacaoTitulares(),
                incidente.status(),
                criadoEmUtc,
                incidente.prazoComunicacao(),
                incidente.prazoVencido(agora)
        );
    }
}
