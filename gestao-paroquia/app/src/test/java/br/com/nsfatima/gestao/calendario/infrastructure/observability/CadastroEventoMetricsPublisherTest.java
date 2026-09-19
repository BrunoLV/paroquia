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

    @Test
    @DisplayName("Deve registrar métricas de eventos: cadastro, lead time, consultas, retrabalho e cancelamento")
    void deveRegistrarMetricasDeEventos() {
        publisher.publishCadastroTempo(Duration.ofMillis(120));
        publisher.publishEventRegistrationLeadTime(Duration.ofMinutes(1440));
        publisher.publishCalendarQueryLatency("/api/v1/eventos", Duration.ofMillis(45));
        publisher.publishAdministrativeRework(true, false, true);
        publisher.publishCancellationFlow("IMMEDIATE", "SUCCESS");
        publisher.publishCreateSuccess(true, true);
        publisher.publishCreateFailure("VALIDACAO");
        publisher.recordSolicitacaoAberta("ALTERACAO_HORARIO", "COORDENADOR");

        Timer cadastroTimer = meterRegistry.find("gestao.evento.tempo.cadastro").timer();
        assertThat(cadastroTimer).isNotNull();
        assertThat(cadastroTimer.count()).isEqualTo(1);

        Timer leadTimeTimer = meterRegistry.find("gestao.evento.lead_time").timer();
        assertThat(leadTimeTimer).isNotNull();
        assertThat(leadTimeTimer.count()).isEqualTo(1);

        Timer queryTimer = meterRegistry.find("gestao.evento.consulta.latencia")
                .tag("endpoint", "/api/v1/eventos")
                .timer();
        assertThat(queryTimer).isNotNull();
        assertThat(queryTimer.count()).isEqualTo(1);

        Counter retrabalho = meterRegistry.find("gestao.evento.retrabalho.total")
                .tag("horario_alterado", "true")
                .tag("cancelamento", "false")
                .tag("organizacao_alterada", "true")
                .counter();
        assertThat(retrabalho).isNotNull();
        assertThat(retrabalho.count()).isEqualTo(1.0);

        Counter cancelamento = meterRegistry.find("gestao.evento.cancelamentos.total")
                .tag("modo", "IMMEDIATE")
                .tag("outcome", "SUCCESS")
                .counter();
        assertThat(cancelamento).isNotNull();
        assertThat(cancelamento.count()).isEqualTo(1.0);

        Counter criados = meterRegistry.find("gestao.evento.criados.total")
                .tag("conflito_pendente", "true")
                .tag("replay", "true")
                .counter();
        assertThat(criados).isNotNull();
        assertThat(criados.count()).isEqualTo(1.0);

        Counter conflitos = meterRegistry.find("gestao.evento.conflitos.total")
                .tag("tipo", "PENDENTE_APROVACAO")
                .counter();
        assertThat(conflitos).isNotNull();
        assertThat(conflitos.count()).isEqualTo(1.0);

        Counter replays = meterRegistry.find("gestao.evento.idempotencia.replays.total").counter();
        assertThat(replays).isNotNull();
        assertThat(replays.count()).isEqualTo(1.0);

        Counter falhas = meterRegistry.find("gestao.evento.falhas.total")
                .tag("categoria", "VALIDACAO")
                .counter();
        assertThat(falhas).isNotNull();
        assertThat(falhas.count()).isEqualTo(1.0);

        Counter solicitadas = meterRegistry.find("gestao.aprovacao.solicitadas.total")
                .tag("tipo", "ALTERACAO_HORARIO")
                .tag("papel_solicitante", "COORDENADOR")
                .counter();
        assertThat(solicitadas).isNotNull();
        assertThat(solicitadas.count()).isEqualTo(1.0);
    }
}

