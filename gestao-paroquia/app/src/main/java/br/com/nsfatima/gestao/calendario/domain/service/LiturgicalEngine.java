package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.CorLiturgica;
import br.com.nsfatima.gestao.calendario.domain.model.LiturgicalOutput;
import br.com.nsfatima.gestao.calendario.domain.model.MarcoLiturgico;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public LocalDate calculateQuartaDeCinzas(int year) {
        return calculateEaster(year).minusDays(46);
    }

    public LocalDate calculateQuartaSanta(int year) {
        return calculateEaster(year).minusDays(4);
    }

    public LocalDate calculateQuintaSanta(int year) {
        return calculateEaster(year).minusDays(3);
    }

    public LocalDate calculateVesperaPascoa(int year) {
        return calculateEaster(year).minusDays(1);
    }

    public LocalDate calculateAscensao(int year) {
        return calculateEaster(year).plusDays(42);
    }

    public LocalDate calculatePentecostes(int year) {
        return calculateEaster(year).plusDays(49);
    }

    public LocalDate calculateSantissimaTrindade(int year) {
        return calculateEaster(year).plusDays(56);
    }

    public LocalDate calculateCorpusChristi(int year) {
        return calculateEaster(year).plusDays(60);
    }

    public LocalDate calculateSagradoCoracao(int year) {
        return calculateEaster(year).plusDays(68);
    }

    /**
     * Calcula o 1º Domingo do Advento do ano civil anterior (início do anoLiturgico).
     */
    public ZonedDateTime calculateAdventoInicio(int liturgicalYear) {
        return calculateAdventoSundayDate(liturgicalYear - 1).atStartOfDay(ZoneOffset.UTC);
    }

    /**
     * Encontra o domingo mais próximo a 30 de novembro do ano especificado.
     */
    private LocalDate calculateAdventoSundayDate(int year) {
        LocalDate baseDate = LocalDate.of(year, 11, 30);
        DayOfWeek dayOfWeek = baseDate.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return baseDate;
        } else if (dayOfWeek.getValue() <= 3) {
            return baseDate.minusDays(dayOfWeek.getValue());
        } else {
            return baseDate.plusDays(7 - dayOfWeek.getValue());
        }
    }

    public LocalDate calculateCristoRei(int year) {
        LocalDate adventoCurrent = calculateAdventoSundayDate(year);
        return adventoCurrent.minusDays(7);
    }

    public LocalDate calculateEpifania(int year) {
        for (int day = 2; day <= 8; day++) {
            LocalDate d = LocalDate.of(year, 1, day);
            if (d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return d;
            }
        }
        return LocalDate.of(year, 1, 6);
    }

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
     * Resolve o ciclo trienal dominical (A, B ou C) com base na regra universal do ano nominal.
     * anoNominal % 3 == 1 -> A
     * anoNominal % 3 == 2 -> B
     * anoNominal % 3 == 0 -> C
     */
    public String resolveCicloLetra(int liturgicalYear) {
        int rem = Math.abs(liturgicalYear) % 3;
        return switch (rem) {
            case 1 -> "A";
            case 2 -> "B";
            case 0 -> "C";
            default -> "C";
        };
    }

    /**
     * Calcula o ano litúrgico completo (LiturgicalOutput) de forma stateless e imutável.
     */
    public LiturgicalOutput calculateLiturgicalYear(int anoNominal) {
        LocalDate dataInicio = calculateAdventoSundayDate(anoNominal - 1);
        LocalDate dataFim = calculateAdventoSundayDate(anoNominal).minusDays(1);
        String cicloLetra = resolveCicloLetra(anoNominal);

        Map<String, MarcoLiturgico> marcos = new LinkedHashMap<>();

        // Marcos móveis
        marcos.put("QUARTA_CINZAS", new MarcoLiturgico("QUARTA_CINZAS", "Quarta-feira de Cinzas", calculateQuartaDeCinzas(anoNominal), CorLiturgica.ROXO, 2.50));
        marcos.put("QUARTA_SANTA", new MarcoLiturgico("QUARTA_SANTA", "Quarta-feira Santa", calculateQuartaSanta(anoNominal), CorLiturgica.ROXO, 2.00));
        marcos.put("QUINTA_SANTA", new MarcoLiturgico("QUINTA_SANTA", "Quinta-feira Santa", calculateQuintaSanta(anoNominal), CorLiturgica.BRANCO, 3.00));
        marcos.put("SABADO_SANTO", new MarcoLiturgico("SABADO_SANTO", "Sábado Santo", calculateVesperaPascoa(anoNominal), CorLiturgica.BRANCO, 3.00));
        marcos.put("PASCOA", new MarcoLiturgico("PASCOA", "Domingo de Páscoa", calculateEaster(anoNominal), CorLiturgica.BRANCO, 3.00));
        marcos.put("ASCENSAO", new MarcoLiturgico("ASCENSAO", "Ascensão do Senhor", calculateAscensao(anoNominal), CorLiturgica.BRANCO, 2.50));
        marcos.put("PENTECOSTES", new MarcoLiturgico("PENTECOSTES", "Domingo de Pentecostes", calculatePentecostes(anoNominal), CorLiturgica.VERMELHO, 3.00));
        marcos.put("SANTISSIMA_TRINDADE", new MarcoLiturgico("SANTISSIMA_TRINDADE", "Santíssima Trindade", calculateSantissimaTrindade(anoNominal), CorLiturgica.BRANCO, 2.50));
        marcos.put("CORPUS_CHRISTI", new MarcoLiturgico("CORPUS_CHRISTI", "Corpus Christi", calculateCorpusChristi(anoNominal), CorLiturgica.BRANCO, 3.00));
        marcos.put("SAGRADO_CORACAO", new MarcoLiturgico("SAGRADO_CORACAO", "Sagrado Coração de Jesus", calculateSagradoCoracao(anoNominal), CorLiturgica.BRANCO, 2.50));
        marcos.put("CRISTO_REI", new MarcoLiturgico("CRISTO_REI", "Nosso Senhor Jesus Cristo, Rei do Universo", calculateCristoRei(anoNominal), CorLiturgica.BRANCO, 2.50));
        marcos.put("EPIFANIA", new MarcoLiturgico("EPIFANIA", "Epifania do Senhor", calculateEpifania(anoNominal), CorLiturgica.BRANCO, 2.50));

        // Solenidades fixas (Santoral)
        marcos.put("MAE_DE_DEUS", new MarcoLiturgico("MAE_DE_DEUS", "Santa Maria, Mãe de Deus", LocalDate.of(anoNominal, 1, 1), CorLiturgica.BRANCO, 3.00));
        marcos.put("SAO_JOSE", new MarcoLiturgico("SAO_JOSE", "São José, Esposo da Virgem Maria", LocalDate.of(anoNominal, 3, 19), CorLiturgica.BRANCO, 3.00));
        marcos.put("ANUNCIACAO_SENHOR", new MarcoLiturgico("ANUNCIACAO_SENHOR", "Anunciação do Senhor", LocalDate.of(anoNominal, 3, 25), CorLiturgica.BRANCO, 3.00));
        marcos.put("NATIVIDADE_JOAO_BATISTA", new MarcoLiturgico("NATIVIDADE_JOAO_BATISTA", "Natividade de São João Batista", LocalDate.of(anoNominal, 6, 24), CorLiturgica.BRANCO, 3.00));
        marcos.put("SAO_PEDRO_SAO_PAULO", new MarcoLiturgico("SAO_PEDRO_SAO_PAULO", "São Pedro e São Paulo, Apóstolos", LocalDate.of(anoNominal, 6, 29), CorLiturgica.VERMELHO, 3.00));
        marcos.put("ASSUNCAO_NOSSA_SENHORA", new MarcoLiturgico("ASSUNCAO_NOSSA_SENHORA", "Assunção de Nossa Senhora", LocalDate.of(anoNominal, 8, 15), CorLiturgica.BRANCO, 3.00));
        marcos.put("TODOS_OS_SANTOS", new MarcoLiturgico("TODOS_OS_SANTOS", "Todos os Santos", LocalDate.of(anoNominal, 11, 1), CorLiturgica.BRANCO, 3.00));
        marcos.put("IMACULADA_CONCEICAO", new MarcoLiturgico("IMACULADA_CONCEICAO", "Imaculada Conceição de Nossa Senhora", LocalDate.of(anoNominal, 12, 8), CorLiturgica.BRANCO, 3.00));
        marcos.put("NATAL_SENHOR", new MarcoLiturgico("NATAL_SENHOR", "Natal do Senhor", LocalDate.of(anoNominal, 12, 25), CorLiturgica.BRANCO, 3.00));

        return new LiturgicalOutput(anoNominal, dataInicio, dataFim, cicloLetra, Collections.unmodifiableMap(marcos));
    }
}
