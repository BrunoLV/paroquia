package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum FrequenciaRecorrenciaInput {
    DIARIA,
    SEMANAL,
    MENSAL;

    @JsonCreator
    public static FrequenciaRecorrenciaInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, FrequenciaRecorrenciaInput.class);
    }
}
