package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.Locale;

public enum PapelOrganizacional {
    ADMIN("admin"),
    BISPO_DIOCESANO("bispo-diocesano"),
    PAROCO("paroco"),
    VIGARIO("vigario"),
    DIACONO("diacono"),
    PADRE("padre"),
    MEMBRO_CPP("membro-cpp"),
    MEMBRO_CAE("membro-cae"),
    COORDENADOR_PASTORAL("coordenador-pastoral"),
    COORDENADOR_MOVIMENTO("coordenador-movimento"),
    MEMBRO_PASTORAL("membro-pastoral"),
    MEMBRO_MOVIMENTO("membro-movimento"),
    COORDENADOR("coordenador"),
    VICE_COORDENADOR("vice-coordenador"),
    MEMBRO("membro"),
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
