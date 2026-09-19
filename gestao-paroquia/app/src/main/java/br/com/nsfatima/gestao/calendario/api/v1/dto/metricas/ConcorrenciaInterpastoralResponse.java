package br.com.nsfatima.gestao.calendario.api.v1.dto.metricas;

import java.util.List;

/**
 * DTO para o indicador e alerta de Concorrência Interpastoral.
 */
public record ConcorrenciaInterpastoralResponse(
        Integer totalConflitosDetectados,
        List<ItemConflitoDTO> conflitos,
        String nivelRiscoConcorrencia
) {
}
