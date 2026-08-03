package br.com.nsfatima.gestao.support.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

/**
 * Conversor JPA que cifra/decifra transparentemente campos {@link LocalDate} sensíveis (dados
 * pessoais sujeitos à LGPD, ex.: datas de nascimento) usando {@link CryptoUtils}. Deve ser aplicado
 * via {@code @Convert} nos campos de entidade que armazenam esse tipo de dado, para que o valor
 * nunca seja persistido em texto claro no banco.
 */
@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {
    /**
     * Cifra a data antes de persistir, delegando a {@link CryptoUtils#encrypt(String)} sobre sua
     * representação ISO-8601 ({@link LocalDate#toString()}).
     *
     * @param attribute data a ser cifrada; {@code null} é persistido como {@code null}
     * @return representação cifrada da data, ou {@code null} se o atributo for nulo
     */
    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return attribute != null ? CryptoUtils.encrypt(attribute.toString()) : null;
    }

    /**
     * Decifra o valor lido do banco e o converte de volta para {@link LocalDate}. Caso a
     * decifragem falhe, tenta interpretar o dado bruto como uma data ISO-8601 em texto claro — isso
     * garante compatibilidade com registros gravados antes da adoção da cifragem (migração
     * gradual), retornando {@code null} apenas se ambas as tentativas falharem.
     *
     * @param dbData valor bruto lido da coluna do banco
     * @return data decifrada, data lida em texto claro (fallback de compatibilidade), ou {@code null}
     *         se o dado estiver em branco ou não puder ser interpretado por nenhuma das duas vias
     */
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
