package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.ReservaImpeditiva;

import java.time.Instant;
import java.util.UUID;

/** Reserva que impede o envio de um ativo para manutenção (ou sua exclusão). */
public record ReservaImpeditivaResponse(
        UUID reservaId,
        String descricao,
        Instant inicio,
        Instant fim,
        String status,
        String solicitanteNome
) {

    public static ReservaImpeditivaResponse de(ReservaImpeditiva r) {
        return new ReservaImpeditivaResponse(
                r.reservaId(), r.descricao(), r.inicio(), r.fim(), r.status(), r.solicitanteNome());
    }
}
