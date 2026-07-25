package br.com.nsfatima.gestao.support.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {
    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return attribute != null ? CryptoUtils.encrypt(attribute.toString()) : null;
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(CryptoUtils.decrypt(dbData));
        } catch (Exception e) {
            try {
                return LocalDate.parse(dbData);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
