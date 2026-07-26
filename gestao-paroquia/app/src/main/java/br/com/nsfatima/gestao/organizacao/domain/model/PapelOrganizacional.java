package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.Locale;

public enum PapelOrganizacional {
    ADMIN("admin"),
    COORDENADOR("coordenador"),
    VICE_COORDENADOR("vice-coordenador"),
    MEMBRO("membro"),
    PAROCO("paroco"),
    VIGARIO("vigario"),
    PADRE("padre"),
    SECRETARIO("secretario");

    private final String storedCode;

    PapelOrganizacional(String storedCode) {
        this.storedCode = storedCode;
    }

    public static PapelOrganizacional fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        for (PapelOrganizacional role : values()) {
            if (role.storedCode.equals(normalized)) {
                return role;
            }
        }
        return null;
    }

    public String storedValue() {
        return storedCode;
    }
}
