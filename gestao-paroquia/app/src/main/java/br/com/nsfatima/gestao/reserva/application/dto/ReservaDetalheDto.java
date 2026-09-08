package br.com.nsfatima.gestao.reserva.application.dto;

import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;

import java.time.Instant;
import java.util.UUID;

public record ReservaDetalheDto(
        UUID id,
        TipoItemReserva tipoItem,
        UUID itemId,
        String itemNome,
        Instant dataHoraInicio,
        Instant dataHoraFim,
        int bufferMinutos,
        Instant fimComBuffer,
        String descricao,
        UUID solicitanteId,
        String solicitanteNome,
        UUID pastoralMovimentoId,
        String pastoralMovimentoNome,
        UUID eventoId,
        String eventoTitulo,
        UUID grupoRecorrenciaId,
        StatusReserva status,
        UUID aprovadorId,
        String aprovadorNome,
        Instant aprovadoEm,
        String motivoCancelamento,
        UUID canceladoPorId,
        String canceladoPorNome,
        String checkAvarias,
        Instant createdAt,
        Instant updatedAt
) {}
