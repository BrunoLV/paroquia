package br.com.nsfatima.gestao.reserva.application.dto;

public record MetricasReservasDto(
        long pendentesAprovacao,
        long devolucoesPendentesHoje,
        long ativasHoje,
        long totalMesAtual
) {}
