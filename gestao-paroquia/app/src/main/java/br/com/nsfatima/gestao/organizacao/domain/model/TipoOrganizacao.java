package br.com.nsfatima.gestao.organizacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoOrganizacao {
    PASTORAL,
    MOVIMENTO,
    LAICATO,
    CLERO,
    CONSELHO,
    OUTRO;

    @JsonCreator
    public static TipoOrganizacao fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoOrganizacao.class);
    }

    public static TipoOrganizacao fromStoredValue(String rawValue) {
        return rawValue == null ? null : EnumRequestNormalizer.normalize(rawValue, TipoOrganizacao.class);
    }
}
