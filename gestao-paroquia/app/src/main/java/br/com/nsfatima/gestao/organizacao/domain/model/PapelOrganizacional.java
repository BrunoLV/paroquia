package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.Locale;

public enum PapelOrganizacional {
    ADMIN("admin", "Administrador do Sistema"),
    BISPO_DIOCESANO("bispo-diocesano", "Bispo Diocesano"),
    PAROCO("paroco", "Pároco"),
    VIGARIO("vigario", "Vigário Paroquial"),
    DIACONO("diacono", "Diácono"),
    PADRE("padre", "Padre / Sacerdote"),
    MESA_CPP("mesa-cpp", "Mesa Diretora do CPP"),
    MEMBRO_CPP("membro-cpp", "Membro do CPP"),
    MEMBRO_CAE("membro-cae", "Membro do CAE"),
    PRESIDENTE_CPC("presidente-cpc", "Presidente do CPC"),
    MEMBRO_CPC("membro-cpc", "Membro do CPC"),
    COORD_PASTORAL_GLOBAL("coord-pastoral-global", "Coordenador Pastoral Global"),
    COORD_PASTORAL_LOCAL("coord-pastoral-local", "Coordenador Pastoral Local"),
    COORD_PASTORAL("coord-pastoral", "Coordenador Pastoral"),
    COORD_MOVIMENTO("coord-movimento", "Coordenador de Movimento"),
    VOLUNTARIO_PASTORAL_LOCAL("voluntario-pastoral-local", "Voluntário Pastoral Local"),
    MEMBRO_PASTORAL("membro-pastoral", "Membro de Pastoral"),
    MEMBRO_MOVIMENTO("membro-movimento", "Membro de Movimento"),
    SECRETARIO("secretario", "Secretário");

    private final String storedCode;
    private final String descricao;

    PapelOrganizacional(String storedCode, String descricao) {
        this.storedCode = storedCode;
        this.descricao = descricao;
    }

    public static PapelOrganizacional fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");

        // Legado / Aliases de compatibilidade
        if ("mesa-diretora-cpp".equals(normalized) || "mesa_diretora_cpp".equalsIgnoreCase(rawValue.trim())) {
            return MESA_CPP;
        }
        if ("coordenador-pastoral-global".equals(normalized) || "coordenador_pastoral_global".equalsIgnoreCase(rawValue.trim())) {
            return COORD_PASTORAL_GLOBAL;
        }
        if ("coordenador-pastoral-local".equals(normalized) || "coordenador_pastoral_local".equalsIgnoreCase(rawValue.trim())) {
            return COORD_PASTORAL_LOCAL;
        }
        if ("coordenador-pastoral".equals(normalized) || "coordenador_pastoral".equalsIgnoreCase(rawValue.trim())) {
            return COORD_PASTORAL;
        }
        if ("coordenador-movimento".equals(normalized) || "coordenador_movimento".equalsIgnoreCase(rawValue.trim())) {
            return COORD_MOVIMENTO;
        }

        for (PapelOrganizacional role : values()) {
            if (role.storedCode.equals(normalized) || role.name().equalsIgnoreCase(rawValue.trim())) {
                return role;
            }
        }
        return null;
    }

    public String storedValue() {
        return storedCode;
    }

    public String getDescricao() {
        return descricao;
    }
}
