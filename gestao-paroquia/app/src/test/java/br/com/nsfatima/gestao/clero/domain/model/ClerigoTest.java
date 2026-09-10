package br.com.nsfatima.gestao.clero.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClerigoTest {

    private static final Instant AGORA = Instant.parse("2026-06-01T12:00:00Z");

    private DadosCadastraisClerigo dados(GrauOrdem grau, LocalDate diaconal, LocalDate presbiteral) {
        return new DadosCadastraisClerigo("João da Silva", "Padre João", LocalDate.of(1980, 1, 1),
                "bra", "12345678901", "1234567", null, "padre@igreja.test", "11999998888", true,
                "1133334444", "Rua da Igreja, 1", grau, "Diocese de Teste",
                diaconal, presbiteral, null);
    }

    @Test
    void cadastrarNormalizaPaisEValidaOrdenacoes() {
        Clerigo c = Clerigo.cadastrar(UUID.randomUUID(),
                dados(GrauOrdem.PADRE_DIOCESANO, LocalDate.of(2005, 1, 1), LocalDate.of(2006, 1, 1)),
                "admin", AGORA);
        assertThat(c.getPaisOrigem()).isEqualTo("BRA");
        assertThat(c.isAtivo()).isTrue();
        assertThat(c.getCriadoPor()).isEqualTo("admin");
    }

    @Test
    void cadastrarRejeitaOrdenacoesInconsistentes() {
        assertThatThrownBy(() -> Clerigo.cadastrar(UUID.randomUUID(),
                dados(GrauOrdem.PADRE_DIOCESANO, LocalDate.of(2005, 1, 1), null), "admin", AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void inativarMarcaSoftDelete() {
        Clerigo c = Clerigo.cadastrar(UUID.randomUUID(),
                dados(GrauOrdem.DIACONO_PERMANENTE, LocalDate.of(2010, 1, 1), null), "admin", AGORA);
        c.inativar("outro-admin", AGORA);
        assertThat(c.isAtivo()).isFalse();
        assertThat(c.getDeletedAt()).isEqualTo(AGORA);
        assertThat(c.getAtualizadoPor()).isEqualTo("outro-admin");
    }

    @Test
    void atualizarCadastroRevalidaOrdenacoes() {
        Clerigo c = Clerigo.cadastrar(UUID.randomUUID(),
                dados(GrauOrdem.DIACONO_PERMANENTE, LocalDate.of(2010, 1, 1), null), "admin", AGORA);
        assertThatThrownBy(() -> c.atualizarCadastro(
                dados(GrauOrdem.BISPO, LocalDate.of(2010, 1, 1), null), "admin", AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
