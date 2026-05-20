package br.com.nsfatima.gestao.calendario.infrastructure.persistence.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import java.time.DayOfWeek;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegraRecorrenciaConverterTest {

    private final RegraRecorrenciaConverter converter = new RegraRecorrenciaConverter();

    @Test
    void shouldConvertRoundTrip() {
        RegraRecorrencia rule = new RegraRecorrencia(
                "SEMANAL",
                2,
                List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                null, null, null, null,
                null);

        String json = converter.convertToDatabaseColumn(rule);
        RegraRecorrencia result = converter.convertToEntityAttribute(json);

        assertEquals(rule, result);
    }

    @Test
    void shouldHandleNulls() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
    }
}
