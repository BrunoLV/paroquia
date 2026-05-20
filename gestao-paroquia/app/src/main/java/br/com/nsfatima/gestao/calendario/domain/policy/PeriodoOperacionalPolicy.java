package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.PeriodoOperacionalResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.PeriodoOperacionalInvalidoException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PeriodoOperacionalPolicy {

    private final Clock clock;

    public PeriodoOperacionalPolicy() {
        this(Clock.systemUTC());
    }

    PeriodoOperacionalPolicy(Clock clock) {
        this.clock = clock;
    }

    public ResolvedPeriod resolve(String granularidade, Instant inicio, Instant fim) {
        boolean hasGranularidade = granularidade != null && !granularidade.isBlank();
        boolean hasInicio = inicio != null;
        boolean hasFim = fim != null;

        if (hasGranularidade && (hasInicio || hasFim)) {
            throw new PeriodoOperacionalInvalidoException(
                    "periodo",
                    "Granularidade e inicio/fim nao podem ser informados juntos",
                    false);
        }

        if (!hasGranularidade && !hasInicio && !hasFim) {
            throw new PeriodoOperacionalInvalidoException(
                    "periodo",
                    "Granularidade ou inicio/fim devem ser informados",
                    true);
        }

        if (!hasGranularidade && (hasInicio != hasFim)) {
            throw new PeriodoOperacionalInvalidoException(
                    hasInicio ? "fim" : "inicio",
                    "inicio e fim devem ser informados juntos",
                    true);
        }

        if (hasGranularidade) {
            String normalized = Objects.requireNonNull(granularidade).trim().toUpperCase();
            Instant now = clock.instant();
            LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
            return switch (normalized) {
                case "DIARIO" -> new ResolvedPeriod(
                        normalized,
                        today.atStartOfDay().toInstant(ZoneOffset.UTC),
                        today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                case "SEMANAL" -> {
                    LocalDate start = today.with(DayOfWeek.MONDAY);
                    yield new ResolvedPeriod(
                            normalized,
                            start.atStartOfDay().toInstant(ZoneOffset.UTC),
                            start.plusWeeks(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                }
                case "MENSAL" -> {
                    LocalDate start = today.withDayOfMonth(1);
                    yield new ResolvedPeriod(
                            normalized,
                            start.atStartOfDay().toInstant(ZoneOffset.UTC),
                            start.plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                }
                case "ANUAL" -> {
                    LocalDate start = today.with(TemporalAdjusters.firstDayOfYear());
                    yield new ResolvedPeriod(
                            normalized,
                            start.atStartOfDay().toInstant(ZoneOffset.UTC),
                            start.plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                }
                default -> throw new PeriodoOperacionalInvalidoException(
                        "granularidade",
                        "Granularidade operacional nao suportada",
                        false);
            };
        }

        Instant requiredInicio = Objects.requireNonNull(inicio);
        Instant requiredFim = Objects.requireNonNull(fim);
        if (!requiredFim.isAfter(requiredInicio)) {
            throw new PeriodoOperacionalInvalidoException(
                    "fim",
                    "fim deve ser maior que inicio",
                    false);
        }

        return new ResolvedPeriod(null, requiredInicio, requiredFim);
    }

    public record ResolvedPeriod(
            String granularidade,
            Instant inicio,
            Instant fim) {

        public PeriodoOperacionalResponse toResponse() {
            return new PeriodoOperacionalResponse(granularidade, inicio, fim);
        }
    }
}
