package br.com.nsfatima.gestao.calendario.infrastructure.persistence.converter;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Converter(autoApply = true)
public class RegraRecorrenciaConverter implements AttributeConverter<RegraRecorrencia, String> {

    private static final ObjectMapper OBJECT_MAPPER =
            tools.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();

    @Override
    public String convertToDatabaseColumn(RegraRecorrencia attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Error converting RegraRecorrencia to JSON", e);
        }
    }

    @Override
    public RegraRecorrencia convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, RegraRecorrencia.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Error converting JSON to RegraRecorrencia", e);
        }
    }
}
