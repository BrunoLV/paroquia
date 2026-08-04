package br.com.nsfatima.gestao.support.infrastructure.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário estático de criptografia simétrica usado para cifrar/decifrar campos sensíveis (dados
 * pessoais sujeitos à LGPD) antes de persistência, via os conversores JPA deste pacote
 * ({@link EncryptedStringConverter}, {@link EncryptedLocalDateConverter}). Usa AES/CBC/PKCS5Padding
 * com um IV (vetor de inicialização) aleatório gerado a cada chamada de {@link #encrypt(String)} e
 * prefixado ao texto cifrado resultante — isso garante que o mesmo valor em claro produza
 * ciframentos distintos a cada execução (proteção contra análise de padrões/ataques de dicionário
 * sobre colunas cifradas), sem exigir armazenamento separado do IV. A chave configurada é sempre
 * normalizada via SHA-256 antes de virar {@link SecretKeySpec}, garantindo o tamanho de 256 bits
 * exigido pelo AES independentemente do tamanho da chave configurada em texto.
 */
public class CryptoUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;
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
     * ignoradas silenciosamente, preservando a chave de fallback definida no bloco estático — isso
     * evita que uma configuração ausente em ambiente de desenvolvimento derrube a aplicação.
     *
     * @param key chave secreta em texto (tipicamente vinda de configuração externa/variável de ambiente)
     * @throws RuntimeException se o algoritmo de digest SHA-256 não estiver disponível na JVM
     */
    public static void setSecretKey(String key) {
        try {
            if (key == null || key.isBlank()) {
                return;
            }
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption key", e);
        }
    }

    /**
     * Cifra o valor informado com AES/CBC/PKCS5Padding, usando um IV aleatório novo a cada chamada.
     * O IV gerado é concatenado antes dos bytes cifrados e o resultado combinado é codificado em
     * Base64, de forma que o próprio valor de saída carregue tudo que {@link #decrypt(String)}
     * precisa para reverter a operação, sem exigir uma coluna adicional para o IV.
     *
     * @param value valor em texto claro a ser cifrado; {@code null} é retornado como {@code null}
     * @return valor cifrado codificado em Base64 (IV + texto cifrado), ou {@code null} se a entrada for nula
     * @throws RuntimeException se ocorrer falha na operação de cifragem (chave inválida, algoritmo indisponível, etc.)
     */
    public static String encrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_SIZE];
            RANDOM.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_SIZE + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_SIZE);
            System.arraycopy(encryptedBytes, 0, combined, IV_SIZE, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during encryption", e);
        }
    }

    /**
     * Decifra um valor previamente cifrado por {@link #encrypt(String)}, extraindo o IV dos
     * primeiros {@link #IV_SIZE} bytes do valor decodificado de Base64 antes de decifrar o restante.
     *
     * @param value valor cifrado codificado em Base64 (IV + texto cifrado); {@code null} é retornado como {@code null}
     * @return valor original em texto claro, ou {@code null} se a entrada for nula
     * @throws RuntimeException se o valor estiver corrompido, com tamanho inválido, ou se a chave/algoritmo não
     *     corresponderem
     */
    public static String decrypt(String value) {
        if (value == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(value);
            if (combined.length < IV_SIZE) {
                throw new IllegalArgumentException("Invalid encrypted data length");
            }

            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            int encryptedSize = combined.length - IV_SIZE;
            byte[] encryptedBytes = new byte[encryptedSize];
            System.arraycopy(combined, IV_SIZE, encryptedBytes, 0, encryptedSize);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during decryption", e);
        }
    }
}
