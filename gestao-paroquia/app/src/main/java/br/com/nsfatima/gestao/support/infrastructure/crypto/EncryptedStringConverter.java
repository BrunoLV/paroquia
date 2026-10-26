package br.com.nsfatima.gestao.support.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Conversor JPA que cifra/decifra transparentemente campos {@link String} sensíveis (dados
 * pessoais sujeitos à LGPD, ex.: CPF, telefone, endereço) usando {@link CryptoUtils}. Deve ser
 * aplicado via {@code @Convert} nos campos de entidade que armazenam esse tipo de dado, para que o
 * valor nunca seja persistido em texto claro no banco.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    /**
     * Cifra o valor antes de persistir (sempre no formato GCM versionado corrente).
     *
     * @param attribute valor em texto claro a ser cifrado; {@code null} é persistido como {@code null}
     * @return valor cifrado, ou {@code null} se o atributo for nulo
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? CryptoUtils.encrypt(attribute) : null;
    }

    /**
     * Decifra o valor lido do banco. Valores com forma de ciphertext ({@link CryptoUtils#pareceCifrado})
     * são decifrados e, se a decifragem falhar (chave divergente, dado adulterado), a exceção propaga: o
     * ciphertext jamais é devolvido como se fosse o valor. Valores sem forma de ciphertext são registros
     * em claro anteriores à adoção da cifragem (fase de expansão da migração) e são lidos como texto; a
     * migração de dados e a próxima gravação os cifram.
     *
     * @param dbData valor bruto lido da coluna do banco
     * @return valor decifrado ou, para registro legado em claro, o próprio texto
     * @throws IllegalStateException se o valor tem forma de ciphertext e não pode ser decifrado
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || !CryptoUtils.pareceCifrado(dbData)) {
            return dbData;
        }
        return CryptoUtils.decrypt(dbData);
    }
}
