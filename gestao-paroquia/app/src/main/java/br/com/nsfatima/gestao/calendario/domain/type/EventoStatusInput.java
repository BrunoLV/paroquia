package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Status de um evento de calendário conforme recebido em requisições da API. Um
 * evento nasce como {@code RASCUNHO} ou já {@code CONFIRMADO}, pode ser marcado
 * como {@code ADICIONADO_EXTRA} quando criado fora do provisionamento anual regular
 * (ano litúrgico já fechado, ver {@code CalendarioBloqueadoException}), e pode ser
 * transicionado para {@code CANCELADO}. Corresponde ao par de saída
 * {@link EventoStatusResponse}.
 */
public enum EventoStatusInput {
    /** Evento ainda em elaboração, não confirmado na agenda. */
    RASCUNHO,
    /** Evento confirmado e válido na agenda. */
    CONFIRMADO,
    /** Evento criado fora do calendário anual regular (ano litúrgico já encerrado). */
    ADICIONADO_EXTRA,
    /** Evento cancelado, não ocupa mais a agenda. */
    CANCELADO;

    /**
     * Normaliza o valor bruto recebido em JSON para a constante correspondente,
     * tolerando variações de caixa/formatação.
     *
     * @param rawValue valor textual recebido na requisição
     * @return constante de status normalizada
     */
    @JsonCreator
    public static EventoStatusInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, EventoStatusInput.class);
    }
}
