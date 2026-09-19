package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link ParoquiaBusinessMetricsBinder}, verificando o correto
 * registro e resolução dos gauges dinâmicos de negócio.
 */
class ParoquiaBusinessMetricsBinderTest {

    private AprovacaoJpaRepository aprovacaoRepository;
    private EventoJpaRepository eventoRepository;
    private MeterRegistry meterRegistry;
    private ParoquiaBusinessMetricsBinder binder;

    @BeforeEach
    void setUp() {
        aprovacaoRepository = mock(AprovacaoJpaRepository.class);
        eventoRepository = mock(EventoJpaRepository.class);
        meterRegistry = new SimpleMeterRegistry();
        binder = new ParoquiaBusinessMetricsBinder(aprovacaoRepository, eventoRepository);
    }

    @Test
    @DisplayName("Deve registrar e avaliar gauges de aprovações pendentes e eventos confirmados e rascunhos")
    void deveRegistrarEAvaliarGaugesDeNegocio() {
        when(aprovacaoRepository.countByStatus(AprovacaoStatus.PENDENTE)).thenReturn(7L);
        when(eventoRepository.countByStatus(EventoStatusInput.CONFIRMADO.name())).thenReturn(42L);
        when(eventoRepository.countByStatus(EventoStatusInput.RASCUNHO.name())).thenReturn(3L);

        binder.bindTo(meterRegistry);

        Gauge pendentes = meterRegistry.find("gestao.aprovacoes.pendentes").gauge();
        assertThat(pendentes).isNotNull();
        assertThat(pendentes.value()).isEqualTo(7.0);

        Gauge confirmados = meterRegistry.find("gestao.eventos.confirmados").gauge();
        assertThat(confirmados).isNotNull();
        assertThat(confirmados.value()).isEqualTo(42.0);

        Gauge rascunhos = meterRegistry.find("gestao.eventos.rascunhos").gauge();
        assertThat(rascunhos).isNotNull();
        assertThat(rascunhos.value()).isEqualTo(3.0);
    }
}
