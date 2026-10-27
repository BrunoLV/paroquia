package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Blind index de CPF por HMAC-SHA256 com pepper")
class CpfBlindIndexTest {

    @AfterEach
    void restaurarPepper() {
        CpfBlindIndex.setPepper(PepperDeTesteExtension.PEPPER);
    }

    @Test
    @DisplayName("não coincide com o SHA-256 puro e é determinístico")
    void naoCoincideComSha256Puro() throws Exception {
        String sha = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest("52998224725".getBytes(StandardCharsets.UTF_8)));

        assertThat(CpfBlindIndex.hashDeCpf("529.982.247-25"))
                .isNotEqualTo(sha)
                .isEqualTo(CpfBlindIndex.hash("52998224725"))
                .hasSize(64);
    }

    @Test
    @DisplayName("pepper diferente gera índice diferente")
    void pepperDiferenteMudaIndice() {
        String original = CpfBlindIndex.hash("52998224725");
        CpfBlindIndex.setPepper("outro-pepper");

        assertThat(CpfBlindIndex.hash("52998224725")).isNotEqualTo(original);
    }

    @Test
    @DisplayName("pepper ausente é rejeitado, sem valor padrão")
    void pepperAusenteERejeitado() {
        assertThatThrownBy(() -> CpfBlindIndex.setPepper(" ")).isInstanceOf(IllegalStateException.class);
    }
}
