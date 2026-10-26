package br.com.nsfatima.gestao.support.infrastructure.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário estático de criptografia simétrica usado para cifrar/decifrar campos sensíveis (dados
 * pessoais sujeitos à LGPD) antes de persistência, via os conversores JPA deste pacote
 * ({@link EncryptedStringConverter}, {@link EncryptedLocalDateConverter}).
 *
 * <p><strong>Formato corrente (escrita):</strong> {@code v1:} + Base64(IV de 12 bytes + texto cifrado + tag
 * de autenticação) com AES-256/GCM. O prefixo identifica a <em>versão da chave</em>: uma rotação futura
 * introduz {@code v2:} e mantém a leitura de {@code v1:}, sem exigir coluna adicional. GCM é cifra
 * autenticada — qualquer adulteração do texto cifrado falha na decifragem em vez de produzir lixo, o que o
 * CBC legado não garantia. O IV é aleatório por chamada, de modo que o mesmo valor em claro produz
 * ciframentos distintos (sem análise de padrões sobre colunas cifradas).
 *
 * <p><strong>Formato legado (somente leitura):</strong> Base64(IV de 16 bytes + AES/CBC/PKCS5Padding),
 * sem prefixo. Como {@code :} não pertence ao alfabeto Base64, os dois formatos nunca se confundem. Registros
 * legados continuam legíveis e passam a ser gravados em GCM na próxima escrita do atributo.
 *
 * <p>A chave configurada é sempre normalizada via SHA-256 antes de virar {@link SecretKeySpec}, garantindo o
 * tamanho de 256 bits exigido pelo AES independentemente do tamanho da chave em texto. Falhas de decifragem
 * nunca devolvem o ciphertext como se fosse valor e jamais incluem o dado na mensagem de erro.
 */
public class CryptoUtils {

    private static final String ALGORITHM_GCM = "AES/GCM/NoPadding";
    private static final String ALGORITHM_CBC_LEGADO = "AES/CBC/PKCS5Padding";
    private static final String PREFIXO_GCM = "v1:";
    private static final int IV_GCM_SIZE = 12;
    private static final int TAG_GCM_BITS = 128;
    private static final int IV_CBC_SIZE = 16;
    private static final int BLOCO_AES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static SecretKeySpec secretKeySpec;

    static {
        setSecretKey("default-dev-fallback-key-should-be-changed-in-prod");
    }

    private CryptoUtils() {
    }

    /**
     * Define (ou redefine) a chave secreta usada por {@link #encrypt(String)} e
     * {@link #decrypt(String)}, derivando-a via SHA-256 para obter uma chave AES de 256 bits a
     * partir de qualquer chave configurada em texto. Chamadas com chave nula ou em branco são
     * ignoradas silenciosamente, preservando a chave de fallback definida no bloco estático.
     *
     * @param key chave secreta em texto (tipicamente vinda de configuração externa/variável de ambiente)
     * @throws IllegalStateException se o algoritmo de digest SHA-256 não estiver disponível na JVM
     */
    public static void setSecretKey(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialize encryption key", e);
        }
    }

    /**
     * Cifra o valor informado com AES-256/GCM e IV aleatório novo a cada chamada, devolvendo
     * {@code v1:} + Base64(IV + texto cifrado + tag).
     *
     * @param value valor em texto claro; {@code null} é retornado como {@code null}
     * @return valor cifrado versionado, ou {@code null} se a entrada for nula
     * @throws IllegalStateException se ocorrer falha na operação de cifragem
     */
    public static String encrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_GCM_SIZE];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(TAG_GCM_BITS, iv));
            byte[] cifrado = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return PREFIXO_GCM + Base64.getEncoder().encodeToString(concatenar(iv, cifrado));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error occurred during encryption", e);
        }
    }

    /**
     * Decifra um valor produzido por {@link #encrypt(String)} (formato {@code v1:}) ou pelo formato CBC
     * legado (sem prefixo). Qualquer falha — chave divergente, dado adulterado ou truncado — lança exceção
     * sem eco do conteúdo.
     *
     * @param value valor cifrado; {@code null} é retornado como {@code null}
     * @return valor original em texto claro, ou {@code null} se a entrada for nula
     * @throws IllegalStateException se o valor estiver corrompido, adulterado ou a chave não corresponder
     */
    public static String decrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            if (value.startsWith(PREFIXO_GCM)) {
                return decifrarGcm(value.substring(PREFIXO_GCM.length()));
            }
            return decifrarCbcLegado(value);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Error occurred during decryption", e);
        }
    }

    /**
     * Indica se o valor tem o formato de um ciphertext produzido por este utilitário (GCM versionado ou CBC
     * legado), sem tentar decifrá-lo. Serve para distinguir dado cifrado de registro em claro anterior à
     * adoção da cifragem: só o primeiro é decifrado (e falha com erro se a chave não servir); o segundo é
     * tratado como texto e cifrado na próxima gravação ou pela migração de dados.
     *
     * @param value valor lido da coluna
     * @return {@code true} se o valor tem a forma de um ciphertext
     */
    public static boolean pareceCifrado(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (value.startsWith(PREFIXO_GCM)) {
            return true;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(value);
            return bytes.length >= IV_CBC_SIZE + BLOCO_AES && bytes.length % BLOCO_AES == 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String decifrarGcm(String base64) throws GeneralSecurityException {
        byte[] combinado = Base64.getDecoder().decode(base64);
        if (combinado.length <= IV_GCM_SIZE) {
            throw new IllegalArgumentException("Invalid encrypted data length");
        }
        Cipher cipher = Cipher.getInstance(ALGORITHM_GCM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(TAG_GCM_BITS, combinado, 0, IV_GCM_SIZE));
        byte[] claro = cipher.doFinal(combinado, IV_GCM_SIZE, combinado.length - IV_GCM_SIZE);
        return new String(claro, StandardCharsets.UTF_8);
    }

    private static String decifrarCbcLegado(String base64) throws GeneralSecurityException {
        byte[] combinado = Base64.getDecoder().decode(base64);
        if (combinado.length < IV_CBC_SIZE) {
            throw new IllegalArgumentException("Invalid encrypted data length");
        }
        Cipher cipher = Cipher.getInstance(ALGORITHM_CBC_LEGADO);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(combinado, 0, IV_CBC_SIZE));
        byte[] claro = cipher.doFinal(combinado, IV_CBC_SIZE, combinado.length - IV_CBC_SIZE);
        return new String(claro, StandardCharsets.UTF_8);
    }

    private static byte[] concatenar(byte[] iv, byte[] cifrado) {
        byte[] resultado = new byte[iv.length + cifrado.length];
        System.arraycopy(iv, 0, resultado, 0, iv.length);
        System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);
        return resultado;
    }
}
