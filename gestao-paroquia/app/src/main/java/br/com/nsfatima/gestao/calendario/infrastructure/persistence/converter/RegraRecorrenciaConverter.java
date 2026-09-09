package br.com.nsfatima.gestao.calendario.infrastructure.persistence.converter;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converte o value object {@link RegraRecorrencia} (regra de recorrência de um evento
 * recorrente, ex.: frequência, intervalo, dias da semana) para uma representação em
 * texto JSON persistida na coluna {@code regra_json}, e vice-versa. A serialização
 * em JSON evita criar uma tabela/coluna dedicada para cada atributo da regra, permitindo
 * evoluir o esquema de recorrência sem nova migração Flyway.
 */
@Converter(autoApply = true)
public class RegraRecorrenciaConverter implements AttributeConverter<RegraRecorrencia, String> {

    private static final ObjectMapper OBJECT_MAPPER =
            JsonMapper.builder().findAndAddModules().build();

    /**
     * Serializa a regra de recorrência para JSON antes de persistir no banco.
     *
     * @param attribute regra de recorrência do evento, ou {@code null} quando o evento não é recorrente
     * @return representação JSON da regra, ou {@code null} se o atributo for {@code null}
     * @throws IllegalArgumentException se a serialização para JSON falhar
     */
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

    /**
     * Desserializa o JSON armazenado na coluna {@code regra_json} de volta para o value
     * object {@link RegraRecorrencia}.
     *
     * @param dbData conteúdo JSON lido do banco, podendo ser {@code null} ou vazio
     * @return a regra de recorrência reconstruída, ou {@code null} se não houver dado
     * @throws IllegalArgumentException se o JSON armazenado for inválido ou incompatível
     */
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
