package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import java.util.Locale;

public enum AprovadorPapel {
    PAROCO("paroco"),
    CONSELHO_COORDENADOR("conselho-coordenador"),
    CONSELHO_VICE_COORDENADOR("conselho-vice-coordenador");

    private final String storedValue;

    AprovadorPapel(String storedValue) {
        this.storedValue = storedValue;
    }

    public String storedValue() {
        return storedValue;
    }

    public static AprovadorPapel fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        for (AprovadorPapel value : values()) {
            if (value.storedValue.equals(normalized)) {
                return value;
            }
        }
        return null;
    }

    public static AprovadorPapel resolveForApproval(String role, String organizationType) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(role);
        TipoOrganizacao tipoOrganizacao = TipoOrganizacao.fromStoredValue(organizationType);

        if (papel == PapelOrganizacional.PAROCO) {
            return PAROCO;
        }
        if (tipoOrganizacao == TipoOrganizacao.CONSELHO && papel == PapelOrganizacional.VICE_COORDENADOR) {
            return CONSELHO_VICE_COORDENADOR;
        }
        return CONSELHO_COORDENADOR;
    }
}
