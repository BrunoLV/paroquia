package br.com.nsfatima.gestao.calendario.domain.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegraRecorrenciaTest {

    @Test
    @DisplayName("Should generate dates for a weekly recurrence (Sundays)")
    void shouldGenerateWeeklyDates() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "SEMANAL",
                1,
                List.of(DayOfWeek.SUNDAY),
                null, null, null, null,
                null);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // 2026-01-04 (Sun), 11, 18, 25
        assertEquals(4, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 1, 4)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 11)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 18)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 25)));
    }

    @Test
    @DisplayName("Should generate dates for an annual date-specific recurrence (May 13th)")
    void shouldGenerateAnnualDateSpecific() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "ANUAL",
                1,
                null,
                null,
                null,
                5,  // May
                13, // 13th
                null);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2028, 12, 31);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // 2026-05-13, 2027-05-13, 2028-05-13
        assertEquals(3, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 5, 13)));
        assertTrue(dates.contains(LocalDate.of(2027, 5, 13)));
        assertTrue(dates.contains(LocalDate.of(2028, 5, 13)));
    }
}
