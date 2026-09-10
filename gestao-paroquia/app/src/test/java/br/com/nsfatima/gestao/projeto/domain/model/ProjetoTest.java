package br.com.nsfatima.gestao.projeto.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjetoTest {

    private static final Instant INICIO = Instant.parse("2026-10-01T00:00:00Z");
    private static final Instant FIM = Instant.parse("2026-10-31T00:00:00Z");

    private Projeto aberto() {
        return Projeto.abrir(UUID.randomUUID(), "Projeto", "desc", UUID.randomUUID(), INICIO, FIM);
    }

    @Test
    void abrirNasceAberto() {
        assertThat(aberto().getStatus()).isEqualTo(ProjetoStatus.ABERTO);
    }

    @Test
    void abrirRejeitaFimAntesDeInicio() {
        assertThatThrownBy(() -> Projeto.abrir(UUID.randomUUID(), "P", null, null, FIM, INICIO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cicloDeVidaCompleto() {
        Projeto p = aberto();
        p.iniciar();
        assertThat(p.getStatus()).isEqualTo(ProjetoStatus.EM_ANDAMENTO);
        p.concluir();
        assertThat(p.getStatus()).isEqualTo(ProjetoStatus.CONCLUIDO);
    }

    @Test
    void naoIniciaForaDeAberto() {
        Projeto p = aberto();
        p.iniciar();
        assertThatThrownBy(p::iniciar).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void naoConcluiForaDeEmAndamento() {
        assertThatThrownBy(aberto()::concluir).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void concluidoETerminal() {
        Projeto p = aberto();
        p.iniciar();
        p.concluir();
        assertThatThrownBy(p::cancelar).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelaDeAbertoOuEmAndamento() {
        Projeto p = aberto();
        p.cancelar();
        assertThat(p.getStatus()).isEqualTo(ProjetoStatus.CANCELADO);
    }

    @Test
    void redefinirPeriodoValidaOrdem() {
        Projeto p = aberto();
        assertThatThrownBy(() -> p.redefinirPeriodo(FIM, INICIO)).isInstanceOf(IllegalArgumentException.class);
        p.redefinirPeriodo(INICIO, FIM.plus(1, ChronoUnit.DAYS));
        assertThat(p.getFim()).isEqualTo(FIM.plus(1, ChronoUnit.DAYS));
    }
}
