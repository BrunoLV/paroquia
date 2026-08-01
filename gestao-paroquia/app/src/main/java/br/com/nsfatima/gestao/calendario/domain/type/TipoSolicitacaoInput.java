package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoSolicitacaoInput {
    CRIACAO_EVENTO,
    EDICAO_EVENTO,
    ALTERACAO_HORARIO,
    CANCELAMENTO,
    RECLASSIFICACAO,
    ALOCACAO_ATIVO_EMPRESTIMO,
    OUTRO;

    @JsonCreator
    public static TipoSolicitacaoInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoSolicitacaoInput.class);
    }
}
