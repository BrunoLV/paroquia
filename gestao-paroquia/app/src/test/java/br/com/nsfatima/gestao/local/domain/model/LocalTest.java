package br.com.nsfatima.gestao.local.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalTest {

    private Local local(int capacidade) {
        return new Local(UUID.randomUUID(), "Salão", "SALAO", EnderecoLocal.vazio(), capacidade, "obs", true, null);
    }

    @Test
    void ctorRejeitaCapacidadeNaoPositiva() {
        assertThatThrownBy(() -> local(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> local(-5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void alterarCadastroNaoMexeNoCicloDeVida() {
        Local l = local(10);
        l.inativar();
        l.alterarCadastro("Novo", "SALA", EnderecoLocal.vazio(), 30, "nova obs", UUID.randomUUID());
        assertThat(l.getNome()).isEqualTo("Novo");
        assertThat(l.getCapacidadeMaxima()).isEqualTo(30);
        assertThat(l.isAtivo()).isFalse();
    }

    @Test
    void alterarCadastroRejeitaCapacidadeNaoPositiva() {
        Local l = local(10);
        assertThatThrownBy(() -> l.alterarCadastro("x", "SALA", EnderecoLocal.vazio(), 0, "o", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
