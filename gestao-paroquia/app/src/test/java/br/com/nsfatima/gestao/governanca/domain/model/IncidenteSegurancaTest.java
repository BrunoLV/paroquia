package br.com.nsfatima.gestao.governanca.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Incidente de seguranca: prazo de 3 dias uteis e transicoes")
class IncidenteSegurancaTest {

    private IncidenteSeguranca detectadoEm(String instante) {
        Instant deteccao = Instant.parse(instante);
        return IncidenteSeguranca.registrar(UUID.randomUUID(), "t", "d", "dados", GravidadeIncidente.ALTA,
                deteccao.minusSeconds(60), deteccao, null);
    }

    @Test
    @DisplayName("deteccao numa quarta-feira vence na segunda-feira seguinte, pulando o fim de semana")
    void prazoPulaFimDeSemana() {
        IncidenteSeguranca incidente = detectadoEm("2026-06-03T12:00:00Z");

        assertThat(incidente.prazoComunicacao()).isEqualTo(Instant.parse("2026-06-08T12:00:00Z"));
    }

    @Test
    @DisplayName("deteccao numa segunda-feira vence na quinta-feira")
    void prazoDentroDaSemana() {
        assertThat(detectadoEm("2026-06-01T08:00:00Z").prazoComunicacao())
                .isEqualTo(Instant.parse("2026-06-04T08:00:00Z"));
    }

    @Test
    @DisplayName("prazo so vence sem ANPD notificada e fora do estado encerrado")
    void prazoVencidoSomenteSemNotificacao() {
        IncidenteSeguranca incidente = detectadoEm("2026-06-01T08:00:00Z");
        Instant depois = Instant.parse("2026-06-10T00:00:00Z");

        assertThat(incidente.prazoVencido(depois)).isTrue();
        assertThat(incidente.prazoVencido(Instant.parse("2026-06-02T00:00:00Z"))).isFalse();
        assertThat(incidente.atualizar(StatusIncidente.NOTIFICADO, true, null, null, null, null, null, depois)
                .prazoVencido(depois)).isFalse();
        assertThat(incidente.atualizar(StatusIncidente.ENCERRADO, null, null, null, null, null, null, depois)
                .prazoVencido(depois)).isFalse();
    }

    @Test
    @DisplayName("NOTIFICADO assume a data atual quando a data da notificacao nao e informada")
    void notificadoAssumeDataAtual() {
        Instant agora = Instant.parse("2026-06-02T09:00:00Z");

        IncidenteSeguranca notificado = detectadoEm("2026-06-01T08:00:00Z")
                .atualizar(StatusIncidente.NOTIFICADO, true, null, "ANPD-1", null, null, null, agora);

        assertThat(notificado.dataNotificacaoAnpd()).isEqualTo(agora);
        assertThat(notificado.protocoloAnpd()).isEqualTo("ANPD-1");
    }

    @Test
    @DisplayName("nao retrocede de NOTIFICADO para CONTIDO")
    void naoRetrocede() {
        IncidenteSeguranca notificado = detectadoEm("2026-06-01T08:00:00Z")
                .atualizar(StatusIncidente.NOTIFICADO, true, null, null, null, null, null, Instant.now());

        assertThatThrownBy(() -> notificado.atualizar(
                StatusIncidente.CONTIDO, null, null, null, null, null, null, Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }
}
