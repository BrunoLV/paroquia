package br.com.nsfatima.gestao.patrimonio.domain.model;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtivoPatrimonialTest {

    private AtivoPatrimonial ativoDisponivel() {
        return AtivoPatrimonial.cadastrar(
                AtivoPatrimonialId.gerar(), "Projetor", CodigoTombamento.de(2026, 1), null,
                AtivoCategoria.ELETRONICO, LocalDate.now(), BigDecimal.valueOf(1500), UUID.randomUUID(),
                true, UUID.randomUUID(), Instant.now());
    }

    @Test
    void cadastraSempreDisponivel() {
        assertThat(ativoDisponivel().getStatus()).isEqualTo(AtivoStatus.DISPONIVEL);
    }

    @Test
    void atualizaApenasDescricao() {
        AtivoPatrimonial ativo = ativoDisponivel();
        ativo.atualizarDescricao("nova especificação", Instant.now());
        assertThat(ativo.getDescricao()).isEqualTo("nova especificação");
    }

    @Test
    void enviarParaManutencaoExigeStatusDisponivel() {
        AtivoPatrimonial ativo = ativoDisponivel();
        ativo.enviarParaManutencao(Instant.now());
        assertThat(ativo.getStatus()).isEqualTo(AtivoStatus.EM_MANUTENCAO);

        assertThatThrownBy(() -> ativo.enviarParaManutencao(Instant.now()))
                .isInstanceOf(AtivoManutencaoInvalidaException.class);
    }

    @Test
    void manutencaoTemPrecedenciaSobreReservado() {
        AtivoPatrimonial ativo = ativoDisponivel();
        ativo.enviarParaManutencao(Instant.now());
        ativo.marcarComoReservado(Instant.now());
        assertThat(ativo.getStatus()).isEqualTo(AtivoStatus.EM_MANUTENCAO);
    }

    @Test
    void concluirManutencaoVoltaParaDisponivel() {
        AtivoPatrimonial ativo = ativoDisponivel();
        ativo.enviarParaManutencao(Instant.now());
        ativo.concluirManutencao(Instant.now());
        assertThat(ativo.getStatus()).isEqualTo(AtivoStatus.DISPONIVEL);
    }

    @Test
    void excluirImpedeNovasOperacoes() {
        AtivoPatrimonial ativo = ativoDisponivel();
        ativo.excluir(Instant.now());
        assertThat(ativo.estaExcluido()).isTrue();
        assertThatThrownBy(() -> ativo.atualizarDescricao("x", Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }
}
