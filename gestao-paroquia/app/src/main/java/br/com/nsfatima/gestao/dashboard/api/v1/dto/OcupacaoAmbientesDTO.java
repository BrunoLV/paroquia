package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Taxa de ocupação dos ambientes da paróquia no dia corrente e na semana. É uma <em>estimativa</em>:
 * baseia-se somente em eventos com local vinculado e numa janela operacional fixa por local; não há
 * entidade de reserva de espaço.
 *
 * @param taxaDiaPercentual percentual (0-100) de ocupação do dia corrente
 * @param taxaSemanaPercentual percentual (0-100) de ocupação da semana corrente
 * @param porLocal ocupação semanal por ambiente, do mais ocupado ao menos
 */
@Schema(description = "Taxa de ocupação estimada dos ambientes (dia e semana)")
public record OcupacaoAmbientesDTO(
        double taxaDiaPercentual,
        double taxaSemanaPercentual,
        List<OcupacaoLocalDTO> porLocal
) {
}
