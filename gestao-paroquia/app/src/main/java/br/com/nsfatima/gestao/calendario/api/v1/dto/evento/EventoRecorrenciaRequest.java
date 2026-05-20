package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.FrequenciaRecorrenciaInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record EventoRecorrenciaRequest(
        @NotNull @Schema(description = "Frequência da recorrência") FrequenciaRecorrenciaInput frequencia,
        @Min(1) @Schema(description = "Intervalo da recorrência (ex: a cada 2 semanas)") int intervalo,
        @Schema(description = "Dias da semana (para frequência SEMANAL ou MENSAL)") List<DayOfWeek> diasDaSemana,
        @Schema(description = "Posição no mês (ex: LAST para última sexta-feira)") String posicaoNoMes,
        @Schema(description = "Posição no ano") String posicaoNoAno,
        @Schema(description = "Mês do ano (1-12)") Integer mesDoAno,
        @Schema(description = "Dia do mês (1-31)") Integer diaDoMes,
        @Schema(description = "Data limite da recorrência") LocalDate dataLimite) {
}
