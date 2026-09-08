package br.com.nsfatima.gestao.reserva.api.v1.dto;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;

import java.time.Instant;
import java.util.UUID;

public record ConflitoDetalheResponse(
        UUID itemId,
        String itemNome,
        TipoItemReserva tipoItem,
        Instant inicioOcupado,
        Instant fimOcupadoComBuffer,
        UUID solicitanteId,
        String solicitanteNome,
        String descricaoReserva
) {
    public static ConflitoDetalheResponse de(ConflitoDetalhe d) {
        return new ConflitoDetalheResponse(
                d.itemId(),
                d.itemNome(),
                d.tipoItem(),
                d.inicioOcupado(),
                d.fimOcupadoComBuffer(),
                d.solicitanteId(),
                d.solicitanteNome(),
                d.descricaoReserva()
        );
    }
}
