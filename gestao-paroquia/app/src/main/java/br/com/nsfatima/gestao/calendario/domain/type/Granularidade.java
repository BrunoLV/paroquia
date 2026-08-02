package br.com.nsfatima.gestao.calendario.domain.type;

public enum Granularidade {
    DIARIO,
    SEMANAL,
    MENSAL,
    ANUAL;

    public static Granularidade fromCode(String rawValue) {
        for (Granularidade constante : values()) {
            if (constante.name().equalsIgnoreCase(rawValue)) {
                return constante;
            }
        }
        return null;
    }
}
