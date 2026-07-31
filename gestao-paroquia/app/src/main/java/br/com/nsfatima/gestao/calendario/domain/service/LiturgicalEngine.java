package br.com.nsfatima.gestao.calendario.domain.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class LiturgicalEngine {

    /**
     * Calcula a data da Páscoa usando o Algoritmo de Meeus/Jones/Butcher.
     */
    public LocalDate calculateEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

    /**
     * Início da Quaresma (Quarta-feira de Cinzas = Páscoa - 46 dias).
     */
    public LocalDate calculateQuartaDeCinzas(int year) {
        return calculateEaster(year).minusDays(46);
    }

    /**
     * Fim da Quaresma (Sábado Santo = Páscoa - 1 dia).
     */
    public LocalDate calculateVesperaPascoa(int year) {
        return calculateEaster(year).minusDays(1);
    }

    /**
     * Fim do Tempo Pascal (Domingo de Pentecostes = Páscoa + 49 dias / 50º dia).
     */
    public LocalDate calculatePentecostes(int year) {
        return calculateEaster(year).plusDays(49);
    }

    /**
     * Calcula o início do Advento (1º Domingo do Advento) para o Ano Litúrgico nominal.
     * O 1º Domingo do Advento é o domingo mais próximo de 30 de novembro do ano civil anterior (anoLiturgico - 1).
     */
    public ZonedDateTime calculateAdventoInicio(int liturgicalYear) {
        LocalDate baseDate = LocalDate.of(liturgicalYear - 1, 11, 30);
        DayOfWeek dayOfWeek = baseDate.getDayOfWeek();

        LocalDate adventoSunday;
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            adventoSunday = baseDate;
        } else if (dayOfWeek.getValue() <= 3) {
            // Qui, Sex, Sáb vão para o próximo domingo? Não, Seg(1), Ter(2), Quarta(3) voltam para o domingo anterior (27, 28, 29 nov)
            adventoSunday = baseDate.minusDays(dayOfWeek.getValue());
        } else {
            // Qui(4), Sex(5), Sáb(6) vão para o domingo seguinte (1, 2, 3 dez)
            adventoSunday = baseDate.plusDays(7 - dayOfWeek.getValue());
        }

        return adventoSunday.atStartOfDay(ZoneOffset.UTC);
    }

    /**
     * Resolve o Ano Litúrgico nominal para uma determinada data UTC.
     * Se a data for igual ou posterior ao 1º Domingo do Advento do ano civil corrente,
     * pertence ao Ano Litúrgico do ano civil seguinte.
     */
    public int resolveAnoLiturgico(Instant eventDate) {
        if (eventDate == null) {
            return LocalDate.now().getYear();
        }
        ZonedDateTime zdt = eventDate.atZone(ZoneOffset.UTC);
        int anoCivil = zdt.getYear();

        ZonedDateTime inicioAdventoProximoAnoLiturgico = calculateAdventoInicio(anoCivil + 1);

        if (zdt.isAfter(inicioAdventoProximoAnoLiturgico) || zdt.isEqual(inicioAdventoProximoAnoLiturgico)) {
            return anoCivil + 1;
        }
        return anoCivil;
    }

    /**
     * Resolve o ciclo trienal da leitura dominical (A, B ou C) com base no ano litúrgico.
     * Anos divisíveis por 3 após resto:
     * (ano - 1) % 3: 0 -> A, 1 -> B, 2 -> C
     * Exemplo: 2026 -> (2026 - 1) = 2025 % 3 = 0 => 2026 é C?
     * Padrão eclesial:
     * 2023 = A (2023 % 3 = 2 => A)
     * 2024 = B (2024 % 3 = 0 => B)
     * 2025 = C (2025 % 3 = 1 => C)
     * 2026 = C (2026 % 3 = 2 => C)
     * 2027 = A (2027 % 3 = 0 => A)
     * 2028 = B (2028 % 3 = 1 => B)
     */
    public String resolveCicloLetra(int liturgicalYear) {
        int remainder = (liturgicalYear - 2026) % 3;
        if (remainder < 0) {
            remainder += 3;
        }
        switch (remainder) {
            case 0:
                return "C";
            case 1:
                return "A";
            case 2:
                return "B";
            default:
                return "C";
        }
    }
}
