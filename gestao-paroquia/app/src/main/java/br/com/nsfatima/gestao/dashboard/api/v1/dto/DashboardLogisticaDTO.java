package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Payload da seção de logística do dashboard: choques de horário por local, taxa de ocupação dos
 * ambientes, eventos por responsável e pendências de homologação por tipo. Todas as coleções já
 * vêm filtradas pelo recorte temporal e pelo escopo organizacional da requisição.
 */
@Schema(description = "Métricas de logística, espaços e agenda do dashboard")
public record DashboardLogisticaDTO(
        @Schema(description = "Pares de eventos em choque de horário no mesmo local")
        List<ChoqueHorarioDTO> choquesHorario,

        @Schema(description = "Taxa de ocupação estimada dos ambientes (dia e semana)")
        OcupacaoAmbientesDTO ocupacaoAmbientes,

        @Schema(description = "Eventos por organização responsável no período")
        List<EventosPorResponsavelDTO> eventosPorResponsavel,

        @Schema(description = "Pendências de homologação agrupadas por tipo")
        List<PendenciaHomologacaoDTO> pendenciasHomologacao
) {
}
