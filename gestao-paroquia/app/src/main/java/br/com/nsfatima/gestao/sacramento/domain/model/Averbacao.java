package br.com.nsfatima.gestao.sacramento.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Nota marginal append-only do assento de Batismo. A secretaria digita; o Pároco homologa.
 *
 * @param reservada anotação disciplinar/reservada, omitida de certidões simples (LGPD)
 */
public record Averbacao(
        UUID id,
        TipoEventoAverbacao tipoEvento,
        LocalDate dataAverbacao,
        String numeroDocumento,
        String teorNota,
        boolean reservada,
        String inseridaPor,
        StatusAverbacao status,
        String homologadaPor,
        Instant homologadaEmUtc) {

    public Averbacao {
        Objects.requireNonNull(id, "ID da averbação é obrigatório.");
        Objects.requireNonNull(tipoEvento, "Tipo de evento da averbação é obrigatório.");
        Objects.requireNonNull(dataAverbacao, "Data da averbação é obrigatória.");
        Objects.requireNonNull(status, "Status da averbação é obrigatório.");
        if (teorNota == null || teorNota.isBlank()) {
            throw new IllegalArgumentException("Teor da nota marginal é obrigatório.");
        }
    }

    public static Averbacao digitar(TipoEventoAverbacao tipo, LocalDate data, String numeroDocumento,
                                    String teorNota, boolean reservada, String usuario) {
        return new Averbacao(UUID.randomUUID(), tipo, data, numeroDocumento, teorNota, reservada, usuario,
                StatusAverbacao.PENDENTE_HOMOLOGACAO, null, null);
    }

    Averbacao homologar(String parocoUsuario, Instant agora) {
        if (status == StatusAverbacao.HOMOLOGADA) {
            throw new IllegalStateException("Averbação já homologada.");
        }
        return new Averbacao(id, tipoEvento, dataAverbacao, numeroDocumento, teorNota, reservada, inseridaPor,
                StatusAverbacao.HOMOLOGADA, parocoUsuario, agora);
    }
}
