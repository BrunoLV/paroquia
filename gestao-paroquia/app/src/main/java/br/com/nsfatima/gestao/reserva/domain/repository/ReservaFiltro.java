package br.com.nsfatima.gestao.reserva.domain.repository;

import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;

import java.time.Instant;
import java.util.UUID;

/**
 * Filtro de consulta para listagem de reservas paroquiais.
 */
public record ReservaFiltro(
        TipoItemReserva tipoItem,
        UUID itemId,
        StatusReserva status,
        UUID pastoralMovimentoId,
        UUID solicitanteId,
        UUID eventoId,
        Instant dataInicio,
        Instant dataFim,
        String termoBusca
) {
    public static ReservaFiltro vazio() {
        return new ReservaFiltro(null, null, null, null, null, null, null, null, null);
    }
}
