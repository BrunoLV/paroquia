package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Locale;

public enum AprovadorPapel {
    PAROCO("paroco"),
    CONSELHO_COORDENADOR("conselho-coordenador"),
    CONSELHO_VICE_COORDENADOR("conselho-vice-coordenador");

    private final String storedCode;

    AprovadorPapel(String storedCode) {
        this.storedCode = storedCode;
    }

    public static AprovadorPapel fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        for (AprovadorPapel value : values()) {
            if (value.storedCode.equals(normalized)) {
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
        boolean isViceOrMembroCpp = papel == PapelOrganizacional.VICE_COORDENADOR
                || papel == PapelOrganizacional.MEMBRO_CPP;
        if (tipoOrganizacao == TipoOrganizacao.CONSELHO_PASTORAL && isViceOrMembroCpp) {
            return CONSELHO_VICE_COORDENADOR;
        }

        return CONSELHO_COORDENADOR;
    }


    public String storedValue() {
        return storedCode;
    }
}
