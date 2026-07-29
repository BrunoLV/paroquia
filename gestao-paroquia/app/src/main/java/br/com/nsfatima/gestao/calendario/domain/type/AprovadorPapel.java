package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Locale;

public enum AprovadorPapel {
    PAROCO("paroco", "Pároco"),
    COORD_CONSELHO("conselho-coordenador", "Coordenador do Conselho"),
    VICE_COORD_CONSELHO("conselho-vice-coordenador", "Vice-Coordenador do Conselho");

    private final String storedCode;
    private final String descricao;

    AprovadorPapel(String storedCode, String descricao) {
        this.storedCode = storedCode;
        this.descricao = descricao;
    }

    public static AprovadorPapel fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");

        if ("conselho-coordenador".equals(normalized) || "conselho_coordenador".equalsIgnoreCase(rawValue.trim())) {
            return COORD_CONSELHO;
        }
        if ("conselho-vice-coordenador".equals(normalized) || "conselho_vice_coordenador".equalsIgnoreCase(rawValue.trim())) {
            return VICE_COORD_CONSELHO;
        }

        for (AprovadorPapel value : values()) {
            if (value.storedCode.equals(normalized) || value.name().equalsIgnoreCase(rawValue.trim())) {
                return value;
            }
        }
        return null;
    }

    public static AprovadorPapel resolveForApproval(String role, String organizationType) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(role);
        TipoOrganizacao tipoOrganizacao = TipoOrganizacao.fromStoredValue(organizationType);

        if (papel == PapelOrganizacional.PAROCO || papel == PapelOrganizacional.BISPO_DIOCESANO) {
            return PAROCO;
        }
        boolean isViceOrMembroCpp = papel == PapelOrganizacional.MEMBRO_CPP
                || papel == PapelOrganizacional.MEMBRO_CPC
                || papel == PapelOrganizacional.MESA_CPP;
        if ((tipoOrganizacao == TipoOrganizacao.CONSELHO_PASTORAL || tipoOrganizacao == TipoOrganizacao.CPP || tipoOrganizacao == TipoOrganizacao.CPC)
                && isViceOrMembroCpp) {
            return VICE_COORD_CONSELHO;
        }

        return COORD_CONSELHO;
    }

    public String storedValue() {
        return storedCode;
    }

    public String getDescricao() {
        return descricao;
    }
}
