package br.com.nsfatima.gestao.calendario.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;

public enum TipoSolicitacaoInput {
    CRIACAO_EVENTO,
    EDICAO_EVENTO,
    ALTERACAO_HORARIO,
    CANCELAMENTO,
    RECLASSIFICACAO,
    OUTRO;

    @JsonCreator
    public static TipoSolicitacaoInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoSolicitacaoInput.class);
    }
}
