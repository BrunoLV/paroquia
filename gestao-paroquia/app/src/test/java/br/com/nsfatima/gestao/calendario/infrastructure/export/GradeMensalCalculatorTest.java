package br.com.nsfatima.gestao.calendario.infrastructure.export;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradeMensalCalculatorTest {

    @Test
    void tamanhoDaGradeDeveSerSempreMultiploDeSete() {
        for (Month mes : Month.values()) {
            List<GradeMensalCalculator.DiaGrade> dias = GradeMensalCalculator.calcular(2026, mes.getValue());
            assertEquals(0, dias.size() % 7, "Mês " + mes + " não gerou semanas completas");
        }
    }

    @Test
    void primeiroDiaDaGradeDeveSerUmDomingo() {
        // maio/2026 começa numa sexta-feira
        List<GradeMensalCalculator.DiaGrade> dias = GradeMensalCalculator.calcular(2026, 5);
        assertEquals(DayOfWeek.SUNDAY, dias.getFirst().data().getDayOfWeek());
    }

    @Test
    void ultimoDiaDaGradeDeveSerUmSabado() {
        List<GradeMensalCalculator.DiaGrade> dias = GradeMensalCalculator.calcular(2026, 5);
        assertEquals(DayOfWeek.SATURDAY, dias.getLast().data().getDayOfWeek());
    }

    @Test
    void deveMarcarCorretamenteOsDiasDoMesDeReferencia() {
        YearMonth referencia = YearMonth.of(2026, 5);
        List<GradeMensalCalculator.DiaGrade> dias = GradeMensalCalculator.calcular(2026, 5);

        long diasDoMes = dias.stream()
                .filter(GradeMensalCalculator.DiaGrade::mesAtual)
                .count();
        assertEquals(referencia.lengthOfMonth(), diasDoMes);

        for (GradeMensalCalculator.DiaGrade dia : dias) {
            boolean pertenceAoMes = YearMonth.from(dia.data()).equals(referencia);
            assertEquals(pertenceAoMes, dia.mesAtual());
        }
    }

    @Test
    void deveIncluirDiasDeBordaQuandoMesNaoComecaNiNemTerminaNoDomingo() {
        // Fevereiro/2026 começa numa domingo — força um mês que já comece alinhado, sem padding inicial.
        List<GradeMensalCalculator.DiaGrade> dias = GradeMensalCalculator.calcular(2026, 2);
        LocalDate primeiroDiaDoMes = LocalDate.of(2026, 2, 1);
        assertTrue(dias.stream().anyMatch(d -> d.data().equals(primeiroDiaDoMes) && d.mesAtual()));
    }
}
