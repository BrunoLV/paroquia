package br.com.nsfatima.gestao.governanca.domain.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Calendário simples de feriados nacionais brasileiros para contagem de dias úteis (prazo de comunicação de
 * incidentes, Art. 48 da LGPD). Cobre os feriados fixos federais e a Sexta-feira da Paixão (móvel, a partir da
 * Páscoa). Feriados estaduais e municipais e pontos facultativos (ex.: Carnaval) não são considerados.
 * Funções puras e sem estado.
 */
public final class FeriadosNacionais {

    private static final int[][] FIXOS = {
        {1, 1}, {4, 21}, {5, 1}, {9, 7}, {10, 12}, {11, 2}, {11, 15}, {11, 20}, {12, 25}
    };

    private FeriadosNacionais() {
    }

    /**
     * Indica se a data é dia útil: nem sábado, nem domingo, nem feriado nacional.
     *
     * @param data data a verificar
     * @return {@code true} se for dia útil
     */
    public static boolean ehDiaUtil(LocalDate data) {
        return data.getDayOfWeek() != DayOfWeek.SATURDAY
                && data.getDayOfWeek() != DayOfWeek.SUNDAY
                && !ehFeriado(data);
    }

    private static boolean ehFeriado(LocalDate data) {
        for (int[] fixo : FIXOS) {
            if (data.getMonthValue() == fixo[0] && data.getDayOfMonth() == fixo[1]) {
                return true;
            }
        }
        return data.equals(pascoa(data.getYear()).minusDays(2));
    }

    private static LocalDate pascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(ano, mes, dia);
    }
}
