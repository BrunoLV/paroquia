package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Par de eventos que disputam o mesmo local físico em horários sobrepostos, no período do filtro.
 *
 * @param localNome nome do ambiente em conflito
 * @param eventoATitulo título de um dos eventos
 * @param eventoBTitulo título do outro evento
 * @param inicio início do primeiro evento do par
 */
@Schema(description = "Choque de horário de dois eventos no mesmo local")
public record ChoqueHorarioDTO(
        String localNome,
        String eventoATitulo,
        String eventoBTitulo,
        LocalDateTime inicio
) {
}
