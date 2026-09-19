package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Conector de métricas de negócio para o Micrometer/Prometheus.
 *
 * <p>Registra {@link Gauge}s dinâmicos que expõem em tempo real para os dashboards do Grafana
 * o estoque de solicitações de governança pendentes e o volume de eventos ativos no banco de dados.</p>
 */
@Component
public class ParoquiaBusinessMetricsBinder implements MeterBinder {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;

    public ParoquiaBusinessMetricsBinder(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoJpaRepository eventoJpaRepository) {
        this.aprovacaoJpaRepository = Objects.requireNonNull(aprovacaoJpaRepository,
                "aprovacaoJpaRepository não pode ser nulo");
        this.eventoJpaRepository = Objects.requireNonNull(eventoJpaRepository,
                "eventoJpaRepository não pode ser nulo");
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("gestao.aprovacoes.pendentes", aprovacaoJpaRepository,
                        repo -> repo.countByStatus(AprovacaoStatus.PENDENTE))
                .description("Quantidade de solicitações de aprovação pendentes de parecer")
                .register(registry);

        Gauge.builder("gestao.eventos.confirmados", eventoJpaRepository,
                        repo -> repo.countByStatus(EventoStatusInput.CONFIRMADO.name()))
                .description("Quantidade total de eventos confirmados no calendário")
                .register(registry);

        Gauge.builder("gestao.eventos.rascunhos", eventoJpaRepository,
                        repo -> repo.countByStatus(EventoStatusInput.RASCUNHO.name()))
                .description("Quantidade total de eventos em rascunho no calendário")
                .register(registry);
    }
}
