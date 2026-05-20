package br.com.nsfatima.gestao.projeto.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProjetoStatus {
    ATIVO,
    INATIVO;

    @JsonCreator
    public static ProjetoStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, ProjetoStatus.class);
    }
}
