package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CryptoConverterTest {

    @BeforeAll
    static void setUp() {
        CryptoUtils.setSecretKey(PepperDeTesteExtension.CHAVE);
    }

    @Test
    void deveCriptografarEDescriptografarString() {
        String original = "João da Silva";
        String encrypted = CryptoUtils.encrypt(original);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        String decrypted = CryptoUtils.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void deveCriptografarEDescriptografarLocalData() {
        LocalDate original = LocalDate.of(1990, 5, 15);
        EncryptedLocalDateConverter converter = new EncryptedLocalDateConverter();

        String encrypted = converter.convertToDatabaseColumn(original);
        assertNotNull(encrypted);
        assertNotEquals(original.toString(), encrypted);

        LocalDate decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void deveTratarNuloValores() {
        EncryptedStringConverter stringConverter = new EncryptedStringConverter();
        assertNull(stringConverter.convertToDatabaseColumn(null));
        assertNull(stringConverter.convertToEntityAttribute(null));

        EncryptedLocalDateConverter localDateConverter = new EncryptedLocalDateConverter();
        assertNull(localDateConverter.convertToDatabaseColumn(null));
        assertNull(localDateConverter.convertToEntityAttribute(null));
    }
}
