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
     * Cifra o valor antes de persistir.
     *
     * @param attribute valor em texto claro a ser cifrado; {@code null} é persistido como {@code null}
     * @return valor cifrado, ou {@code null} se o atributo for nulo
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? CryptoUtils.encrypt(attribute) : null;
    }

    /**
     * Decifra o valor lido do banco. Caso a decifragem falhe, retorna o dado bruto inalterado —
     * isso garante compatibilidade com registros gravados antes da adoção da cifragem (migração
     * gradual), tratando-os como texto já em claro.
     *
     * @param dbData valor bruto lido da coluna do banco
     * @return valor decifrado, o próprio dado bruto (fallback de compatibilidade) se em branco ou
     *         se a decifragem falhar
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            return CryptoUtils.decrypt(dbData);
        } catch (Exception e) {
            return dbData;
        }
    }
}
