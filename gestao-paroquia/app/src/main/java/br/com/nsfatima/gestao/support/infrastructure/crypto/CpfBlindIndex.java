package br.com.nsfatima.gestao.support.infrastructure.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

/**
 * Blind index do CPF: HMAC-SHA256 com chave secreta própria (<em>pepper</em>), distinta da chave de
 * cifragem. Um SHA-256 puro de CPF é reversível por força bruta (apenas 10^9 combinações); com o pepper
 * fora do banco, um vazamento da base não permite recomputar nem confirmar CPFs. O índice permite
 * unicidade e busca exata sem decifrar a base. Como {@link CryptoUtils}, o estado é estático e alimentado
 * por {@link CryptoInitializer} a partir da propriedade {@code app.security.cpf-pepper}, que
 * deliberadamente não possui valor padrão no código.
 */
public final class CpfBlindIndex {

    private static final String HMAC = "HmacSHA256";
    private static final String MSG_PEPPER = "Propriedade app.security.cpf-pepper é obrigatória.";
    private static byte[] pepper;

    private CpfBlindIndex() {
    }

    /**
     * Define o pepper do índice.
     *
     * @param valor segredo em texto; não pode ser nulo nem em branco
     * @throws IllegalStateException se o segredo não for informado
     */
    public static void setPepper(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(MSG_PEPPER);
        }
        pepper = valor.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Calcula o índice do CPF já normalizado (somente dígitos).
     *
     * @param cpfDigitos CPF contendo apenas dígitos; {@code null} resulta em {@code null}
     * @return HMAC-SHA256 em hexadecimal (64 caracteres), ou {@code null}
     * @throws IllegalStateException se o pepper não tiver sido configurado
     */
    public static String hash(String cpfDigitos) {
        if (cpfDigitos == null) {
            return null;
        }
        if (pepper == null) {
            throw new IllegalStateException(MSG_PEPPER);
        }
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(pepper, HMAC));
            return HexFormat.of().formatHex(mac.doFinal(cpfDigitos.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 indisponível", e);
        }
    }

    /**
     * Normaliza um CPF para dígitos e calcula o índice.
     *
     * @param cpf CPF com ou sem máscara
     * @return índice do CPF normalizado, ou {@code null} se a entrada for nula
     */
    public static String hashDeCpf(String cpf) {
        return cpf == null ? null : hash(cpf.replaceAll("\\D", ""));
    }
}
