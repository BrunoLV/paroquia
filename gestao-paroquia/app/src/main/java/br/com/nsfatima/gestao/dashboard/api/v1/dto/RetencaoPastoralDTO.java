package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Pastoral/movimento em situação de atenção quanto à retenção: número crítico de voluntários
 * ativos e/ou sem liderança designada.
 *
 * @param grupoId identificador do grupo
 * @param nome nome do grupo
 * @param totalVoluntarios voluntários com vínculo ativo
 * @param temLideranca há coordenador designado
 * @param motivos motivos do alerta (ex.: "Menos de 3 voluntários", "Sem liderança designada")
 */
@Schema(description = "Pastoral em situação de atenção quanto à retenção de membros")
public record RetencaoPastoralDTO(
        UUID grupoId,
        String nome,
        long totalVoluntarios,
        boolean temLideranca,
        List<String> motivos
) {
}
