package br.com.nsfatima.gestao.calendario.domain.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegraRecorrenciaTest {

    @Test
    @DisplayName("Should generate dates for a weekly recurrence (Sundays)")
    void shouldGenerateWeeklyDates() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "SEMANAL",
                1,
                List.of(DayOfWeek.SUNDAY),
                null, null, null, null,
                null, null);

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
                null, null);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2028, 12, 31);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // 2026-05-13, 2027-05-13, 2028-05-13
        assertEquals(3, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 5, 13)));
        assertTrue(dates.contains(LocalDate.of(2027, 5, 13)));
        assertTrue(dates.contains(LocalDate.of(2028, 5, 13)));
    }

    @Test
    @DisplayName("Should generate dates for a daily recurrence")
    void shouldGenerateDailyDates() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "DIARIA",
                2, // Every 2 days
                null, null, null, null, null,
                null, null);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 10);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // 1, 3, 5, 7, 9
        assertEquals(5, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 1, 1)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 9)));
    }

    @Test
    @DisplayName("Should respect numeroOcorrencias limit")
    void shouldRespectCountLimit() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "DIARIA",
                1,
                null, null, null, null, null,
                null, 3); // Only 3 occurrences

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        assertEquals(3, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 1, 1)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 2)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 3)));
    }

    @Test
    @DisplayName("Should generate dates for first Friday of the month")
    void shouldGenerateFirstFridayOfMonth() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "MENSAL",
                1,
                List.of(DayOfWeek.FRIDAY),
                "FIRST", // first Friday
                null, null, null, null, null);

        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // 2026-05-01 (1st Fri), 2026-06-05 (1st Fri), 2026-07-03 (1st Fri)
        assertEquals(3, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 5, 1)));
        assertTrue(dates.contains(LocalDate.of(2026, 6, 5)));
        assertTrue(dates.contains(LocalDate.of(2026, 7, 3)));
    }

    @Test
    @DisplayName("Should generate dates for third Saturday of the month every 2 months")
    void shouldGenerateThirdSaturdayEveryTwoMonths() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "MENSAL",
                2, // every 2 months
                List.of(DayOfWeek.SATURDAY),
                "3RD", // third Saturday
                null, null, null, null, null);

        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // May 2026 3rd Saturday: May 16th (Saturdays: 2, 9, 16, 23, 30)
        // July 2026 3rd Saturday: July 18th (Saturdays: 4, 11, 18, 25)
        // September 2026 3rd Saturday: Sept 19th (Saturdays: 5, 12, 19, 26)
        assertEquals(3, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 5, 16)));
        assertTrue(dates.contains(LocalDate.of(2026, 7, 18)));
        assertTrue(dates.contains(LocalDate.of(2026, 9, 19)));
    }

    @Test
    @DisplayName("Should generate dates for last Saturday of the month using ULTIMO")
    void shouldGenerateLastSaturdayOfMonth() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "MENSAL",
                1,
                List.of(DayOfWeek.SATURDAY),
                "ULTIMO", // last Saturday
                null, null, null, null, null);

        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // May 2026 last Saturday: May 30th
        // June 2026 last Saturday: June 27th (Saturdays: 6, 13, 20, 27)
        assertEquals(2, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 5, 30)));
        assertTrue(dates.contains(LocalDate.of(2026, 6, 27)));
    }
}
