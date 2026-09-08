package br.com.nsfatima.gestao.reserva.api.v1.dto;

import br.com.nsfatima.gestao.reserva.application.dto.DisponibilidadeFaixaDto;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;

import java.time.Instant;
import java.util.UUID;

public record DisponibilidadeFaixaResponse(
        UUID reservaId,
        Instant inicio,
        Instant fim,
        Instant fimComBuffer,
        int bufferMinutos,
        StatusReserva status,
        String responsavel,
        String descricao
) {
    public static DisponibilidadeFaixaResponse de(DisponibilidadeFaixaDto dto) {
        return new DisponibilidadeFaixaResponse(
                dto.reservaId(),
                dto.inicio(),
                dto.fim(),
                dto.fimComBuffer(),
                dto.bufferMinutos(),
                dto.status(),
                dto.responsavel(),
                dto.descricao()
        );
    }
}
