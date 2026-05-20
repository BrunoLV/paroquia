package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.Locale;

public enum PapelOrganizacional {
    COORDENADOR("coordenador"),
    VICE_COORDENADOR("vice-coordenador"),
    MEMBRO("membro"),
    PAROCO("paroco"),
    VIGARIO("vigario"),
    PADRE("padre"),
    SECRETARIO("secretario");

    private final String storedValue;

    PapelOrganizacional(String storedValue) {
        this.storedValue = storedValue;
    }

    public String storedValue() {
        return storedValue;
    }

    public static PapelOrganizacional fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        for (PapelOrganizacional role : values()) {
            if (role.storedValue.equals(normalized)) {
                return role;
            }
        }
        return null;
    }
}
