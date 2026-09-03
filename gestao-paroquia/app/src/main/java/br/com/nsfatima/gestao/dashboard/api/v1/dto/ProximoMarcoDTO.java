package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Próximo evento mais crítico ainda por ocorrer de um projeto ativo — o de maior prioridade
 * (menor {@code nivelPrioridade}) e, em empate, o mais próximo no tempo. Nulo quando o projeto
 * não tem mais eventos futuros.
 *
 * @param titulo título do evento
 * @param inicio data e hora de início
 * @param nivelPrioridade prioridade do evento (1 = máxima, 4 = mínima)
 */
@Schema(description = "Próximo marco crítico de um projeto ativo")
public record ProximoMarcoDTO(
        String titulo,
        LocalDateTime inicio,
        int nivelPrioridade
) {
}
