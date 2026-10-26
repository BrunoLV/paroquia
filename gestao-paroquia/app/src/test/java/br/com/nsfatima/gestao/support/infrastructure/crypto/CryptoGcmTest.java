package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cifragem AES-GCM versionada com leitura do formato CBC legado")
class CryptoGcmTest {

    private static final String CHAVE = "chave-de-teste-gcm";

    @BeforeAll
    static void configurarChave() {
        CryptoUtils.setSecretKey(CHAVE);
    }

    private static String cifrarEmCbcLegado(String claro) throws Exception {
        byte[] chave = MessageDigest.getInstance("SHA-256").digest(CHAVE.getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[16];
        iv[0] = 7;
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(chave, "AES"), new IvParameterSpec(iv));
        byte[] cifrado = cipher.doFinal(claro.getBytes(StandardCharsets.UTF_8));
        byte[] combinado = new byte[iv.length + cifrado.length];
        System.arraycopy(iv, 0, combinado, 0, iv.length);
        System.arraycopy(cifrado, 0, combinado, iv.length, cifrado.length);
        return Base64.getEncoder().encodeToString(combinado);
    }

    @Test
    @DisplayName("grava em GCM com prefixo de versão de chave e IV distinto a cada chamada")
    void gravaEmGcmVersionado() {
        String a = CryptoUtils.encrypt("52998224725");
        String b = CryptoUtils.encrypt("52998224725");

        assertThat(a).startsWith("v1:").isNotEqualTo(b);
        assertThat(CryptoUtils.decrypt(a)).isEqualTo("52998224725");
    }

    @Test
    @DisplayName("decifra registro legado CBC sem prefixo")
    void decifraLegadoCbc() throws Exception {
        String legado = cifrarEmCbcLegado("João da Silva Sauro");

        assertThat(CryptoUtils.pareceCifrado(legado)).isTrue();
        assertThat(new EncryptedStringConverter().convertToEntityAttribute(legado)).isEqualTo("João da Silva Sauro");
    }

    @Test
    @DisplayName("falha de decifragem lança erro e nunca devolve o ciphertext como valor")
    void falhaLancaErro() {
        String adulterado = "v1:" + Base64.getEncoder().encodeToString(new byte[48]);
        EncryptedStringConverter conversor = new EncryptedStringConverter();

        assertThatThrownBy(() -> conversor.convertToEntityAttribute(adulterado))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageNotContaining(adulterado);
    }

    @Test
    @DisplayName("chave divergente falha na decifragem do formato GCM")
    void chaveDivergenteFalha() {
        String cifrado = CryptoUtils.encrypt("segredo");
        CryptoUtils.setSecretKey("outra-chave-qualquer");
        try {
            assertThatThrownBy(() -> CryptoUtils.decrypt(cifrado)).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> new EncryptedLocalDateConverter().convertToEntityAttribute(cifrado))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            CryptoUtils.setSecretKey(CHAVE);
        }
    }

    @Test
    @DisplayName("registro em claro anterior à cifragem é lido como texto e data")
    void registroEmClaroELidoComoTexto() {
        assertThat(CryptoUtils.pareceCifrado("Maria da Silva")).isFalse();
        assertThat(new EncryptedStringConverter().convertToEntityAttribute("maria@exemplo.com"))
                .isEqualTo("maria@exemplo.com");
        assertThat(new EncryptedLocalDateConverter().convertToEntityAttribute("1990-05-15"))
                .isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @AfterAll
    static void restaurarChavePadrao() {
        CryptoUtils.setSecretKey(PepperDeTesteExtension.CHAVE);
    }
}
