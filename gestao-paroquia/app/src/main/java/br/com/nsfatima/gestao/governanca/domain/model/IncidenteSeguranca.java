package br.com.nsfatima.gestao.governanca.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de domínio puro para representação de incidentes de segurança (Art. 48 LGPD).
 */
public record IncidenteSeguranca(
        UUID id,
        String titulo,
        String descricao,
        String tipoDadoAfetado,
        GravidadeIncidente categoriaGravidade,
        Instant dataOcorrencia,
        Instant dataDeteccao,
        String medidasMitigacao,
        boolean notificadoAnpd,
        Instant dataNotificacaoAnpd,
        String protocoloAnpd,
        boolean notificadoTitulares,
        Instant dataNotificacaoTitulares,
        StatusIncidente status
) {
}
