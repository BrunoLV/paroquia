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
        LocalDate dataLimite,
        Integer numeroOcorrencias) {

    /**
     * Generates a list of dates between the specified range based on the recurrence rules.
     */
    public List<LocalDate> gerarDatas(LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate dataFimReal = dataLimite != null && dataLimite.isBefore(fim) ? dataLimite : fim;

        if ("DIARIA".equalsIgnoreCase(frequencia)) {
            gerarDatasDiarias(datas, inicio, dataFimReal);
        } else if ("SEMANAL".equalsIgnoreCase(frequencia)) {
            gerarDatasSemanais(datas, inicio, dataFimReal);
        } else if ("MENSAL".equalsIgnoreCase(frequencia)) {
            gerarDatasMensais(datas, inicio, dataFimReal, inicio);
        } else if ("ANUAL".equalsIgnoreCase(frequencia)) {
            gerarDatasAnuais(datas, inicio, dataFimReal);
        }

        datas.sort(LocalDate::compareTo);

        if (numeroOcorrencias != null && datas.size() > numeroOcorrencias) {
            return datas.subList(0, numeroOcorrencias);
        }

        return datas;
    }

    private void gerarDatasDiarias(List<LocalDate> datas, LocalDate inicioParam, LocalDate dataFimReal) {
        LocalDate cursor = inicioParam;
        while (!cursor.isAfter(dataFimReal)) {
            datas.add(cursor);
            cursor = cursor.plusDays(intervalo);
        }
    }

    private void gerarDatasSemanais(List<LocalDate> datas, LocalDate inicioParam, LocalDate dataFimReal) {
        LocalDate cursor = inicioParam;
        while (!cursor.isAfter(dataFimReal)) {
            if (diasDaSemana != null && !diasDaSemana.isEmpty()) {
                for (DayOfWeek dia : diasDaSemana) {
                    LocalDate proximo = cursor.with(TemporalAdjusters.nextOrSame(dia));
                    if (!proximo.isBefore(cursor) && !proximo.isAfter(dataFimReal) && !datas.contains(proximo)) {
                        datas.add(proximo);
                    }
                }
            }
            cursor = cursor.plusWeeks(intervalo);
        }
    }

    private void gerarDatasMensais(
            List<LocalDate> datas,
            LocalDate inicioParam,
            LocalDate dataFimReal,
            LocalDate inicio) {
        LocalDate cursor = inicioParam;
        while (!cursor.isAfter(dataFimReal)) {
            if (posicaoNoMes != null && !posicaoNoMes.isBlank() && diasDaSemana != null && !diasDaSemana.isEmpty()) {
                int ordinal = getOrdinalFromPosicao(posicaoNoMes);
                if (ordinal != 0) {
                    for (DayOfWeek dia : diasDaSemana) {
                        LocalDate proximo;
                        if (ordinal > 0) {
                            proximo = cursor.with(TemporalAdjusters.dayOfWeekInMonth(ordinal, dia));
                        } else {
                            proximo = cursor.with(TemporalAdjusters.lastInMonth(dia));
                        }

                        if (!proximo.isBefore(inicio) && !proximo.isAfter(dataFimReal) && !datas.contains(proximo)) {
                            datas.add(proximo);
                        }
                    }
                }
            } else if (diaDoMes != null) {
                try {
                    int diaAjustado = Math.min(diaDoMes, cursor.lengthOfMonth());
                    LocalDate dataFixa = cursor.withDayOfMonth(diaAjustado);
                    if (!dataFixa.isBefore(inicio) && !dataFixa.isAfter(dataFimReal)) {
                        datas.add(dataFixa);
                    }
                } catch (Exception e) {
                    // Skip invalid days (e.g., Feb 30th)
                }
            }
            cursor = cursor.plusMonths(intervalo);
        }
    }

    private int getOrdinalFromPosicao(String posicao) {
        if (posicao == null) {
            return 0;
        }
        String p = posicao.toUpperCase().trim();
        switch (p) {
            case "1":
            case "1ST":
            case "FIRST":
            case "PRIMEIRO":
            case "PRIMEIRA":
                return 1;
            case "2":
            case "2ND":
            case "SECOND":
            case "SEGUNDO":
            case "SEGUNDA":
                return 2;
            case "3":
            case "3RD":
            case "THIRD":
            case "TERCEIRO":
            case "TERCEIRA":
                return 3;
            case "4":
            case "4TH":
            case "FOURTH":
            case "QUARTO":
            case "QUARTA":
                return 4;
            case "5":
            case "5TH":
            case "FIFTH":
            case "QUINTO":
            case "QUINTA":
                return 5;
            case "LAST":
            case "ULTIMO":
            case "ULTIMA":
                return -1;
            default:
                try {
                    return Integer.parseInt(p);
                } catch (NumberFormatException e) {
                    return 0; // Invalid
                }
        }
    }


    private void gerarDatasAnuais(List<LocalDate> datas, LocalDate inicioParam, LocalDate dataFimReal) {
        LocalDate cursor = inicioParam;
        while (!cursor.isAfter(dataFimReal)) {
            if (mesDoAno != null && diaDoMes != null) {
                try {
                    LocalDate dataFixa = LocalDate.of(cursor.getYear(), mesDoAno, diaDoMes);
                    if (!dataFixa.isBefore(cursor.withDayOfYear(1)) && !dataFixa.isAfter(dataFimReal)) {
                        if (!dataFixa.isBefore(cursor)) {
                            datas.add(dataFixa);
                        }
                    }
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
            cursor = cursor.plusYears(intervalo);
        }
    }
}
