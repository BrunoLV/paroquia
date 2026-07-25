package br.com.nsfatima.gestao.support.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? CryptoUtils.encrypt(attribute) : null;
    }

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
