package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import java.util.List;

/**
 * DTO para o indicador e alerta de Concorrência Interpastoral.
 */
public record ConcorrenciaInterpastoralResponse(
        Integer totalConflitosDetectados,
        List<ConflitoItemDto> conflitos,
        String nivelRiscoConcorrencia
) {
}
