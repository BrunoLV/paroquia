package br.com.nsfatima.gestao.reserva.api.v1.dto;

import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;

import java.time.Instant;
import java.util.UUID;

public record ReservaResponse(
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
) {
    public static ReservaResponse de(ReservaDetalheDto dto) {
        if (dto == null) return null;
        return new ReservaResponse(
                dto.id(),
                dto.tipoItem(),
                dto.itemId(),
                dto.itemNome(),
                dto.dataHoraInicio(),
                dto.dataHoraFim(),
                dto.bufferMinutos(),
                dto.fimComBuffer(),
                dto.descricao(),
                dto.solicitanteId(),
                dto.solicitanteNome(),
                dto.pastoralMovimentoId(),
                dto.pastoralMovimentoNome(),
                dto.eventoId(),
                dto.eventoTitulo(),
                dto.grupoRecorrenciaId(),
                dto.status(),
                dto.aprovadorId(),
                dto.aprovadorNome(),
                dto.aprovadoEm(),
                dto.motivoCancelamento(),
                dto.canceladoPorId(),
                dto.canceladoPorNome(),
                dto.checkAvarias(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }

    public static ReservaResponse de(Reserva r) {
        if (r == null) return null;
        return new ReservaResponse(
                r.getId().value(),
                r.getTipoItem(),
                r.getItemId(),
                null,
                r.getIntervalo().dataHoraInicio(),
                r.getIntervalo().dataHoraFim(),
                r.getIntervalo().bufferMinutos(),
                r.getIntervalo().fimComBuffer(),
                r.getDescricao(),
                r.getSolicitanteId(),
                null,
                r.getPastoralMovimentoId(),
                null,
                r.getEventoId(),
                null,
                r.getGrupoRecorrenciaId(),
                r.getStatus(),
                r.getAprovadorId(),
                null,
                r.getAprovadoEm(),
                r.getMotivoCancelamento(),
                r.getCanceladoPorId(),
                null,
                r.getCheckAvarias(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
