package br.com.nsfatima.gestao.organizacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoOrganizacao {
    MATRIZ,
    COMUNIDADE,
    PASTORAL,
    MOVIMENTO,
    CLERO,
    CONSELHO_PASTORAL,
    CONSELHO_ECONOMICO,
    OUTRO;

    @JsonCreator
    public static TipoOrganizacao fromJson(String rawValue) {
        if ("CONSELHO".equalsIgnoreCase(rawValue)) {
            return CONSELHO_PASTORAL;
        }
        return EnumRequestNormalizer.normalize(rawValue, TipoOrganizacao.class);
    }

    public static TipoOrganizacao fromStoredValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        if ("CONSELHO".equalsIgnoreCase(rawValue)) {
            return CONSELHO_PASTORAL;
        }
        return EnumRequestNormalizer.normalize(rawValue, TipoOrganizacao.class);
    }
}
