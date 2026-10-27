package br.com.nsfatima.gestao.support.domain.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Relógio de negócio da paróquia. Os instantes continuam persistidos em UTC, mas toda decisão de
 * calendário civil (qual é "hoje", qual o mês corrente, a que ano paroquial pertence um evento, em
 * qual dia cai um aniversário) deve ser tomada no fuso da paróquia, senão uma celebração às 22h de
 * 31/12 em Brasília entraria no ano seguinte e os dashboards virariam o dia três horas adiantados.
 *
 * <p>Concentrar relógio e fuso aqui dá um único ponto configurável ({@code app.timezone.output}) e
 * permite testar as viradas (31/12 22h, último dia do mês, 29/02) com um {@link Clock} fixo.
 * Classe pura: o registro como bean é feito na infraestrutura.
 */
public final class RelogioNegocio {

    private final Clock clock;
    private final ZoneId zona;

    /**
     * Cria o relógio de negócio.
     *
     * @param clock relógio de referência (instantes absolutos; seu fuso é ignorado)
     * @param fuso fuso civil da paróquia
     */
    public RelogioNegocio(Clock clock, ZoneId fuso) {
        this.clock = Objects.requireNonNull(clock, "clock é obrigatório");
        this.zona = Objects.requireNonNull(fuso, "fuso é obrigatório");
    }

    /** Fuso civil da paróquia. */
    public ZoneId fuso() {
        return zona;
    }

    /** Instante atual. */
    public Instant agora() {
        return clock.instant();
    }

    /** Dia civil corrente no fuso da paróquia. */
    public LocalDate hoje() {
        return dataDe(agora());
    }

    /** Dia civil, no fuso da paróquia, em que cai o instante informado. */
    public LocalDate dataDe(Instant instante) {
        return instante.atZone(zona).toLocalDate();
    }

    /** Ano civil (e paroquial), no fuso da paróquia, do instante informado. */
    public int anoDe(Instant instante) {
        return dataDe(instante).getYear();
    }

    /** Instante da meia-noite local do dia informado. */
    public Instant inicioDoDia(LocalDate dia) {
        return dia.atStartOfDay(zona).toInstant();
    }

    /** Instante da meia-noite local do primeiro dia do mês corrente. */
    public Instant inicioDoMes() {
        return inicioDoDia(hoje().withDayOfMonth(1));
    }

    /**
     * Projeta o horário local de um instante-modelo sobre outro dia civil. É a regra única de
     * materialização de ocorrências recorrentes: a missa das 22h30 de sábado em Brasília continua às
     * 22h30 locais em todo sábado, mesmo que em UTC isso já seja o domingo seguinte.
     *
     * @param modelo instante cujo horário local é replicado
     * @param dia dia civil (no fuso informado) da nova ocorrência
     * @param fuso fuso civil da paróquia
     * @return instante da ocorrência no dia informado, com o mesmo horário local do modelo
     */
    public static Instant mesmoHorarioNoDia(Instant modelo, LocalDate dia, ZoneId fuso) {
        return dia.atTime(modelo.atZone(fuso).toLocalTime()).atZone(fuso).toInstant();
    }

    /** Versão de {@link #mesmoHorarioNoDia(Instant, LocalDate, ZoneId)} no fuso deste relógio. */
    public Instant mesmoHorarioNoDia(Instant modelo, LocalDate dia) {
        return mesmoHorarioNoDia(modelo, dia, zona);
    }
}
