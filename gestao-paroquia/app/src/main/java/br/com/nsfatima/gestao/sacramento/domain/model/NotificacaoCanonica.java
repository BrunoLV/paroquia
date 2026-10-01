package br.com.nsfatima.gestao.sacramento.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Comunicação formal ao pároco do local de batismo para averbação marginal (Cânn. 895, 1054, 1122).
 * Registra emissão e, quando houver, o envio.
 */
public record NotificacaoCanonica(
        UUID id, String destinatarioParoquia, LocalDate emitidaEm, LocalDate enviadaEm, String registradaPor) {

    public NotificacaoCanonica {
        Objects.requireNonNull(id, "ID da notificação é obrigatório.");
        Objects.requireNonNull(emitidaEm, "Data de emissão da notificação é obrigatória.");
        if (destinatarioParoquia == null || destinatarioParoquia.isBlank()) {
            throw new IllegalArgumentException("Paróquia destinatária da notificação é obrigatória.");
        }
    }
}
