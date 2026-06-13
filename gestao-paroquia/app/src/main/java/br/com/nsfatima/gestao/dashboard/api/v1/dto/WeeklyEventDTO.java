package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resumo de um evento agendado para a semana corrente")
public record WeeklyEventDTO(
    @Schema(description = "Título do evento")
    String titulo,

    @Schema(description = "Data e hora de início do evento")
    LocalDateTime inicio,

    @Schema(description = "Nome do local onde ocorrerá o evento")
    String localNome,

    @Schema(description = "Nome da organização/pastoral responsável")
    String organizacaoNome
) {}
