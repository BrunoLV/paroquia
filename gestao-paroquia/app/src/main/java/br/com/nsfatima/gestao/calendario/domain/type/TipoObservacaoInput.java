package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoObservacaoInput {
    NOTA,
    JUSTIFICATIVA,
    APROVACAO,
    REPROVACAO,
    CANCELAMENTO,
    AJUSTE_HORARIO;

    @JsonCreator
    public static TipoObservacaoInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoObservacaoInput.class);
    }
}
