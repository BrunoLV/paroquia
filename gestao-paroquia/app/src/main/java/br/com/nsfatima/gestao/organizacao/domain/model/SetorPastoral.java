package br.com.nsfatima.gestao.organizacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SetorPastoral {
    SANTIFICACAO_LITURGIA,
    FORMACAO_ENSINAR,
    SOCIAL_CARIDADE;

    @JsonCreator
    public static SetorPastoral fromValue(String value) {
        return EnumRequestNormalizer.normalize(value, SetorPastoral.class);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
