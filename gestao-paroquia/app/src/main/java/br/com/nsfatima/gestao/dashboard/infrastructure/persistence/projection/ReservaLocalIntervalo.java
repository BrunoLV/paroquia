package br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection;

import java.time.Instant;
import java.util.UUID;

/**
 * Intervalo reservado de um local, base do cálculo de ocupação de ambientes.
 *
 * @param localId local reservado
 * @param inicio início da reserva
 * @param fim fim da reserva
 */
public record ReservaLocalIntervalo(UUID localId, Instant inicio, Instant fim) {
}
