package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.FrequenciaRecorrenciaInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Define a regra de recorrência a ser aplicada na geração de ocorrências futuras de um evento
 * (ex: "toda última sexta-feira do mês" ou "todo dia 15, por 12 meses"). A combinação de campos
 * preenchidos depende da {@code frequencia} escolhida; campos não aplicáveis à frequência informada
 * devem ser deixados nulos.
 */
public record EventoRecorrenciaRequest(
        @NotNull @Schema(description = "Frequência da recorrência") FrequenciaRecorrenciaInput frequencia,
        @Min(1) @Schema(description = "Intervalo da recorrência (ex: a cada 2 semanas)") int intervalo,
        @Schema(description = "Dias da semana (para frequência SEMANAL ou MENSAL)") List<DayOfWeek> diasDaSemana,
        @Schema(description = "Posição ordinal do dia dentro do mês (ex: LAST para a última ocorrência, ou "
                + "FIRST/SECOND/THIRD/FOURTH), usada com frequência MENSAL baseada em dia da semana")
        String posicaoNoMes,
        @Schema(description = "Posição ordinal do dia dentro do ano, usada com frequência ANUAL baseada em "
                + "dia da semana")
        String posicaoNoAno,
        @Schema(description = "Mês do ano (1-12)") Integer mesDoAno,
        @Schema(description = "Dia do mês (1-31)") Integer diaDoMes,
        @Schema(description = "Data limite da recorrência: nenhuma ocorrência é gerada após esta data")
    LocalDate dataLimite,
        @Schema(description = "Número máximo de ocorrências a gerar, como alternativa à data limite")
        Integer numeroOcorrencias) {
}
