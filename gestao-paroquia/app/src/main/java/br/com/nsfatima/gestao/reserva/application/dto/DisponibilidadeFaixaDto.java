package br.com.nsfatima.gestao.reserva.application.dto;

import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;

import java.time.Instant;
import java.util.UUID;

public record DisponibilidadeFaixaDto(
        UUID reservaId,
        Instant inicio,
        Instant fim,
        Instant fimComBuffer,
        int bufferMinutos,
        StatusReserva status,
        String responsavel,
        String descricao
) {}
