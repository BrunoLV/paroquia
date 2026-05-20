package br.com.nsfatima.gestao.calendario.domain.type;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Record representing the recurrence rules for an event.
 */
public record RegraRecorrencia(
        String frequencia,
        int intervalo,
        List<DayOfWeek> diasDaSemana,
        String posicaoNoMes,
        String posicaoNoAno, // e.g., "LAST" for monthly patterns
        Integer mesDoAno,    // 1-12
        Integer diaDoMes,    // 1-31
        LocalDate dataLimite) {

    /**
     * Generates a list of dates between the specified range based on the recurrence rules.
     */
    public List<LocalDate> gerarDatas(LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate atual = inicio;
        LocalDate dataFimReal = dataLimite != null && dataLimite.isBefore(fim) ? dataLimite : fim;

        if ("SEMANAL".equalsIgnoreCase(frequencia)) {
            gerarDatasSemanais(datas, atual, dataFimReal);
        } else if ("MENSAL".equalsIgnoreCase(frequencia)) {
            gerarDatasMensais(datas, atual, dataFimReal, inicio);
        } else if ("ANUAL".equalsIgnoreCase(frequencia)) {
            gerarDatasAnuais(datas, atual, dataFimReal);
        }

        datas.sort(LocalDate::compareTo);
        return datas;
    }

    private void gerarDatasSemanais(List<LocalDate> datas, LocalDate atual, LocalDate dataFimReal) {
        while (!atual.isAfter(dataFimReal)) {
            if (diasDaSemana != null && !diasDaSemana.isEmpty()) {
                for (DayOfWeek dia : diasDaSemana) {
                    LocalDate proximo = atual.with(TemporalAdjusters.nextOrSame(dia));
                    if (!proximo.isBefore(atual) && !proximo.isAfter(dataFimReal) && !datas.contains(proximo)) {
                        datas.add(proximo);
                    }
                }
            }
            atual = atual.plusWeeks(intervalo);
        }
    }

    private void gerarDatasMensais(List<LocalDate> datas, LocalDate atual, LocalDate dataFimReal, LocalDate inicio) {
        while (!atual.isAfter(dataFimReal)) {
            if ("LAST".equalsIgnoreCase(posicaoNoMes) && diasDaSemana != null && !diasDaSemana.isEmpty()) {
                for (DayOfWeek dia : diasDaSemana) {
                    LocalDate ultimoDia = atual.with(TemporalAdjusters.lastInMonth(dia));
                    if (!ultimoDia.isBefore(inicio) && !ultimoDia.isAfter(dataFimReal)) {
                        datas.add(ultimoDia);
                    }
                }
            } else if (diaDoMes != null) {
                try {
                    LocalDate dataFixa = atual.withDayOfMonth(diaDoMes);
                    if (!dataFixa.isBefore(inicio) && !dataFixa.isAfter(dataFimReal)) {
                        datas.add(dataFixa);
                    }
                } catch (Exception e) {
                    // Skip invalid days (e.g., Feb 30th)
                }
            }
            atual = atual.plusMonths(intervalo);
        }
    }

    private void gerarDatasAnuais(List<LocalDate> datas, LocalDate atual, LocalDate dataFimReal) {
        while (!atual.isAfter(dataFimReal)) {
            if (mesDoAno != null && diaDoMes != null) {
                try {
                    LocalDate dataFixa = LocalDate.of(atual.getYear(), mesDoAno, diaDoMes);
                    if (!dataFixa.isBefore(atual.withDayOfYear(1)) && !dataFixa.isAfter(dataFimReal)) {
                        if (!dataFixa.isBefore(atual)) {
                             datas.add(dataFixa);
                        }
                    }
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
            atual = atual.plusYears(intervalo);
        }
    }
}
