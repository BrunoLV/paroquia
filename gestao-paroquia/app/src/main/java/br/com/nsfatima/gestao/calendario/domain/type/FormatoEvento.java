package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Formato de realização do evento no calendário paroquial.
 * Pode ser Presencial, Online (videoconferência/remoto) ou Híbrido (transmissão e atividades físicas conjuntas).
 */
public enum FormatoEvento {
    PRESENCIAL("Presencial"),
    ONLINE("Online"),
    HIBRIDO("Híbrido");

    private final String descricao;

    FormatoEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static FormatoEvento fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PRESENCIAL;
        }
        return EnumRequestNormalizer.normalize(value, FormatoEvento.class);
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
