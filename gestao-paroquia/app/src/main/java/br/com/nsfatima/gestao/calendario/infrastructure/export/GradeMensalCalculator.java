package br.com.nsfatima.gestao.calendario.infrastructure.export;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcula a grade mensal (semanas completas, dias do mês anterior/seguinte preenchendo as bordas)
 * usada pelas exportações em modo {@code GRADE}, replicando o mesmo algoritmo de preenchimento já
 * usado no frontend ({@code useCalendarGrid.ts}: domingo como primeiro dia da semana).
 */
public final class GradeMensalCalculator {

    private GradeMensalCalculator() {
    }

    /**
     * Um dia da grade mensal, com indicação se pertence ao mês de referência ou é apenas
     * preenchimento de borda (dia do mês anterior/seguinte).
     *
     * @param data data do dia
     * @param mesAtual {@code true} se a data pertence ao mês de referência da grade
     */
    public record DiaGrade(LocalDate data, boolean mesAtual) {
    }

    /**
     * Calcula a grade completa (sempre múltiplo de 7 dias, semanas inteiras) para o mês
     * informado, com domingo como primeiro dia da semana.
     *
     * @param ano ano de referência
     * @param mes mês de referência (1 a 12)
     * @return lista de dias da grade, do primeiro domingo visível ao último sábado visível
     */
    public static List<DiaGrade> calcular(int ano, int mes) {
        YearMonth yearMonth = YearMonth.of(ano, mes);
        LocalDate primeiroDia = yearMonth.atDay(1);
        LocalDate ultimoDia = yearMonth.atEndOfMonth();

        // DayOfWeek: segunda=1 .. domingo=7; "% 7" normaliza para domingo=0 (primeiro dia da semana).
        int diaSemanaInicio = primeiroDia.getDayOfWeek().getValue() % 7;

        List<DiaGrade> dias = new ArrayList<>();

        for (int i = diaSemanaInicio; i > 0; i--) {
            dias.add(new DiaGrade(primeiroDia.minusDays(i), false));
        }

        for (LocalDate d = primeiroDia; !d.isAfter(ultimoDia); d = d.plusDays(1)) {
            dias.add(new DiaGrade(d, true));
        }

        while (dias.size() % 7 != 0) {
            LocalDate ultimaData = dias.get(dias.size() - 1).data();
            dias.add(new DiaGrade(ultimaData.plusDays(1), false));
        }

        return dias;
    }
}
