package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.support.domain.EnumRequestNormalizer;

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

    public static FormatoEvento fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PRESENCIAL;
        }
        return EnumRequestNormalizer.normalize(value, FormatoEvento.class);
    }

    public String toValue() {
        return name();
    }
}
