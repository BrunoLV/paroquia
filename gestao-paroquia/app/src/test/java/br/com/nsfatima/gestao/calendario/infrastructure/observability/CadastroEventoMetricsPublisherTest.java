package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para validar a publicação de métricas de tempos e contadores
 * operacionais no {@link CadastroEventoMetricsPublisher} utilizando Micrometer.
 */
class CadastroEventoMetricsPublisherTest {

    private MeterRegistry meterRegistry;
    private CadastroEventoMetricsPublisher publisher;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        publisher = new CadastroEventoMetricsPublisher(meterRegistry);
    }

    @Test
    @DisplayName("Deve registrar latências de decisão, execução e tempo total nos respectivos Timers")
    void deveRegistrarLatenciasNosTimers() {
        publisher.publishAprovacaoDecisionLatency("CRIACAO_EVENTO", "APROVADA", Duration.ofSeconds(15));
        publisher.publishAprovacaoExecutionLatency("CRIACAO_EVENTO", Duration.ofMillis(250));
        publisher.publishAprovacaoTotalLatency("CRIACAO_EVENTO", Duration.ofSeconds(16));

        Timer decisaoTimer = meterRegistry.find("gestao.aprovacao.tempo.decisao")
                .tag("tipo", "CRIACAO_EVENTO")
                .tag("status", "APROVADA")
                .timer();
        assertThat(decisaoTimer).isNotNull();
        assertThat(decisaoTimer.count()).isEqualTo(1);
        assertThat(decisaoTimer.totalTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(15.0);

        Timer execucaoTimer = meterRegistry.find("gestao.aprovacao.tempo.execucao")
                .tag("tipo", "CRIACAO_EVENTO")
                .timer();
        assertThat(execucaoTimer).isNotNull();
        assertThat(execucaoTimer.count()).isEqualTo(1);

        Timer totalTimer = meterRegistry.find("gestao.aprovacao.tempo.total")
                .tag("tipo", "CRIACAO_EVENTO")
                .timer();
        assertThat(totalTimer).isNotNull();
        assertThat(totalTimer.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve incrementar contadores de aprovações, rejeições, deleções e edições")
    void deveIncrementarContadoresOperacionais() {
        publisher.recordAprovacao("CRIACAO_EVENTO");
        publisher.recordAprovacao("CRIACAO_EVENTO");
        publisher.recordRejeicao("EDICAO_EVENTO");
        publisher.recordDelecao("EVENTO", "APROVACAO");
        publisher.recordDelecao("EVENTO", "DIRETA");
        publisher.recordEdicao("EVENTO", "DIRETA");

        Counter aprovacoes = meterRegistry.find("gestao.aprovacoes.total")
                .tag("tipo", "CRIACAO_EVENTO")
                .counter();
        assertThat(aprovacoes).isNotNull();
        assertThat(aprovacoes.count()).isEqualTo(2.0);

        Counter rejeicoes = meterRegistry.find("gestao.rejeicoes.total")
                .tag("tipo", "EDICAO_EVENTO")
                .counter();
        assertThat(rejeicoes).isNotNull();
        assertThat(rejeicoes.count()).isEqualTo(1.0);

        Counter delecaoAprovacao = meterRegistry.find("gestao.delecoes.total")
                .tag("recurso", "EVENTO")
                .tag("origem", "APROVACAO")
                .counter();
        assertThat(delecaoAprovacao).isNotNull();
        assertThat(delecaoAprovacao.count()).isEqualTo(1.0);

        Counter delecaoDireta = meterRegistry.find("gestao.delecoes.total")
                .tag("recurso", "EVENTO")
                .tag("origem", "DIRETA")
                .counter();
        assertThat(delecaoDireta).isNotNull();
        assertThat(delecaoDireta.count()).isEqualTo(1.0);

        Counter edicaoDireta = meterRegistry.find("gestao.edicoes.total")
                .tag("recurso", "EVENTO")
                .tag("origem", "DIRETA")
                .counter();
        assertThat(edicaoDireta).isNotNull();
        assertThat(edicaoDireta.count()).isEqualTo(1.0);
    }
}
