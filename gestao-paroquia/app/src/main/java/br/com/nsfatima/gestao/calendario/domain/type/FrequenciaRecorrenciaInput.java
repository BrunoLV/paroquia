package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.support.domain.EnumRequestNormalizer;

/**
 * Frequência de recorrência de um evento conforme recebida em requisições da API,
 * usada por {@code RegraRecorrencia} para determinar o intervalo de repetição das
 * ocorrências geradas a partir de um evento recorrente. Corresponde ao par de saída
 * {@link FrequenciaRecorrenciaResponse}.
 */
public enum FrequenciaRecorrenciaInput {
    /** Evento se repete todos os dias dentro do período de recorrência. */
    DIARIA,
    /** Evento se repete semanalmente dentro do período de recorrência. */
    SEMANAL,
    /** Evento se repete mensalmente dentro do período de recorrência. */
    MENSAL;

    /**
     * Normaliza o valor bruto recebido em JSON para a constante correspondente,
     * tolerando variações de caixa/formatação.
     *
     * @param rawValue valor textual recebido na requisição
     * @return constante de frequência normalizada
     */
    public static FrequenciaRecorrenciaInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, FrequenciaRecorrenciaInput.class);
    }
}
