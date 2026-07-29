package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.Locale;

public enum PapelOrganizacional {
    ADMIN("admin"),
    BISPO_DIOCESANO("bispo-diocesano"),
    PAROCO("paroco"),
    VIGARIO("vigario"),
    DIACONO("diacono"),
    PADRE("padre"),
    MESA_DIRETORA_CPP("mesa-diretora-cpp"),
    MEMBRO_CPP("membro-cpp"),
    MEMBRO_CAE("membro-cae"),
    PRESIDENTE_CPC("presidente-cpc"),
    MEMBRO_CPC("membro-cpc"),
    COORDENADOR_PASTORAL_GLOBAL("coordenador-pastoral-global"),
    COORDENADOR_PASTORAL_LOCAL("coordenador-pastoral-local"),
    COORDENADOR_PASTORAL("coordenador-pastoral"),
    COORDENADOR_MOVIMENTO("coordenador-movimento"),
    VOLUNTARIO_PASTORAL_LOCAL("voluntario-pastoral-local"),
    MEMBRO_PASTORAL("membro-pastoral"),
    MEMBRO_MOVIMENTO("membro-movimento"),
    SECRETARIO("secretario");


    private final String storedCode;

    PapelOrganizacional(String storedCode) {
        this.storedCode = storedCode;
    }

    public static PapelOrganizacional fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");
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
