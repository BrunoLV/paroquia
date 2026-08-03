package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Tipo de observação de evento conforme recebido em requisições da API. Apenas
 * {@code NOTA} é aceito no endpoint manual de observação (ver
 * {@code ObservacaoTipoManualInvalidoException}); os demais valores são gerados
 * automaticamente pelo próprio sistema ao longo do fluxo de aprovação e mutação de
 * eventos. Corresponde ao par de saída {@link TipoObservacaoResponse}.
 */
public enum TipoObservacaoInput {
    /** Nota manual registrada diretamente pelo usuário. */
    NOTA,
    /** Justificativa associada a uma solicitação de aprovação. */
    JUSTIFICATIVA,
    /** Registro automático de aprovação de uma solicitação. */
    APROVACAO,
    /** Registro automático de reprovação de uma solicitação. */
    REPROVACAO,
    /** Registro automático de cancelamento do evento. */
    CANCELAMENTO,
    /** Registro automático de alteração de data/horário do evento. */
    AJUSTE_HORARIO;

    /**
     * Normaliza o valor bruto recebido em JSON para a constante correspondente,
     * tolerando variações de caixa/formatação.
     *
     * @param rawValue valor textual recebido na requisição
     * @return constante de tipo de observação normalizada
     */
    @JsonCreator
    public static TipoObservacaoInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoObservacaoInput.class);
    }
}
