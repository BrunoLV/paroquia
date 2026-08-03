package br.com.nsfatima.gestao.organizacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Classifica uma {@link Organizacao} do tipo {@code PASTORAL} segundo os três eixos clássicos da ação
 * pastoral paroquial: Santificação/Liturgia, Formação/Ensinar e Social/Caridade. Usado apenas para
 * agrupamento/relatórios organizacionais, sem impacto nas regras de autorização.
 */
public enum SetorPastoral {
    SANTIFICACAO_LITURGIA,
    FORMACAO_ENSINAR,
    SOCIAL_CARIDADE;

    /**
     * Normaliza um valor recebido via JSON (variações de caixa/acentuação) para a constante correspondente.
     *
     * @param value valor bruto recebido na requisição
     * @return o {@link SetorPastoral} correspondente
     */
    @JsonCreator
    public static SetorPastoral fromValue(String value) {
        return EnumRequestNormalizer.normalize(value, SetorPastoral.class);
    }

    /**
     * Serializa esta constante para o valor JSON exposto na API, que é simplesmente o nome do enum.
     *
     * @return o nome desta constante
     */
    @JsonValue
    public String toValue() {
        return name();
    }
}
