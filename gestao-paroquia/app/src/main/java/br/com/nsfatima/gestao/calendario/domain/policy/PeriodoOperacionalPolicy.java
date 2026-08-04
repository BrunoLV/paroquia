package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.PeriodoOperacionalResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.PeriodoOperacionalInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.type.Granularidade;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * Política que resolve o período operacional (janela temporal) usado em consultas
 * de métricas/indicadores do calendário (ex.: taxa de retrabalho, SLA de despacho
 * pastoral). O período pode ser informado de duas formas mutuamente exclusivas:
 * uma {@code granularidade} pré-definida (diária, semanal, mensal, anual), que é
 * traduzida no período corrente correspondente relativo à data atual do relógio; ou
 * um intervalo explícito de {@code inicio}/{@code fim}. As duas formas não podem ser
 * combinadas nem omitidas simultaneamente.
 */
@Component
public class PeriodoOperacionalPolicy {

    private final Clock clock;

    /**
     * Cria a política usando o relógio UTC do sistema como referência de "agora"
     * para resolver granularidades.
     */
    public PeriodoOperacionalPolicy() {
        this(Clock.systemUTC());
    }

    PeriodoOperacionalPolicy(Clock clock) {
        this.clock = clock;
    }

    /**
     * Resolve o período operacional efetivo a partir da granularidade ou do
     * intervalo explícito informado, validando que exatamente uma das duas formas
     * foi utilizada e que o intervalo resultante é temporalmente consistente.
     *
     * @param granularidade código de granularidade pré-definida, ou {@code null}/vazio se um intervalo explícito for
     *     usado
     * @param inicio início do intervalo explícito, ou {@code null} se uma granularidade for usada
     * @param fim fim do intervalo explícito, ou {@code null} se uma granularidade for usada
     * @return período operacional resolvido, com granularidade e/ou intervalo efetivo
     * @throws PeriodoOperacionalInvalidoException se granularidade e intervalo forem combinados, se nenhum dos dois
     *     for informado, se apenas um de início/fim for informado, se a granularidade não for suportada, ou se o fim
     *     não for posterior ao início
     */
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
            Granularidade granularidadeEnum = Granularidade.fromCode(normalized);
            if (granularidadeEnum == null) {
                throw new PeriodoOperacionalInvalidoException(
                        "granularidade",
                        "Granularidade operacional nao suportada",
                        false);
            }
            Instant now = clock.instant();
            LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
            return switch (granularidadeEnum) {
                case DIARIO -> new ResolvedPeriod(
                        normalized,
                        today.atStartOfDay().toInstant(ZoneOffset.UTC),
                        today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                case SEMANAL -> {
                    LocalDate start = today.with(DayOfWeek.MONDAY);
                    yield new ResolvedPeriod(
                            normalized,
                            start.atStartOfDay().toInstant(ZoneOffset.UTC),
                            start.plusWeeks(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                }
                case MENSAL -> {
                    LocalDate start = today.withDayOfMonth(1);
                    yield new ResolvedPeriod(
                            normalized,
                            start.atStartOfDay().toInstant(ZoneOffset.UTC),
                            start.plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                }
                case ANUAL -> {
                    LocalDate start = today.with(TemporalAdjusters.firstDayOfYear());
                    yield new ResolvedPeriod(
                            normalized,
                            start.atStartOfDay().toInstant(ZoneOffset.UTC),
                            start.plusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC));
                }
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

    /**
     * Período operacional já resolvido, pronto para ser usado como janela de
     * consulta de métricas.
     *
     * @param granularidade granularidade que originou o período, ou {@code null} se resolvido a partir de intervalo
     *     explícito
     * @param inicio início efetivo do período
     * @param fim fim efetivo do período
     */
    public record ResolvedPeriod(
            String granularidade,
            Instant inicio,
            Instant fim) {

        /**
         * Converte o período resolvido para o DTO de resposta da API.
         *
         * @return resposta de período operacional para exposição via API
         */
        public PeriodoOperacionalResponse toResponse() {
            return new PeriodoOperacionalResponse(granularidade, inicio, fim);
        }
    }
}
