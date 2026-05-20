package br.com.nsfatima.gestao.governanca.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum AnoParoquialStatus {
    PLANEJAMENTO,
    FECHADO;

    @JsonCreator
    public static AnoParoquialStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, AnoParoquialStatus.class);
    }
}
