package br.com.nsfatima.gestao.patrimonio.domain.model;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtivoManutencaoTest {

    private final Instant inicio = Instant.now();
    private final Instant fimPrevisto = inicio.plus(3, ChronoUnit.DAYS);

    private AtivoManutencao emAberto() {
        return AtivoManutencao.registrar(
                AtivoManutencaoId.generate(), AtivoPatrimonialId.generate(), "troca de lâmpada",
                new PeriodoManutencao(inicio, fimPrevisto), UUID.randomUUID(), inicio);
    }

    @Test
    void periodoInvertidoRejeitado() {
        assertThatThrownBy(() -> new PeriodoManutencao(fimPrevisto, inicio))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void novaManutencaoNasceEmAberto() {
        assertThat(emAberto().emAberto()).isTrue();
    }

    @Test
    void concluirRegistraDataFimReal() {
        AtivoManutencao m = emAberto();
        Instant real = inicio.plus(2, ChronoUnit.DAYS);
        m.concluir(real);
        assertThat(m.getDataFimReal()).isEqualTo(real);
        assertThat(m.emAberto()).isFalse();
    }

    @Test
    void naoConcluiDuasVezes() {
        AtivoManutencao m = emAberto();
        m.concluir(fimPrevisto);
        assertThatThrownBy(() -> m.concluir(fimPrevisto))
                .isInstanceOf(AtivoManutencaoInvalidaException.class);
    }
}
