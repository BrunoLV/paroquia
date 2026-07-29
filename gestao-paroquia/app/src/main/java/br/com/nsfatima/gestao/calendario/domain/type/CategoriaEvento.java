package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum que define as categorias de eventos da paróquia.
 */
public enum CategoriaEvento {
    SOLENIDADE(1),
    LITURGICO(2),
    SACRAMENTAL(2),
    PASTORAL(3),
    FORMATIVO(3),
    ADMINISTRATIVO(3),
    ASSISTENCIAL(3),
    SOCIAL(3),
    MOVIMENTO(4);

    private final int nivelPrioridade;

    CategoriaEvento(int nivelPrioridade) {
        this.nivelPrioridade = nivelPrioridade;
    }

    public int getNivelPrioridade() {
        return nivelPrioridade;
    }

    @JsonCreator
    public static CategoriaEvento fromValue(String value) {
        return EnumRequestNormalizer.normalize(value, CategoriaEvento.class);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
