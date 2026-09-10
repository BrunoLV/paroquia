package br.com.nsfatima.gestao.aprovacao.domain.model;

import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoJaDecididaException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AprovacaoTest {

    private static final Instant AGORA = Instant.parse("2026-06-01T12:00:00Z");

    private Aprovacao pendente() {
        return Aprovacao.solicitar(UUID.randomUUID(), UUID.randomUUID(), "CRIACAO_EVENTO", "PAROCO",
                "solicitante-1", null, AGORA);
    }

    @Test
    void solicitarNasceoPendente() {
        assertThat(pendente().getStatus()).isEqualTo(AprovacaoStatus.PENDENTE);
    }

    @Test
    void aprovarRegistraDecisor() {
        Aprovacao a = pendente();
        a.aprovar("decisor-1", "PAROCO", "ok", AGORA);
        assertThat(a.getStatus()).isEqualTo(AprovacaoStatus.APROVADA);
        assertThat(a.getAprovadorId()).isEqualTo("decisor-1");
        assertThat(a.getDecididoEmUtc()).isEqualTo(AGORA);
    }

    @Test
    void naoDecideDuasVezes() {
        Aprovacao a = pendente();
        a.aprovar("d", "PAROCO", null, AGORA);
        assertThatThrownBy(() -> a.reprovar("d2", "PAROCO", null, AGORA))
                .isInstanceOf(AprovacaoJaDecididaException.class);
    }

    @Test
    void assegurarDecidivelFalhaSeExpirada() {
        Instant inicioNoPassado = AGORA.minus(1, ChronoUnit.HOURS);
        assertThatThrownBy(() -> pendente().assegurarDecidivel(inicioNoPassado, AGORA))
                .isInstanceOf(AprovacaoExecucaoFalhouException.class);
    }

    @Test
    void assegurarDecidivelFalhaSeJaDecidida() {
        Aprovacao a = pendente();
        a.aprovar("d", "PAROCO", null, AGORA);
        assertThatThrownBy(() -> a.assegurarDecidivel(null, AGORA))
                .isInstanceOf(AprovacaoJaDecididaException.class);
    }

    @Test
    void execucaoConcluidaSoSobreAprovada() {
        Aprovacao a = pendente();
        assertThatThrownBy(() -> a.registrarExecucaoConcluida(AGORA)).isInstanceOf(IllegalStateException.class);
        a.aprovar("d", "PAROCO", null, AGORA);
        a.registrarExecucaoConcluida(AGORA);
        assertThat(a.getExecutadoEmUtc()).isEqualTo(AGORA);
    }

    @Test
    void falhaExecucaoSoSobreAprovada() {
        Aprovacao a = pendente();
        a.aprovar("d", "PAROCO", null, AGORA);
        a.registrarFalhaExecucao("conflito de agenda");
        assertThat(a.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(a.getMensagemErroExecucao()).isEqualTo("conflito de agenda");
    }
}
