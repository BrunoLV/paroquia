package br.com.nsfatima.gestao.reserva.application.service;

import java.time.Instant;
import java.util.UUID;

/**
 * Projeção pública (module API) de uma reserva que impede uma operação sobre um ativo — envio para
 * manutenção. Não expõe o agregado {@code Reserva}.
 */
public record ReservaImpeditivaView(
        UUID reservaId,
        String descricao,
        Instant inicio,
        Instant fim,
        String status,
        String solicitanteNome) {
}
