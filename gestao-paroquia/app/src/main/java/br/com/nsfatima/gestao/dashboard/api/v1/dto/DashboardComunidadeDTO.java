package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Seção de vida comunitária do dashboard: pastorais em atenção quanto à retenção de membros.
 * O engajamento agregado (fiéis com/sem vínculo) já vem no resumo geral do endpoint de métricas.
 */
@Schema(description = "Métricas de vida comunitária e engajamento pastoral")
public record DashboardComunidadeDTO(
        @Schema(description = "Pastorais com número crítico de voluntários ou sem liderança")
        List<RetencaoPastoralDTO> retencaoPastorais
) {
}
