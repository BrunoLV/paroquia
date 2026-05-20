package br.com.nsfatima.gestao.aprovacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum AprovacaoStatus {
    PENDENTE,
    APROVADA,
    REPROVADA,
    FALHA_EXECUCAO;

    @JsonCreator
    public static AprovacaoStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, AprovacaoStatus.class);
    }
}
