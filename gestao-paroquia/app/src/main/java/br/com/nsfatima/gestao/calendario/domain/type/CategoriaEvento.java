package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum que define as categorias de eventos da paróquia.
 */
public enum CategoriaEvento {
    PASTORAL,
    SOCIAL,
    LITURGICO,
    ADMINISTRATIVO,
    SACRAMENTAL,
    FORMATIVO,
    ASSISTENCIAL;

    @JsonCreator
    public static CategoriaEvento fromValue(String value) {
        return EnumRequestNormalizer.normalize(value, CategoriaEvento.class);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
