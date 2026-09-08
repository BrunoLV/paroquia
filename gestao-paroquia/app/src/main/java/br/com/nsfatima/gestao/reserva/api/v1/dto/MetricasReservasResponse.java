package br.com.nsfatima.gestao.reserva.api.v1.dto;

import br.com.nsfatima.gestao.reserva.application.dto.MetricasReservasDto;

public record MetricasReservasResponse(
        long pendentesAprovacao,
        long devolucoesPendentesHoje,
        long ativasHoje,
        long totalMesAtual
) {
    public static MetricasReservasResponse de(MetricasReservasDto dto) {
        return new MetricasReservasResponse(
                dto.pendentesAprovacao(),
                dto.devolucoesPendentesHoje(),
                dto.ativasHoje(),
                dto.totalMesAtual()
        );
    }
}
