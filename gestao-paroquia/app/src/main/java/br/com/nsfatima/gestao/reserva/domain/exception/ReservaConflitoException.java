package br.com.nsfatima.gestao.reserva.domain.exception;

import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReservaConflitoException extends RuntimeException {

    public record ConflitoDetalhe(
            UUID itemId,
            String itemNome,
            TipoItemReserva tipoItem,
            Instant inicioOcupado,
            Instant fimOcupadoComBuffer,
            UUID solicitanteId,
            String solicitanteNome,
            String descricaoReserva
    ) {}

    private final List<ConflitoDetalhe> conflitos;

    public ReservaConflitoException(String message, List<ConflitoDetalhe> conflitos) {
        super(message);
        this.conflitos = conflitos != null ? List.copyOf(conflitos) : List.of();
    }

    public List<ConflitoDetalhe> getConflitos() {
        return conflitos;
    }
}
